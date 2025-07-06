package io.zmeu;

public enum BlockContext {
    BLOCK,
    /*
     * context changed after =
     * so var x = {} -> object and anything else is a block statement
     * */
    OBJECT
}
