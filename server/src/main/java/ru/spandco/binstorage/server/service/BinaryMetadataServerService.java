package ru.spandco.binstorage.server.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.spandco.binstorage.server.hub.BinaryDataHub;
import ru.spandco.binstorage.server.extensions.BinaryModelExtension;
import ru.spandco.binarystoragemodel.SaveMode;
import ru.spandco.binarystoragemodel.BinaryModel;
import ru.spandco.logic.storage.entity.BinaryMetadata;
import ru.spandco.logic.storage.service.BinaryMetadataService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service("BinaryMetadataServerService")
@RequiredArgsConstructor
@ExtensionMethod(BinaryModelExtension.class)
public class BinaryMetadataServerService {

    @Autowired
    private BinaryDataHub binaryDataHub;

    private BinaryMetadataService binaryMetadataService;

    private String rootFolder = "D:\\GitSmevJava\\binaryStorage\\data";

    private Integer commitTimeoutSeconds = 2;

    @Autowired
    public void setService(BinaryMetadataService dependency) {
        this.binaryMetadataService = dependency;
    }

    public BinaryModel Get(String id) throws IOException {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException();

        Path dataFilePath = GetDataFilePath(id);

        if (!Files.exists(dataFilePath))
            throw new FileNotFoundException("Запрошенный файл не найден");

        List<BinaryMetadata> binaryMetadataList
                = binaryMetadataService.getBinaryMetadata(new String[]{ id });

        BinaryMetadata metadata = binaryMetadataList.stream()
                .findFirst()
                .get();

        try (InputStream readStream = Files
                .newInputStream(new File(dataFilePath.toUri()).toPath());
             ByteArrayOutputStream writeStream = new ByteArrayOutputStream();) {
            IOUtils.copy(readStream, writeStream);
            return new BinaryModel().MapFromBinaryMetadata(metadata, writeStream);
        }
    }

    public BinaryModel GetInfo(String id) throws FileNotFoundException {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException();

        Path dataFilePath = GetDataFilePath(id);

        if (!Files.exists(dataFilePath))
            throw new FileNotFoundException("Запрошенный файл не найден");

        List<BinaryMetadata> binaryMetadataList
                = binaryMetadataService.getBinaryMetadata(new String[]{ id });

        BinaryMetadata metadata = binaryMetadataList.stream()
                .findFirst()
                .get();

        return new BinaryModel().MapFromBinaryMetadata(metadata, null);
    }

    public void Delete(String id) throws IOException {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException();

        Path dataPath = GetDataFilePath(id);
        Files.delete(dataPath);

        binaryMetadataService.deleteBinaryMetadata(id);
    }

    public void Confirm(String[] ids) {
        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException();
        }

        binaryMetadataService.confirmSave(ids);
    }

    public Boolean IsConfirmed(String id) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException();

        return binaryMetadataService.IsConfirmed(id);
    }

    public String Save(MultipartFile file, SaveMode mode,
                       String id, String name, String mimeType,
                       Boolean isConfirmationRequered) throws IOException, InterruptedException {

        if (id == null || id.length() == 0) {
            id = UUID.randomUUID().toString();
        }

        else if (mode == SaveMode.Undefined) {
            throw new IllegalArgumentException("При явно заданном идентификаторе должен " +
                    "быть указан способ записи Content-SaveMode: Create, Update или CreateOrUpdate");
        }

        else if (mode == SaveMode.Create && Files.exists(GetDataFilePath(id))) {
            throw new IllegalArgumentException("Явно указан способ записи 'создание', но файл уже существует");
        }

        else if (mode == SaveMode.Update && !Files.exists(GetDataFilePath(id))) {
            throw new IllegalArgumentException("Указан способ записи 'Обновление', но файла не существует");
        }

        GetOrCreateDirectory(id);

        ExecutorService taskExecutor = Executors.newFixedThreadPool(2);
        // Запуск задачи сохранения файла в директорию
        String finalId = id;
        Runnable saveDataTask = () -> {
            try {
                byte[] data = file.getBytes();
                SaveDataAsync(data, finalId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        taskExecutor.execute(saveDataTask);

        // Запуск задачи сохранения бинарных данных файла
        BinaryMetadata binaryMetadata = new BinaryMetadata();
        binaryMetadata.setId(id);
        binaryMetadata.setMimeType(mimeType);
        binaryMetadata.setName(name);

        if (isConfirmationRequered) {
            Date date = new Date();
            date.setTime((date.getTime() / 1000) + commitTimeoutSeconds) ;
            binaryMetadata.setTimeToDelete(date);
        }

        Runnable saveMetadataTask = new Runnable() {
            public void run() {
                binaryDataHub.NotifyBinaryMetadataSaved(binaryMetadata);
                binaryMetadataService.saveBinaryMetadata(binaryMetadata);
            }
        };
        taskExecutor.execute(saveMetadataTask);

        // Ожидаем завершения всех задач
        taskExecutor.shutdown();
        try {
            taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new InterruptedException("Ошибка завершения потоков сохранение файла" +
                    "с id " + id);
        }

        return id;
    }

    protected void SaveDataAsync(byte[] data, String id) throws IOException {
        Path filePath = GetDataFilePath(id);
        try (InputStream is = new ByteArrayInputStream(data);
              OutputStream outputStream = Files
                      .newOutputStream(Paths.get(filePath.toString()))) {
            IOUtils.copy(is, outputStream);
        } catch (IOException ignored) {
            throw new IOException("Ошибка сохранение файла на диск");
        }
    }

    private String GetOrCreateDirectory(String id) throws IOException {
        Path directoryPath = Paths.get(GetDataFilePath(id).toUri()).getParent();
        Files.createDirectory(directoryPath);
        return directoryPath.toString();
    }

    private Path GetDataFilePath(String id) {
        return Paths.get(rootFolder, GetSubfolderPath(id), id + ".bin");
    }

    private static String GetSubfolderPath(String id) {
        return new String(new char[]{
                id.toCharArray()[0],
                id.toCharArray()[1]
        });
    }

}
