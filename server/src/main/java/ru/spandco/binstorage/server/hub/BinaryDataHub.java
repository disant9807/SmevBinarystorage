package ru.spandco.binstorage.server.hub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.spandco.logic.storage.entity.BinaryMetadata;

@Component("BinaryDataHub")
public class BinaryDataHub {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public BinaryMetadataEventSaveArgs BinaryMetadataSaved;

    public BinaryMetadataEventConfirmArgs BinaryMetadataConfirmed;

    public void NotifyBinaryMetadataSaved(BinaryMetadata binaryMetadata) {
        BinaryMetadataSaved = new BinaryMetadataEventSaveArgs(this, binaryMetadata);
        applicationEventPublisher.publishEvent(BinaryMetadataSaved);
    }

    public void NotifyBinaryMetadataConfirmed(BinaryMetadata binaryMetadata) {
        BinaryMetadataConfirmed = new BinaryMetadataEventConfirmArgs(this, binaryMetadata);
        applicationEventPublisher.publishEvent(BinaryMetadataConfirmed);
    }
}
