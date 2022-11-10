package com.engineersbox.yajgejogl.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.PrintStream;

public abstract class LoggerCompat {

    public static PrintStream asPrintStream(final Logger logger,
                                            final Level level) {
        return new PrintStream(IoBuilder.forLogger(logger).setLevel(level).buildOutputStream());
    }
}
