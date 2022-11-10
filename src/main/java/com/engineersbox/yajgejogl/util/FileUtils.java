package com.engineersbox.yajgejogl.util;

import java.io.File;

public class FileUtils {
    public static boolean fileExists(final String fileName) {
        final File file = new File(fileName);
        return file.exists() && file.isFile();
    }
}
