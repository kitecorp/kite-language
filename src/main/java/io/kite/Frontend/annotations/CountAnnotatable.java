package io.kite.Frontend.annotations;

/**
 * Annotation added to elements that support the @count(number) annotation.
 * For now just resource and component are supported
 */
public interface CountAnnotatable {
    Boolean counted();

    void counted(Boolean evaluatedCount);
}