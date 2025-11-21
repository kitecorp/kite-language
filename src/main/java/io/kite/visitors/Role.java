package io.kite.visitors;

public enum Role {
    KEYWORD,      // e.g., input, output, schema, resource, val, var, fun
    TYPE,         // type identifiers (vm, string[], union parts)
    IDENTIFIER,   // variable/resource names
    STRING,       // "hello"
    NUMBER,       // 123, 4.56
    BOOLEAN,      // true/false
    DECORATOR,    // @allowed, @provider
    PUNCTUATION,  // punctuation like (), {}, [], =, :, , etc. (optional)
    NORMAL        // default text
}