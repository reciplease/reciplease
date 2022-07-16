package org.reciplease.utils;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResourceUtils {
    @SneakyThrows
    public static String readTestResource(final Class<?> aClass, final String fileName) {
        final var packagePath = aClass.getName()
                .replace('.', '/')
                .replace('$', '/');
        return Files.readString(Paths.get("src/test/resources", packagePath, fileName), StandardCharsets.UTF_8);
    }
}