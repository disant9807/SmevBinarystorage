package ru.spandco.binstorage.server.hub;

import org.springframework.context.ApplicationEvent;
import ru.spandco.logic.storage.entity.BinaryMetadata;

public class BinaryMetadataEventConfirmArgs extends ApplicationEvent {

    public BinaryMetadataEventConfirmArgs(Object source, BinaryMetadata area) {
        super(source);
        binaryMetadata = area;
    }
    public BinaryMetadata binaryMetadata;
}

