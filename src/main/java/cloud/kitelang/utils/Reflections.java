package cloud.kitelang.utils;

import cloud.kitelang.api.annotations.Property;
import cloud.kitelang.api.annotations.PropertyKind;

import java.lang.reflect.Field;

public class Reflections {
    public static boolean isImmutable(Field field) {
        Property annotation = field.getAnnotation(Property.class);
        return annotation != null && annotation.kind() == PropertyKind.OUTPUT;
    }
}
