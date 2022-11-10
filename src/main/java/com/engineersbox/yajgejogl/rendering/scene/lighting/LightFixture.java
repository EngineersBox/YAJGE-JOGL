package com.engineersbox.yajgejogl.rendering.scene.lighting;

import org.joml.Vector3f;

public interface LightFixture {

    Vector3f getColor();
    void setColor(final Vector3f color);
    Vector3f getPosition();
    void setPosition(final Vector3f position);
    float getIntensity();
    void setIntensity(final float intensity);
}
