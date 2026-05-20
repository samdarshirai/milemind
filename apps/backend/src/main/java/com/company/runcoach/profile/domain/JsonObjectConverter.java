package com.company.runcoach.profile.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class JsonObjectConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize JSON object.", ex);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            String payload = normalizePayload(dbData);
            return OBJECT_MAPPER.readValue(payload, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to deserialize JSON object.", ex);
        }
    }

    private String normalizePayload(String dbData) throws JsonProcessingException {
        if (dbData.startsWith("\"") && dbData.endsWith("\"")) {
            return OBJECT_MAPPER.readValue(dbData, String.class);
        }
        return dbData;
    }
}
