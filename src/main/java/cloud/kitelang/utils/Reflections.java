package cloud.kitelang.utils;

import cloud.kitelang.api.annotations.Property;

import java.lang.reflect.Field;

public class Reflections {
    public static boolean isImmutable(Field field) {
        Property annotation = field.getAnnotation(Property.class);
        return annotation != null && annotation.output();
    }
}
