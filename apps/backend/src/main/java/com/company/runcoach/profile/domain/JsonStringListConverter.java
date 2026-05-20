package com.company.runcoach.profile.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class JsonStringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize JSON list.", ex);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            String payload = normalizePayload(dbData);
            return OBJECT_MAPPER.readValue(payload, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to deserialize JSON list.", ex);
        }
    }

    private String normalizePayload(String dbData) throws JsonProcessingException {
        if (dbData.startsWith("\"") && dbData.endsWith("\"")) {
            return OBJECT_MAPPER.readValue(dbData, String.class);
        }
        return dbData;
    }
}
