package io.kite.Runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResourcePathTest {

    @Test
    @DisplayName("Parse simple resource path: vm.main")
    void parseSimplePath() {
        ResourcePath path = ResourcePath.parse("vm.main");

        assertNull(path.getFilePath());
        assertNull(path.getParentPath());
        assertEquals("vm", path.getType());
        assertEquals("main", path.getName());
        assertTrue(path.getSegments().isEmpty());
        assertEquals("vm.main", path.toDatabaseKey());
        assertEquals("vm.main", path.toDisplayName());
        assertTrue(path.isCollection());
    }

    @Test
    @DisplayName("Parse resource with array index: vm.servers[0]")
    void parseArrayIndex() {
        ResourcePath path = ResourcePath.parse("vm.servers[0]");

        assertNull(path.getFilePath());
        assertNull(path.getParentPath());
        assertEquals("vm", path.getType());
        assertEquals("servers", path.getName());
        assertEquals(1, path.getSegments().size());
        assertEquals(ResourcePath.PathSegment.SegmentType.ARRAY_INDEX,
                path.getSegments().get(0).getType());
        assertEquals("0", path.getSegments().get(0).getValue());
        assertEquals("vm.servers[0]", path.toDatabaseKey());
        assertTrue(path.isItem());
    }

    @Test
    @DisplayName("Parse resource with map key: vm.servers[\"web\"]")
    void parseMapKey() {
        ResourcePath path = ResourcePath.parse("vm.servers[\"web\"]");

        assertEquals("vm", path.getType());
        assertEquals("servers", path.getName());
        assertEquals(1, path.getSegments().size());
        assertEquals(ResourcePath.PathSegment.SegmentType.MAP_KEY,
                path.getSegments().get(0).getType());
        assertEquals("web", path.getSegments().get(0).getValue());
        assertEquals("vm.servers[\"web\"]", path.toDatabaseKey());
    }

    @Test
    @DisplayName("Parse resource with component: myapp.vm.servers")
    void parseWithComponent() {
        ResourcePath path = ResourcePath.parse("myapp.vm.servers");

        assertNull(path.getFilePath());
        assertEquals("myapp", path.getParentPath());
        assertEquals("vm", path.getType());
        assertEquals("servers", path.getName());
        assertEquals("myapp.vm.servers", path.toDatabaseKey());
        assertEquals("myapp.vm.servers", path.toDisplayName());
    }

    @Test
    @DisplayName("Parse resource with component and index: myapp.vm.servers[0]")
    void parseWithComponentAndIndex() {
        ResourcePath path = ResourcePath.parse("myapp.vm.servers[0]");

        assertEquals("myapp", path.getParentPath());
        assertEquals("vm", path.getType());
        assertEquals("servers", path.getName());
        assertEquals(1, path.getSegments().size());
        assertEquals("0", path.getSegments().get(0).getValue());
        assertEquals("myapp.vm.servers[0]", path.toDatabaseKey());
    }

    @Test
    @DisplayName("Parse resource with file path: modules/network.kite:vm.main")
    void parseWithFilePath() {
        ResourcePath path = ResourcePath.parse("modules/network.kite:vm.main");

        assertEquals("modules/network.kite", path.getFilePath());
        assertNull(path.getParentPath());
        assertEquals("vm", path.getType());
        assertEquals("main", path.getName());
        assertEquals("modules/network.kite:vm.main", path.toDatabaseKey());
        assertEquals("vm.main", path.toDisplayName());
    }

    @Test
    @DisplayName("Parse full path: modules/network.kite:myapp.vm.servers[\"web\"]")
    void parseFullPath() {
        ResourcePath path = ResourcePath.parse("modules/network.kite:myapp.vm.servers[\"web\"]");

        assertEquals("modules/network.kite", path.getFilePath());
        assertEquals("myapp", path.getParentPath());
        assertEquals("vm", path.getType());
        assertEquals("servers", path.getName());
        assertEquals(1, path.getSegments().size());
        assertEquals("web", path.getSegments().get(0).getValue());
        assertEquals("modules/network.kite:myapp.vm.servers[\"web\"]", path.toDatabaseKey());
        assertEquals("myapp.vm.servers[\"web\"]", path.toDisplayName());
    }

    @Test
    @DisplayName("Parse resource with multiple segments: vm.config[\"db\"][\"host\"]")
    void parseMultipleSegments() {
        ResourcePath path = ResourcePath.parse("vm.config[\"db\"][\"host\"]");

        assertEquals("vm", path.getType());
        assertEquals("config", path.getName());
        assertEquals(2, path.getSegments().size());
        assertEquals("db", path.getSegments().get(0).getValue());
        assertEquals("host", path.getSegments().get(1).getValue());
        assertEquals("vm.config[\"db\"][\"host\"]", path.toDatabaseKey());
    }

    @Test
    @DisplayName("Parse resource with mixed segments: vm.servers[0][\"config\"]")
    void parseMixedSegments() {
        ResourcePath path = ResourcePath.parse("vm.servers[0][\"config\"]");

        assertEquals(2, path.getSegments().size());
        assertEquals(ResourcePath.PathSegment.SegmentType.ARRAY_INDEX,
                path.getSegments().get(0).getType());
        assertEquals("0", path.getSegments().get(0).getValue());
        assertEquals(ResourcePath.PathSegment.SegmentType.MAP_KEY,
                path.getSegments().get(1).getType());
        assertEquals("config", path.getSegments().get(1).getValue());
    }

    @Test
    @DisplayName("Build path programmatically")
    void buildPath() {
        ResourcePath path = ResourcePath.builder()
                .parentPath("myapp")
                .type("vm")
                .name("servers")
                .build();

        assertEquals("myapp.vm.servers", path.toDatabaseKey());
    }

    @Test
    @DisplayName("Append index to path")
    void appendIndex() {
        ResourcePath base = ResourcePath.parse("vm.servers");
        ResourcePath withIndex = base.appendIndex(0);

        assertEquals("vm.servers", base.toDatabaseKey());
        assertEquals("vm.servers[0]", withIndex.toDatabaseKey());
    }

    @Test
    @DisplayName("Append key to path")
    void appendKey() {
        ResourcePath base = ResourcePath.parse("vm.servers");
        ResourcePath withKey = base.appendKey("web");

        assertEquals("vm.servers", base.toDatabaseKey());
        assertEquals("vm.servers[\"web\"]", withKey.toDatabaseKey());
    }

    @Test
    @DisplayName("Chain multiple appends")
    void chainAppends() {
        ResourcePath path = ResourcePath.parse("vm.config")
                .appendKey("database")
                .appendKey("connection")
                .appendKey("host");

        assertEquals("vm.config[\"database\"][\"connection\"][\"host\"]",
                path.toDatabaseKey());
    }

    @Test
    @DisplayName("Get base path from complex path")
    void getBasePath() {
        ResourcePath path = ResourcePath.parse("myapp.vm.servers[0][\"config\"]");

        assertEquals("myapp.vm.servers", path.getBasePath());
    }

    @Test
    @DisplayName("Test isCollection and isItem")
    void testCollectionVsItem() {
        ResourcePath collection = ResourcePath.parse("vm.servers");
        ResourcePath item = ResourcePath.parse("vm.servers[0]");

        assertTrue(collection.isCollection());
        assertFalse(collection.isItem());

        assertFalse(item.isCollection());
        assertTrue(item.isItem());
    }

    @Test
    @DisplayName("Parse with single quotes: vm.servers['web']")
    void parseSingleQuotes() {
        ResourcePath path = ResourcePath.parse("vm.servers['web']");

        assertEquals("web", path.getSegments().get(0).getValue());
        assertEquals("vm.servers[\"web\"]", path.toDatabaseKey());
    }

    @Test
    @DisplayName("Throw exception on invalid path")
    void throwOnInvalidPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            ResourcePath.parse("invalid");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ResourcePath.parse("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ResourcePath.parse(null);
        });
    }

    @Test
    @DisplayName("Parse deeply nested component path: parent.main.child.instance.vm.server")
    void parseDeepNesting() {
        ResourcePath path = ResourcePath.parse("parent.main.child.instance.vm.server");

        assertEquals("parent.main.child.instance", path.getParentPath());
        assertEquals("vm", path.getType());
        assertEquals("server", path.getName());
        assertEquals("parent.main.child.instance.vm.server", path.toDatabaseKey());
    }

    @Test
    @DisplayName("Handle complex real-world scenarios")
    void complexRealWorld() {
        // Scenario 1: Loop creating VMs in a component
        ResourcePath component = ResourcePath.parse("myapp.vm.servers");

        for (int i = 0; i < 3; i++) {
            ResourcePath instance = component.appendIndex(i);
            System.out.println("Database key: " + instance.toDatabaseKey());
            // Output: myapp.vm.servers[0], myapp.vm.servers[1], myapp.vm.servers[2]
        }

        // Scenario 2: Map-based resources
        ResourcePath configs = ResourcePath.parse("vm.environments");
        String[] envs = {"dev", "staging", "prod"};

        for (String env : envs) {
            ResourcePath envConfig = configs.appendKey(env);
            System.out.println("Database key: " + envConfig.toDatabaseKey());
            // Output: vm.environments["dev"], vm.environments["staging"], vm.environments["prod"]
        }

        // Scenario 3: Nested within component with file path
        ResourcePath fullPath = ResourcePath.builder()
                .filePath("infrastructure/main.kite")
                .parentPath("webapp")
                .type("vm")
                .name("servers")
                .build()
                .appendKey("frontend")
                .appendKey("primary");

        System.out.println("Database key: " + fullPath.toDatabaseKey());
        System.out.println("Display name: " + fullPath.toDisplayName());
        // Database key: infrastructure/main.kite:webapp.vm.servers["frontend"]["primary"]
        // Display name: webapp.vm.servers["frontend"]["primary"]
    }
}