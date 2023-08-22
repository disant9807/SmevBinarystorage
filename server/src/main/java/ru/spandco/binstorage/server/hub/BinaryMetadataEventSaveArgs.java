package ru.spandco.binstorage.server.hub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.spandco.logic.storage.entity.BinaryMetadata;

public class BinaryMetadataEventSaveArgs extends ApplicationEvent {

    public BinaryMetadataEventSaveArgs(Object source, BinaryMetadata area) {
        super(source);
        binaryMetadata = area;
    }
    public BinaryMetadata binaryMetadata;
}

