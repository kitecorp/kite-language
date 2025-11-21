package io.kite.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;


public class FileHelpers {
    public static final int LAST_MODIFIED_IN_MINUTES = 50000;

    public static File[] loadFiles() {
        return loadFiles("glob:**.kite");
    }

    public static File[] loadFiles(String extensions)  {
        var matcher = FileSystems.getDefault().getPathMatcher(extensions);
        var ignore = FileSystems.getDefault().getPathMatcher("glob:./{build,gradle,.gradle,git,.git}**,./defaults.ki");

        try (Stream<Path> pathStream = Files.find(Path.of("."), Integer.MAX_VALUE, (p, basicFileAttributes) -> {
            if (Files.isDirectory(p) || !Files.isReadable(p)) {
                return false;
            }
            if (ignore.matches(p)) {
                return false;
            }
            if (!matcher.matches(p)) {
                return false;
            }
            var fileTime = basicFileAttributes.lastModifiedTime();
            // negative if less, positive if greater
            // 1 means fileTime equals or after the provided instant argument
            // -1 means fileTime before the provided instant argument
            return ChronoUnit.MINUTES.between(fileTime.toInstant(), Instant.now()) <= LAST_MODIFIED_IN_MINUTES;
        })) {
            return pathStream.map(Path::toFile).toArray(File[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
