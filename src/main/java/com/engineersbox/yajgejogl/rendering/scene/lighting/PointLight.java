package com.engineersbox.yajgejogl.rendering.scene.lighting;

import org.joml.Vector3f;

public class PointLight implements LightFixture {

    private Vector3f color;
    private Vector3f position;
    private float intensity;
    private Attenuation attenuation;
    
    public PointLight(final Vector3f color,
                      final Vector3f position,
                      final float intensity) {
        this.attenuation = new Attenuation(1, 0, 0);
        this.color = color;
        this.position = position;
        this.intensity = intensity;
    }

    public PointLight(final Vector3f color,
                      final Vector3f position,
                      final float intensity,
                      final Attenuation attenuation) {
        this(color, position, intensity);
        this.attenuation = attenuation;
    }

    public PointLight(final PointLight pointLight) {
        this(
                new Vector3f(pointLight.getColor()),
                new Vector3f(pointLight.getPosition()),
                pointLight.getIntensity(),
                pointLight.getAttenuation()
        );
    }

    @Override
    public Vector3f getColor() {
        return this.color;
    }

    @Override
    public void setColor(final Vector3f color) {
        this.color = color;
    }

    @Override
    public Vector3f getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(final Vector3f position) {
        this.position = position;
    }

    @Override
    public float getIntensity() {
        return this.intensity;
    }

    @Override
    public void setIntensity(final float intensity) {
        this.intensity = intensity;
    }

    public Attenuation getAttenuation() {
        return this.attenuation;
    }

    public void setAttenuation(final Attenuation attenuation) {
        this.attenuation = attenuation;
    }

}