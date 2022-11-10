package com.engineersbox.yajgejogl.resources.assets.shader;

import com.engineersbox.yajgejogl.logging.LoggerCompat;
import com.engineersbox.yajgejogl.rendering.scene.atmosphere.Fog;
import com.engineersbox.yajgejogl.rendering.scene.lighting.Attenuation;
import com.engineersbox.yajgejogl.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajgejogl.rendering.scene.lighting.PointLight;
import com.engineersbox.yajgejogl.rendering.scene.lighting.SpotLight;
import com.engineersbox.yajgejogl.resources.BindableResource;
import com.engineersbox.yajgejogl.resources.assets.material.Material;
import com.engineersbox.yajgejogl.util.UniformUtils;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLUniformData;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;

public class Shader extends ShaderProgram implements BindableResource {

    private static final Logger LOGGER = LogManager.getLogger(Shader.class);
    private static final PrintStream LOGGER_STREAM = LoggerCompat.asPrintStream(Shader.LOGGER, Level.ERROR);

    private ShaderCode vertexShader;
    private ShaderCode fragmentShader;
    private ShaderCode geometryShader;
    private boolean valid;
    private final GL2 gl;

    public Shader(final GL2 gl) {
        super.init(gl);
        this.gl = gl;
        this.valid = true;
    }

    public Shader withVertex(final String path) {
        final ShaderCode code = createShaderCode(GL4.GL_VERTEX_SHADER, path);
        if (super.linked()) {
            super.replaceShader(this.gl, this.vertexShader, code, Shader.LOGGER_STREAM);
        } else {
            super.add(this.gl, code, Shader.LOGGER_STREAM);
        }
        this.vertexShader = code;
        return this;
    }

    public Shader withFragment(final String path) {
        final ShaderCode code = createShaderCode(GL4.GL_FRAGMENT_SHADER, path);
        if (super.linked()) {
            super.replaceShader(this.gl, this.fragmentShader, code, Shader.LOGGER_STREAM);
        } else {
            super.add(this.gl, code, Shader.LOGGER_STREAM);
        }
        this.fragmentShader = code;
        return this;
    }

    public Shader withGeometryShader(final String path) {
        final ShaderCode code = createShaderCode(GL4.GL_GEOMETRY_SHADER, path);
        if (super.linked()) {
            super.replaceShader(this.gl, this.geometryShader, code, Shader.LOGGER_STREAM);
        } else {
            super.add(this.gl, code, Shader.LOGGER_STREAM);
        }
        this.geometryShader = code;
        return this;
    }

    private ShaderCode createShaderCode(final int type,
                                        final String path) {
        final String shaderType = ShaderCode.shaderTypeStr(type);
        Shader.LOGGER.trace("[Shader: {}] Creating {} code from {}", super.program(), shaderType, path);
        final ShaderCode code = ShaderCode.create(this.gl, type, 1, Shader.class, new String[]{path}, false);
        if (code == null) {
            throw new GLException(String.format(
                    "Unable to create ShaderCode instance for %s from source %s",
                    shaderType,
                    path
            ));
        }
        Shader.LOGGER.debug("[Shader: {}] Compiling {} program", super.program(), shaderType);
        this.valid &= code.compile(this.gl) && code.isValid();
        return code;
    }

    public void build() {
        if (!this.valid) {
            Shader.LOGGER.warn("[Shader: {}] Marked as invalid, skipping link and validation", super.program());
            return;
        }
        Shader.LOGGER.debug("[Shader: {}] Linking and validating program", super.program());
        this.valid &= super.link(this.gl, Shader.LOGGER_STREAM);
        this.valid &= super.validateProgram(this.gl, Shader.LOGGER_STREAM);
    }

    public void setUniform(final GLUniformData uniform) {
//        Shader.LOGGER.trace("[Shader: {}] Binding uniform {} to shader", super.program(), uniform);
        if (uniform.setLocation(this.gl, super.program()) < 0) {
            Shader.LOGGER.error("[Shader: {}] Cannot bind uniform \"{}\", program has no matching location", super.program(), uniform.getName());
            return;
        }
        this.gl.glUniform(uniform);
    }

    public void setUniform(final String name,
                           final Material material) {
        setUniform(new GLUniformData(
                name + ".diffuse",
                4,
                material.getDiffuseColour().get(Buffers.newDirectFloatBuffer(4))
        ));
//        setUniform(new GLUniformData(
//                name + ".ambient",
//                4,
//                material.getAmbientColour().get(Buffers.newDirectFloatBuffer(4))
//        ));
        setUniform(new GLUniformData(
                name + ".specular",
                4,
                material.getSpecularColour().get(Buffers.newDirectFloatBuffer(4))
        ));
        setUniform(new GLUniformData(
                name + ".reflectance",
                material.getReflectance()
        ));
        setUniform(new GLUniformData(
                name + ".hasTexture",
                material.getAlbedo() != null ? 1 : 0
        ));
        setUniform(new GLUniformData(
                name + ".hasNormalMap",
                material.getNormalMap() != null ? 1 : 0
        ));
//        setUniform(new GLUniformData(
//                name + ".hasDisplacementMap",
//                material.getDisplacementMap() != null ? 1 : 0
//        ));
    }

    public void setUniform(final String name,
                           final PointLight pointLight,
                           final int pos) {
        setUniform(name + "[" + pos + "]", pointLight);
    }

    public void setUniform(final String name,
                           final SpotLight spotLight,
                           final int pos) {
        setUniform(name + "[" + pos + "]", spotLight);
    }

    public void setUniform(final String name,
                           final SpotLight spotLight) {
        setUniform(name + ".pl", spotLight.getPointLight());
        setUniform(UniformUtils.from(
                name + ".coneDir",
                spotLight.getConeDirection()
        ));
        setUniform(new GLUniformData(
                name + ".cutoff",
                spotLight.getCutOff()
        ));
    }

    public void setUniform(final String name,
                           final PointLight pointLight) {
        setUniform(UniformUtils.from(name + ".colour", pointLight.getColor()));
        setUniform(UniformUtils.from(name + ".position", pointLight.getPosition()));
        setUniform(new GLUniformData(name + ".intensity", pointLight.getIntensity()));
        final Attenuation att = pointLight.getAttenuation();
        setUniform(new GLUniformData(name + ".att.constant", att.getConstant()));
        setUniform(new GLUniformData(name + ".att.linear", att.getLinear()));
        setUniform(new GLUniformData(name + ".att.exponent", att.getExponent()));
    }

    public void setUniform(final String name,
                           final DirectionalLight dirLight) {
        setUniform(UniformUtils.from(name + ".colour", dirLight.getColor()));
        setUniform(UniformUtils.from(name + ".direction", dirLight.getDirection()));
        setUniform(new GLUniformData(name + ".intensity", dirLight.getIntensity()));
    }

    public void setUniform(final String name,
                           final Fog fog) {
        setUniform(new GLUniformData(name + ".isActive", fog.isActive() ? 1 : 0));
        setUniform(UniformUtils.from(name + ".colour", fog.getColour()));
        setUniform(new GLUniformData(name + ".density", fog.getDensity()));
    }

    public void createUniform(final String name) {
        final int uniformLocation = this.gl.glGetUniformLocation(super.program(), name);
        if (uniformLocation < 0) {
            throw new GLException("Could not find uniform: " + name);
        }
    }

    public void createUniform(final String name,
                              final int size) {
        for (int i = 0; i < size; i++) {
            createUniform(name + "[" + i + "]");
        }
    }

    public void createMaterialUniform(final String name) {
        createUniform(name + ".diffuse");
        createUniform(name + ".specular");
        createUniform(name + ".hasTexture");
        createUniform(name + ".hasNormalMap");
        createUniform(name + ".reflectance");
    }

    public void createPointLightListUniform(final String name,
                                            final int size) {
        for (int i = 0; i < size; i++) {
            createPointLightUniform(name + "[" + i + "]");
        }
    }

    public void createPointLightUniform(final String name) {
        createUniform(name + ".colour");
        createUniform(name + ".position");
        createUniform(name + ".intensity");
        createUniform(name + ".att.constant");
        createUniform(name + ".att.linear");
        createUniform(name + ".att.exponent");
    }

    public void createSpotLightListUniform(final String name,
                                           final int size) {
        for (int i = 0; i < size; i++) {
            createSpotLightUniform(name + "[" + i + "]");
        }
    }

    public void createSpotLightUniform(final String name) {
        createPointLightUniform(name + ".pl");
        createUniform(name + ".coneDir");
        createUniform(name + ".cutoff");
    }

    public void createDirectionalLightUniform(final String name) {
        createUniform(name + ".colour");
        createUniform(name + ".direction");
        createUniform(name + ".intensity");
    }

    public void createFogUniform(final String name) {
        createUniform(name + ".isActive");
        createUniform(name + ".colour");
        createUniform(name + ".density");
    }

    @Override
    public void bind() {
        super.useProgram(this.gl, true);
    }

    @Override
    public void unbind() {
        super.useProgram(this.gl, false);
    }

    @Override
    public void destroy() {
        Shader.LOGGER.trace("[Shader: {}] Releasing and destroying shader", super.program());
        super.release(this.gl);
        super.destroy(this.gl);
    }

    public boolean isValid() {
        return this.valid;
    }
}
