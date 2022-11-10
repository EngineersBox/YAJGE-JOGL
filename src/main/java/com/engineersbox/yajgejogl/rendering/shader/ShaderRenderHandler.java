package com.engineersbox.yajgejogl.rendering.shader;

import com.engineersbox.yajgejogl.rendering.RenderContext;
import com.engineersbox.yajgejogl.resources.BindableResource;
import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import com.jogamp.opengl.GL2;

import java.util.function.Consumer;

public abstract class ShaderRenderHandler implements BindableResource {

    protected final GL2 gl;
    protected final Shader shader;

    protected ShaderRenderHandler(final GL2 gl, final Shader shader) {
        this.gl = gl;
        this.shader = shader;
    }

    public abstract void render(final RenderContext context);

    public Shader provideShader() {
        return this.shader;
    }

    @Override
    public void bind() {
        this.shader.bind();
    }

    @Override
    public void unbind() {
        this.shader.unbind();
    }

    @Override
    public void destroy() {
        this.shader.destroy();
    }
}
