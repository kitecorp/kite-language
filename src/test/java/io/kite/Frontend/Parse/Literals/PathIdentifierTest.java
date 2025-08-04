package io.kite.Frontend.Parse.Literals;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PathIdentifierTest {
    @Test
    void testPathIdentifier() {
        var id = PathIdentifier.of("Test");
        assertEquals("Test", id.string());
        assertArrayEquals(new String[]{"Test"}, id.getPaths());
    }

    @Test
    void testPathIdentifierMultiple() {
        var id = PathIdentifier.of("Test.VM");
        assertEquals("Test.VM", id.string());
        assertArrayEquals(new String[]{"Test", "VM"}, id.getPaths());
    }

}