package com.example.portal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Convert an object to JSON string
    public static String toJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }

    // Convert a JSON string to an object
    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }

    // Convert a JSON string to a List of objects
    public static <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) throws IOException {
        return objectMapper.readValue(json, typeReference);
    }
}
