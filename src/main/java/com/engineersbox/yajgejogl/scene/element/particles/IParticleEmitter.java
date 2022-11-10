package com.engineersbox.yajgejogl.scene.element.particles;

import com.engineersbox.yajgejogl.scene.element.SceneElement;

import java.util.List;

public interface IParticleEmitter {
    void cleanup();

    Particle getBaseParticle();

    List<SceneElement> getParticles();
}
