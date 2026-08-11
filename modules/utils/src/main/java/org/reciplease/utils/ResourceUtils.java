package org.reciplease.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceUtils {
    public static String readTestResource(final Class<?> aClass, final String fileName) throws IOException {
        final var packagePath = aClass.getName()
                .replace('.', '/')
                .replace('$', '/');
        return Files.readString(Path.of("src/test/resources", packagePath, fileName));
    }
}