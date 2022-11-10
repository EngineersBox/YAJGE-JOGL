package com.engineersbox.yajgejogl.core;

import com.engineersbox.yajgejogl.debug.OpenGLInfo;
import com.engineersbox.yajgejogl.input.MouseInput;
import com.engineersbox.yajgejogl.resources.config.io.ConfigHandler;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.FPSAnimator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.nio.IntBuffer;

public class Engine implements GLEventListener {

    private static final Logger LOGGER = LogManager.getLogger(Engine.class);

    private final Window window;
    private final FPSAnimator animator;
    private final JFrame jf;
    private final IGameLogic gameLogic;
    private final MouseInput mouseInput;
    private final String windowTitle;
    private boolean initalised;
    private GL2 gl;
    private OpenGLInfo info;

    public Engine(final String windowTitle,
                  final IGameLogic gameLogic) {
        this(
                windowTitle,
                ConfigHandler.CONFIG.video.resolution.width,
                ConfigHandler.CONFIG.video.resolution.height,
                gameLogic
        );
    }

    public Engine(final String windowTitle,
                  final int width,
                  final int height,
                  final IGameLogic gameLogic) {
        this.initalised = false;
        this.gameLogic = gameLogic;
        this.windowTitle = windowTitle;
        this.mouseInput = new MouseInput();
        this.jf = new JFrame();
        this.window = new Window(getCapabilities());
        this.window.addGLEventListener(this);
        this.window.requestFocusInWindow();
        this.window.addMouseListener(this.mouseInput);
        this.window.addMouseMotionListener(this.mouseInput);
        this.jf.getContentPane().add(this.window);
        this.jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.jf.setVisible(true);
        this.jf.setPreferredSize(new Dimension(width, height));
        this.jf.pack();
        this.animator = new FPSAnimator(this.window, ConfigHandler.CONFIG.video.fps);
        this.animator.start();
    }

    private GLCapabilities getCapabilities() {
        final GLProfile profile;
        if (System.getProperty("os.name").startsWith("Mac")) {
            Engine.LOGGER.info("OSX Detected, forcing OpenGL 4.X");
            profile = GLProfile.get(GLProfile.GL4);
        } else {
            profile = GLProfile.getMaxProgrammable(true);
        }
        return new GLCapabilities(profile);
    }

    private static GL2 getContext(final GLAutoDrawable dr) {
        if (dr.getGL().isGL2()) {
            GL2 gl = dr.getGL().getGL2();
            if (ConfigHandler.CONFIG.engine.glOptions.logs) {
                gl = new DebugGL2(gl);
                dr.setGL(gl);
            }
            return gl;
        } else {
            GL4 gl = dr.getGL().getGL4();
            if (ConfigHandler.CONFIG.engine.glOptions.logs) {
                gl = new DebugGL4(gl);
                dr.setGL(gl);
            }
            return (GL2) gl;
        }
    }

    protected void cleanup() {
        this.gameLogic.cleanup();
    }

    @Override
    public void init(final GLAutoDrawable glAutoDrawable) {
        this.gl = Engine.getContext(glAutoDrawable);
        saturateOpenGLInfo();
        Engine.LOGGER.info("[OPENGL] Created {} context", this.gl.getGLProfile().getImplName());
        this.info.log(true, this.gl);
        this.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.gl.glEnable(GL2.GL_DEPTH_TEST);
        this.gl.glEnable(GL2.GL_STENCIL_TEST);
        this.gl.glEnable(GL2.GL_BLEND);
        this.gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        if (ConfigHandler.CONFIG.engine.glOptions.cullface) {
            this.gl.glEnable(GL2.GL_CULL_FACE);
            this.gl.glCullFace(GL2.GL_BACK);
        }
        if (ConfigHandler.CONFIG.engine.glOptions.showTrianges) {
            this.gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        }
        this.gameLogic.init(this.gl, this.window);
        this.initalised = true;
    }

    private void saturateOpenGLInfo() {
        final IntBuffer supportedExtensionsCount = Buffers.newDirectIntBuffer(1);
        this.gl.glGetIntegerv(GL2.GL_NUM_EXTENSIONS, supportedExtensionsCount);
        this.info = new OpenGLInfo(
                this.gl.glGetString(GL2.GL_VERSION),
                this.gl.glGetString(GL2.GL_SHADING_LANGUAGE_VERSION),
                this.gl.glGetString(GL2.GL_VENDOR),
                this.gl.glGetString(GL2.GL_RENDERER),
                supportedExtensionsCount.get(0)
        );
    }

    @Override
    public void dispose(final GLAutoDrawable glAutoDrawable) {
        this.gameLogic.cleanup();
    }

    @Override
    public void display(final GLAutoDrawable glAutoDrawable) {
        if (!this.initalised) {
            return;
        }
        this.gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        this.mouseInput.input();
        this.gameLogic.input(this.window, this.mouseInput);
        this.gameLogic.update(
                this.gl,
                this.animator.getLastFPSPeriod(),
                this.mouseInput,
                this.window
        );
        this.gameLogic.render(this.gl, this.window);
        this.gameLogic.debugInfo(gl, glAutoDrawable, this.window, this.info, this.animator);
        this.gl.glFlush();
        glAutoDrawable.swapBuffers();
    }

    @Override
    public void reshape(final GLAutoDrawable glAutoDrawable,
                        final int x,
                        final int y,
                        final int width,
                        final int height) {
        this.window.reshape(x, y, width, height);
        this.window.updateProjectionMatrix();
    }
}
