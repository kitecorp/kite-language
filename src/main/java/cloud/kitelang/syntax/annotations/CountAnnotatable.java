package cloud.kitelang.syntax.annotations;

/**
 * Annotation added to elements that support the @count(number) annotation.
 * For now just resource and component are supported
 */
public interface CountAnnotatable {
    boolean isCounted();

    void setCounted(boolean evaluatedCount);
}