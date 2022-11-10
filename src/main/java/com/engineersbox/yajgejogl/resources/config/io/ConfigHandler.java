package com.engineersbox.yajgejogl.resources.config.io;

import com.engineersbox.yajgejogl.resources.config.Config;
import com.engineersbox.yajgejogl.resources.loader.ResourceLoader;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.io.IOException;

public abstract class ConfigHandler {
    private static final String CONFIG_FILE_PARAMETER = "c4610.config";
    public static final Config CONFIG;

    static {
        final com.typesafe.config.Config typesafeConfig;
        try {
            typesafeConfig = ConfigFactory.parseString(ResourceLoader.loadResourceAsString("/config.conf")).resolve();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        CONFIG = new Config(typesafeConfig);
    }
}
