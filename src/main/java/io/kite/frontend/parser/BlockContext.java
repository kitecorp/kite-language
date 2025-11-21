package io.kite.frontend.parser;

public enum BlockContext {
    BLOCK,
    /*
     * context changed after =
     * so var x = {} -> object and anything else is a block statement
     * */
    OBJECT
}
