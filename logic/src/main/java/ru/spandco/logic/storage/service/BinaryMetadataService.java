package ru.spandco.logic.storage.service;

import javassist.NotFoundException;
import ru.spandco.logic.storage.dao.BinaryMetadataDao;
import ru.spandco.logic.storage.entity.BinaryMetadata;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service("BinaryMetadataService")
@RequiredArgsConstructor
public class BinaryMetadataService {
    private final BinaryMetadataDao dao;

    public void commitTransaction(String[] binaryIds) throws NotFoundException {
        List<BinaryMetadata> bins = dao.getBinaryMetadataByIds(binaryIds);

        if(bins.stream().count() != binaryIds.length) {
            List<String> existingIds = bins.stream()
                    .map(BinaryMetadata::getId).collect(Collectors.toList());
            String[] notFound = (String[]) Arrays.stream(binaryIds)
                    .filter(e -> !existingIds.contains(e)).toArray();

            throw new NotFoundException(
                    "Идентификаторы [" + String.join(",", notFound) + "] не найдены"
            );
        }

        for(BinaryMetadata bin : bins) {
            bin.setTimeToDelete(null);
        }

        dao.saveAll(bins);
    }

    public String[] getUnconfirmedTimeoutIds() {
        List<BinaryMetadata> binaryMetadata = dao
                .getUnconfirmedBinaryMetadataArray();

        return binaryMetadata.stream()
                .map(BinaryMetadata::getId).toArray(String[]::new);
    }

    public List<BinaryMetadata> getUnconfirmedBinaryMetadataArray() {
        return dao.getUnconfirmedBinaryMetadataArray();
    }

    public void saveBinaryMetadata(BinaryMetadata binary) {


        BinaryMetadata binaryMetadata =  dao.getBinaryMetadataById(binary.getId());
        if (binaryMetadata == null) {
            binaryMetadata = new BinaryMetadata();
            binaryMetadata.setName(binary.getName());
            binaryMetadata.setMimeType(binary.getMimeType());
            binaryMetadata.setTimeToDelete(binary.getTimeToDelete());
            binaryMetadata.setId(binary.getId());
        } else {
            binaryMetadata = binary;
        }

        dao.save(binaryMetadata);
    }

    public List<BinaryMetadata> getBinaryMetadata(String[] ids) {
        return dao.getBinaryMetadataByIds(ids);
    }

    public void deleteBinaryMetadata(String id) {
        dao.deleteById(id);
    }

    public void confirmSave(String[] binaryStorageIds) {
        List<BinaryMetadata> binaryMetadata = dao.getBinaryMetadataByIds(binaryStorageIds);

        if (binaryMetadata.stream().count() != binaryStorageIds.length) {
            // Ошибка
        }

        for(BinaryMetadata metadata : binaryMetadata) {
            metadata.setTimeToDelete(null);
            dao.save(metadata);
        }
    }

    public Boolean IsConfirmed(String id) {
        BinaryMetadata binaryMetadata = dao.getBinaryMetadataById(id);
        return binaryMetadata.getTimeToDelete() == null;
    }
}
