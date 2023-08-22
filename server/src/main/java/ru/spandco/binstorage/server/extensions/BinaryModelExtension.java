package ru.spandco.binstorage.server.extensions;

import lombok.experimental.UtilityClass;
import ru.spandco.logic.storage.entity.BinaryMetadata;
import java.io.ByteArrayOutputStream;
import ru.spandco.binarystoragemodel.BinaryModel;

@UtilityClass
public class BinaryModelExtension {
    public static BinaryModel MapFromBinaryMetadata (BinaryModel model, BinaryMetadata binaryMetadata, ByteArrayOutputStream content) {
        model.Id = binaryMetadata.getId();
        model.Version = binaryMetadata.getVersion();
        model.Name = binaryMetadata.getName();
        model.MimeType = binaryMetadata.getMimeType();
        model.Size = binaryMetadata.getSize();
        model.TimeToDelete = binaryMetadata.getTimeToDelete();
        model.Content = content;

        return model;
    }
}
