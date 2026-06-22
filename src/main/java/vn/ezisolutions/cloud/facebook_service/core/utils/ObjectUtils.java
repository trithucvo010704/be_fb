package vn.ezisolutions.cloud.facebook_service.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.List;

public class ObjectUtils {

    public static <T> T cast(String value, ObjectMapper mapper) throws JsonProcessingException {
        TypeReference<T> typeRef = new TypeReference<>() {
        };
        return mapper.readValue(value, typeRef);
    }

    public static <T> List<T> castToList(String value, ObjectMapper mapper) throws JsonProcessingException {
        TypeReference<List<T>> typeRef = new TypeReference<List<T>>() {
        };
        return mapper.readValue(value, typeRef);
    }


    public static boolean hasToString(Class<?> clazz) {
        Method m;
        try {
            m = clazz.getMethod("toString");
        } catch (NoSuchMethodException e) {
            return false;
        }
        return (m.getDeclaringClass() != Object.class);
    }

    public static <T> String toString(T t) {
        if (!hasToString(t.getClass())) {
            throw new RuntimeException(String.format("%s can not toString", t.getClass()));
        }
        return t.toString();
    }

}
