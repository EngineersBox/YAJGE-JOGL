package com.engineersbox.yajgejogl.core;


import com.engineersbox.yajgejogl.debug.OpenGLInfo;
import com.engineersbox.yajgejogl.input.MouseInput;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.FPSAnimator;

public interface IGameLogic {

    void init(final GL2 gl, final Window window) ;
    void input(final Window window, final MouseInput mouseInput);
    void resize(final Window window);
    void update(final GL2 gl, final float interval, final MouseInput mouseInput, final Window window);
    void render(final GL2 gl, final Window window);
    void debugInfo(final GL2 gl, final GLAutoDrawable glAutoDrawable, final Window window, final OpenGLInfo info, final FPSAnimator animator);
    void cleanup();
}