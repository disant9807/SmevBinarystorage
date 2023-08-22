package ru.spandco.binstorage.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.spandco.binarystoragemodel.SaveMode;
import ru.spandco.binarystoragemodel.BinaryModel;
import ru.spandco.binstorage.server.service.BinaryMetadataServerService;

import javax.validation.constraints.Null;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/bin")
@RestController
public class BinController {

    private final Logger logger =
            LoggerFactory.getLogger(BinController.class);
    private BinaryMetadataServerService binaryMetadataServerService;

    private static final String EMPTY_STRING = "";

    @Autowired
    public void setService(BinaryMetadataServerService dependency) {
            this.binaryMetadataServerService = dependency;
    }

    @RequestMapping(
            path = "/save",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveMultipart (@RequestPart("files") MultipartFile[] files,
                                   @RequestParam Boolean isConfirmationRequered) {
        try {
            List<String> resultIds = new ArrayList<>();
            for(MultipartFile file : files) {
                String resultId = binaryMetadataServerService.Save(
                        file,
                        SaveMode.Create,
                        null,
                        file.getOriginalFilename(),
                        file.getContentType(),
                        isConfirmationRequered
                );
                resultIds.add(resultId);
                logger.info("файл сохранен с id " + resultId);
            }
            logger.info("Список файлов с длинной " + files.length
            + "успешно сохранен");
            return new ResponseEntity<>(resultIds.toArray(new String[]{}),
                    HttpStatus.OK);
        } catch (InterruptedException e) {
            logger.error("Ошибка при обработке данных перед сохранением " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            logger.error("Ошибка при сохранении данных на диск " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            path = "/",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> save(@RequestPart("file") MultipartFile file,
                                       @RequestParam SaveMode mode, @Null String id,
                                       @RequestParam String mimeType,
                                       @RequestParam String name,
                                       @RequestParam Boolean isConfirmationRequered) {

        try {
            String result = binaryMetadataServerService.Save(file, mode, id,
                    name, mimeType, isConfirmationRequered);
            logger.info("Данные успешно сохранение с ид " + result);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (InterruptedException e) {
            logger.error("Ошибка при обработке данных перед сохранением " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            logger.error("Ошибка при сохранении данных на диск " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            path = "/data",
            method = RequestMethod.GET)
    public HttpEntity<?> Get(@RequestParam String id) {
        try {
            BinaryModel resultModel = binaryMetadataServerService.Get(id);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Id", resultModel.Id);
            if (!resultModel.Name.isEmpty()) {
                headers.add("Content-Name", resultModel.Name);
            }
            if (!resultModel.MimeType.isEmpty()) {
                headers.add("Content-Type", resultModel.MimeType);
            }
            ContentDisposition contentDisposition = ContentDisposition
                    .builder("attachment")
                    .filename(resultModel.Name)
                    .build();
            headers.setContentDisposition(contentDisposition);

            return new HttpEntity<>(resultModel.Content.toByteArray(), headers);
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Проблема получения файла по id " + id +
                    " из-за ошибки: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            path = "/info",
            method = RequestMethod.GET,
            produces= MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> GetInfo(@RequestParam String id) throws Exception {
        try {
            BinaryModel resultModel = binaryMetadataServerService.GetInfo(id);

            return new ResponseEntity<>(resultModel, HttpStatus.OK);
        } catch (FileNotFoundException | IllegalArgumentException e) {
            logger.error("Проблема получения метаданных файла по id " + id +
                    " из-за ошибки: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            path = "/delete ",
            method = RequestMethod.POST)
    public ResponseEntity<?> Delete(@RequestParam String id) {
        try {
            binaryMetadataServerService.Delete(id);

            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Проблема удаление файла по id " + id +
                    " из-за ошибки: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            path = "/confirm",
            method = RequestMethod.POST)
    public ResponseEntity<?> ConfirmSave(@RequestBody String[] ids) {
        try {
            binaryMetadataServerService.Confirm(ids);
            return new ResponseEntity(HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Проблема подтверждение файлов по id " + Arrays.toString(ids) +
                    " из-за ошибки: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
