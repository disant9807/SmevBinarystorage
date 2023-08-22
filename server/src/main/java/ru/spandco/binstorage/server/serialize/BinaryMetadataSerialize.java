package ru.spandco.binstorage.server.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.spandco.logic.storage.entity.BinaryMetadata;

import java.io.IOException;

public class BinaryMetadataSerialize extends JsonSerializer<BinaryMetadata> {

    @Override
    public void serialize(BinaryMetadata metadata, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("ID", metadata.getId());
        jsonGenerator.writeNumberField("Version", metadata.getVersion());
        jsonGenerator.writeStringField("Name", metadata.getName());
        jsonGenerator.writeStringField("MimeType", metadata.getMimeType());
        jsonGenerator.writeNumberField("Size", metadata.getSize());
        jsonGenerator.writeStringField("TimeToDelete", metadata.getTimeToDelete().toString());
        jsonGenerator.writeEndObject();

    }
}
