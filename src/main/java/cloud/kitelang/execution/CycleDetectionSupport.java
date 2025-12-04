package cloud.kitelang.execution;

import cloud.kitelang.execution.values.Deferred;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public interface CycleDetectionSupport {
    static @Nullable <T> Object propertyOrDeferred(Map<String, T> environment, String name) {
        T t = environment.get(name);
        // if instance was not installed yet -> it will be installed later so we return a deferred object
        return Objects.requireNonNullElseGet(t, () -> new Deferred(name));
    }
}
