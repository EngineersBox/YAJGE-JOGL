package com.engineersbox.yajgejogl.debug;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record OpenGLInfo(String version,
                         String glslVersion,
                         String vendor,
                         String renderer,
                         int extensions) {

    private static final Logger LOGGER = LogManager.getLogger(OpenGLInfo.class);

    public void log(final boolean showExtensions,
                    final GL2 gl) {
        OpenGLInfo.LOGGER.info("[OPENGL] OpenGL Version: {}", this.version);
        OpenGLInfo.LOGGER.info("[OPENGL] GLSL Version: {}", this.glslVersion);
        OpenGLInfo.LOGGER.info("[OPENGL] Vendor: {}", this.vendor);
        OpenGLInfo.LOGGER.info("[OPENGL] Renderer: {}", this.renderer);
        OpenGLInfo.LOGGER.info("[OPENGL] Found {} supported Extensions", this.extensions);
        if (!showExtensions) {
            return;
        }
        for (int i = 0; i < this.extensions; i++) {
            OpenGLInfo.LOGGER.info("\t{}. {}", i + 1, gl.glGetStringi(GL2.GL_EXTENSIONS, i));
        }

    }

}
