package cloud.kitelang.execution;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a fully qualified entity path for resources or components.
 * Can represent any hierarchical entity in the system.
 * <p>
 * - File path (optional): Which file/module the entity is defined in
 * - Parent path (optional): Parent entity path (component or resource collection)
 * - Type: The entity type (e.g., "vm", "webapp", "subnet")
 * - Name: The entity instance name (e.g., "main", "servers", "primary")
 * - Segments: Array/map accessors for collection items
 * <p>
 * Examples:
 * <p>
 * Resources:
 * - vm.main
 * - vm.servers[0]
 * - vm.servers["web"]
 * - myapp.vm.servers["web"]  (resource in component)
 * - modules/network.kite:myapp.vm.servers["web"]  (with file path)
 * <p>
 * Components:
 * - webapp.main
 * - webapp.servers[0]
 * - webapp.environments["prod"]
 * - parent.main.child.instance  (nested component)
 */
@Data
@Builder
@Slf4j
public class ResourcePath {
    private String filePath;        // e.g., "modules/network.kite"
    private ResourcePath parentPath;   // e.g., ResourcePath(type=webapp, name=myapp)
    private String type;    // e.g., "vm"
    private String name;    // e.g., "servers"

    @Builder.Default
    private List<PathSegment> segments = new ArrayList<>();

    /**
     * Parse an entity path string into a ResourcePath object.
     * Works for both resource and component paths.
     * <p>
     * Examples:
     * Resources:
     * - "vm.main" -> ResourcePath(type=vm, name=main)
     * - "vm.servers[0]" -> ResourcePath(type=vm, name=servers, segments=[ArrayIndex(0)])
     * - "myapp.vm.servers[\"web\"]" -> ResourcePath(parentPath=myapp, type=vm, name=servers, segments=[MapKey("web")])
     * - "modules/network.kite:myapp.vm.servers[\"web\"]" -> Full path with file
     * <p>
     * Components:
     * - "webapp.main" -> ResourcePath(type=webapp, name=main)
     * - "webapp.servers[0]" -> ResourcePath(type=webapp, name=servers, segments=[ArrayIndex(0)])
     * - "parent.main.child.instance" -> ResourcePath(parentPath=parent.main, type=child, name=instance)
     */
    public static ResourcePath parse(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        ResourcePathBuilder builder = ResourcePath.builder();
        builder.segments(new ArrayList<>());

        String remaining = path;

        // Extract file path if present (before ':')
        if (remaining.contains(":")) {
            int colonIndex = remaining.indexOf(":");
            builder.filePath(remaining.substring(0, colonIndex));
            remaining = remaining.substring(colonIndex + 1);
        }

        // Extract segments (everything in brackets)
        List<PathSegment> segments = extractSegments(remaining);

        // Remove segments from the string to get the base path
        String basePath = remaining.replaceAll("\\[.*?\\]", "");

        // Split base path by dots
        String[] parts = basePath.split("\\.");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid path format. Expected at least 'type.name': " + path);
        }

        // Determine if we have a parent path
        if (parts.length == 2) {
            // Simple: type.name
            builder.type(parts[0]);
            builder.name(parts[1]);
        } else if (parts.length == 3) {
            // With parent: parent.type.name (e.g., "myapp.vm.servers")
            // The parent is a single component instance name - create ResourcePath with just name
            builder.parentPath(ResourcePath.builder()
                    .name(parts[0])
                    .segments(new ArrayList<>())
                    .build());
            builder.type(parts[1]);
            builder.name(parts[2]);
        } else if (parts.length > 3) {
            // Nested: parent.path.goes.here.type.name (e.g., "parent.main.child.instance.vm.server")
            // Recursively parse the parent parts
            String[] parentParts = new String[parts.length - 2];
            System.arraycopy(parts, 0, parentParts, 0, parts.length - 2);
            builder.parentPath(parse(String.join(".", parentParts)));
            builder.type(parts[parts.length - 2]);
            builder.name(parts[parts.length - 1]);
        }

        builder.segments(segments);

        return builder.build();
    }

    /**
     * Extract all path segments (array indices and map keys) from a path string
     */
    private static List<PathSegment> extractSegments(String path) {
        List<PathSegment> segments = new ArrayList<>();

        // Pattern to match [0], ["key"], or ['key']
        Pattern pattern = Pattern.compile("\\[(\\d+|\"[^\"]*\"|'[^']*')\\]");
        Matcher matcher = pattern.matcher(path);

        while (matcher.find()) {
            String match = matcher.group(1);

            if (match.matches("\\d+")) {
                // Array index
                segments.add(PathSegment.builder()
                        .type(PathSegment.SegmentType.ARRAY_INDEX)
                        .value(match)
                        .build());
            } else {
                // Map key (remove quotes)
                String key = match.substring(1, match.length() - 1);
                segments.add(PathSegment.builder()
                        .type(PathSegment.SegmentType.MAP_KEY)
                        .value(key)
                        .build());
            }
        }

        return segments;
    }

    /**
     * Get the parent path as a string for building full paths.
     * Recursively traverses the parent hierarchy.
     */
    private String getParentPathString() {
        if (parentPath == null) {
            return null;
        }
        return parentPath.toDisplayName();
    }

    /**
     * Build the full database key for this entity (resource or component)
     * Format: [filePath:]parentPath.type.name[segments...]
     */
    public String toDatabaseKey() {
        StringBuilder sb = new StringBuilder();

        if (filePath != null && !filePath.isEmpty()) {
            sb.append(filePath).append(":");
        }

        String parentStr = getParentPathString();
        if (parentStr != null && !parentStr.isEmpty()) {
            sb.append(parentStr).append(".");
        }

        // Handle the case where this ResourcePath only has a name (e.g., component instance)
        if (type != null) {
            sb.append(type).append(".");
        }
        sb.append(name);

        for (PathSegment segment : segments) {
            sb.append(segment.toString());
        }

        return sb.toString();
    }

    /**
     * Build a display-friendly name (without file path)
     */
    public String toDisplayName() {
        StringBuilder sb = new StringBuilder();

        String parentStr = getParentPathString();
        if (parentStr != null && !parentStr.isEmpty()) {
            sb.append(parentStr).append(".");
        }

        // Handle the case where this ResourcePath only has a name (e.g., component instance)
        if (type != null) {
            sb.append(type).append(".");
        }
        sb.append(name);

        for (PathSegment segment : segments) {
            sb.append(segment.toString());
        }

        return sb.toString();
    }

    /**
     * Build a display-friendly name (without file path)
     */
    public String toSegmentName() {
        StringBuilder sb = new StringBuilder();

        String parentStr = getParentPathString();
        if (parentStr != null && !parentStr.isEmpty()) {
            sb.append(parentStr).append(".");
        }

        sb.append(name);

        for (PathSegment segment : segments) {
            sb.append(segment.toString());
        }

        return sb.toString();
    }

    /**
     * Create a child path by appending a segment
     */
    public ResourcePath append(PathSegment segment) {
        List<PathSegment> newSegments = new ArrayList<>(this.segments);
        newSegments.add(segment);

        return ResourcePath.builder()
                .filePath(this.filePath)
                .parentPath(this.parentPath)
                .type(this.type)
                .name(this.name)
                .segments(newSegments)
                .build();
    }

    /**
     * Create a child path by appending an array index
     */
    public ResourcePath appendIndex(int index) {
        return append(PathSegment.builder()
                .type(PathSegment.SegmentType.ARRAY_INDEX)
                .value(String.valueOf(index))
                .build());
    }

    /**
     * Create a child path by appending an array index
     */
    public ResourcePath appendIndex(Object index) {
        return append(PathSegment.builder()
                .type(PathSegment.SegmentType.ARRAY_INDEX)
                .value(String.valueOf(index))
                .build());
    }

    /**
     * Create a child path by appending a map key
     */
    public ResourcePath appendKey(String key) {
        return append(PathSegment.builder()
                .type(PathSegment.SegmentType.MAP_KEY)
                .value(key)
                .build());
    }

    /**
     * Get the base path without segments (for grouping entities)
     * Useful for finding all items in a collection
     */
    public String getBasePath() {
        StringBuilder sb = new StringBuilder();

        String parentStr = getParentPathString();
        if (parentStr != null && !parentStr.isEmpty()) {
            sb.append(parentStr).append(".");
        }

        // Handle the case where this ResourcePath only has a name (e.g., component instance)
        if (type != null) {
            sb.append(type).append(".");
        }
        sb.append(name);

        return sb.toString();
    }

    /**
     * Check if this path represents a collection (has no segments)
     */
    public boolean isCollection() {
        return segments.isEmpty();
    }

    /**
     * Check if this path represents an individual item (has segments)
     */
    public boolean isItem() {
        return !segments.isEmpty();
    }

    /**
     * Represents a single path segment (array index or map key)
     */
    @Data
    @Builder
    public static class PathSegment {
        private SegmentType type;
        private String value;

        @Override
        public String toString() {
            return switch (type) {
                case ARRAY_INDEX -> "[" + value + "]";
                case MAP_KEY -> "[\"" + value + "\"]";
                case PROPERTY -> "." + value;
            };
        }

        public enum SegmentType {
            ARRAY_INDEX,    // [0], [1], [2]
            MAP_KEY,        // ["web"], ["db"]
            PROPERTY        // .hostname, .port
        }
    }
}