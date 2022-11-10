package com.engineersbox.yajgejogl.resources.loader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ResourceLoader {
    private static final Logger LOGGER = LogManager.getLogger(ResourceLoader.class);

    private ResourceLoader() {
        throw new IllegalStateException("Utility class");
    }

    public static File loadResourceAsFile(final String fileName) throws URISyntaxException, IOException {
        final URL resource = ResourceLoader.class.getResource(fileName);
        if (resource == null) {
            throw new IOException(String.format(
                    "Could not find resource: %s",
                    fileName
            ));
        }
        final File fileResource = new File(resource.toURI());
        if (!fileResource.exists()) {
            throw new IOException(String.format(
                    "Could not find resource: %s",
                    fileName
            ));
        }
        return fileResource;
    }

    public static InputStream loadResourceAsStream(final String fileName) throws IOException {
        final URL resource = ResourceLoader.class.getResource(formatFilename(fileName));
        if (resource == null) {
            throw new RuntimeException(String.format(
                    "Could not read resource file %s",
                    fileName
            ));
        }
        try {
            if (!new File(resource.toURI()).exists()) {
                throw new IOException(String.format(
                        "Could not read resource file %s", fileName
                ));
            }
        } catch (final URISyntaxException e) {
            throw new IOException(String.format(
                    "Could not read resource file %s", fileName
            ), e);
        }
        final InputStream stream = ResourceLoader.class.getResourceAsStream(formatFilename(fileName));
        if (stream == null) {
            throw new IOException(String.format(
                    "Could not find resource: %s",
                    fileName
            ));
        }
        return stream;
    }

    public static String loadAsString(final String fileName) {
        String contents = null;
        try  {
            final File file = new File(fileName);
            if (!file.exists()) {
                throw new IOException();
            }
            contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
        } catch (final IOException e) {
            LOGGER.error("Could not read file {}", fileName, e);
        }
        return contents;
    }

    public static String loadResourceAsString(final String fileName) throws IOException {
        final URL resource = ResourceLoader.class.getResource(formatFilename(fileName));
        if (resource == null) {
            throw new RuntimeException(String.format(
                    "Could not read resource file %s",
                    fileName
            ));
        }
        try {
            if (!new File(resource.toURI()).exists()) {
                throw new IOException(String.format(
                        "Could not read resource file %s", fileName
                ));
            }
        } catch (final URISyntaxException e) {
            throw new IOException(String.format(
                    "Could not read resource file %s", fileName
            ), e);
        }
        try (final InputStream inputStream = ResourceLoader.class.getResourceAsStream(formatFilename(fileName))) {
            if (inputStream == null) {
                throw new IOException(String.format(
                        "Could not find resource: %s",
                        fileName
                ));
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            LOGGER.error("Could not open resource {} as a stream", fileName, e);
        }
        return null;
    }

    public static List<String> loadResourceAsStringLines(final String fileName) {
        final List<String> lines = new ArrayList<>();
        final URL resource = ResourceLoader.class.getResource(formatFilename(fileName));
        if (resource == null) {
            throw new RuntimeException(String.format(
                    "Could not read resource file %s",
                    fileName
            ));
        }
        final File resourceFile;
        try {
            resourceFile = new File(resource.toURI());
            if (!resourceFile.exists()) {
                throw new IllegalStateException(String.format(
                        "Could not read resource file %s", fileName
                ));
            }
        } catch (final URISyntaxException e) {
            throw new IllegalStateException(String.format(
                    "Could not read resource file %s", fileName
            ), e);
        }

        try (final LineIterator lineIterator = FileUtils.lineIterator(new File(resource.toURI()), StandardCharsets.UTF_8.name())) {
            while (lineIterator.hasNext()) {
                lines.add(lineIterator.nextLine());
            }
        } catch (final IOException | URISyntaxException e) {
            throw new IllegalStateException(String.format(
                    "Could not read resource file %s", fileName
            ), e);
        }
        return lines;
    }

    private static String formatFilename(final String filename) {
        if (filename.startsWith("/")) {
            return filename;
        }
        return "/" + filename;
    }
}
