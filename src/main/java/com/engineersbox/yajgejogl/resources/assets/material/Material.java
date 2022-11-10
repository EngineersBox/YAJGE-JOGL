package com.engineersbox.yajgejogl.resources.assets.material;

import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import org.joml.Vector4f;

public class Material {

    public static final Vector4f DEFAULT_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    private Vector4f ambientColour;
    private Vector4f diffuseColour;
    private Vector4f specularColour;
    private float reflectance;
    private Texture albedo;
    private Texture normalMap;
//    private Texture displacementMap;
    private Shader shader;

    public Material() {
        this(
                Material.DEFAULT_COLOUR,
                Material.DEFAULT_COLOUR,
                Material.DEFAULT_COLOUR,
                0.0f,
                null,
                null,
//                null,
                null
        );
    }

    public Material(final Texture texture) {
        this(texture, 0);
    }

    public Material(final Vector4f colour, final float reflectance) {
        this(
                colour,
                colour,
                colour,
                reflectance,
                null,
                null,
                null
        );
    }

    public Material(final Texture texture,
                    final float reflectance) {
        this(
                DEFAULT_COLOUR,
                DEFAULT_COLOUR,
                DEFAULT_COLOUR,
                reflectance,
                texture,
                null,
//                null,
                null
        );
    }

    public Material(final Vector4f ambientColour,
                    final Vector4f diffuseColour,
                    final Vector4f specularColour,
                    final float reflectance,
                    final Texture albedo,
                    final Texture normalMap,
//                    final Texture displacementMap,
                    final Shader shader) {
        this.ambientColour = ambientColour;
        this.diffuseColour = diffuseColour;
        this.specularColour = specularColour;
        this.reflectance = reflectance;
        this.albedo = albedo;
        this.normalMap = normalMap;
//        this.displacementMap = displacementMap;
        this.shader = shader;
    }

    public Texture getNormalMap() {
        return this.normalMap;
    }

    public Vector4f getAmbientColour() {
        return ambientColour;
    }

    public void setAmbientColour(final Vector4f ambientColour) {
        this.ambientColour = ambientColour;
    }

    public Vector4f getDiffuseColour() {
        return diffuseColour;
    }

    public void setDiffuseColour(final Vector4f diffuseColour) {
        this.diffuseColour = diffuseColour;
    }

    public Vector4f getSpecularColour() {
        return specularColour;
    }

    public void setSpecularColour(final Vector4f specularColour) {
        this.specularColour = specularColour;
    }

    public float getReflectance() {
        return reflectance;
    }

    public void setReflectance(final float reflectance) {
        this.reflectance = reflectance;
    }

    public Texture getAlbedo() {
        return albedo;
    }

    public void setAlbedo(final Texture albedo) {
        this.albedo = albedo;
    }

    public void setNormalMap(final Texture normalMap) {
        this.normalMap = normalMap;
    }

//    public Texture getDisplacementMap() {
//        return displacementMap;
//    }
//
//    public void setDisplacementMap(final Texture displacementMap) {
//        this.displacementMap = displacementMap;
//    }

    public Shader getShader() {
        return shader;
    }

    public void setShader(final Shader shader) {
        this.shader = shader;
    }
}
