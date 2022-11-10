package com.engineersbox.yajgejogl.scene.element.particles;

import com.engineersbox.yajgejogl.resources.assets.material.Texture;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import org.joml.Vector3f;

public class Particle extends SceneElement {

    private long updateTextureMillis;
    private long currentAnimTimeMillis;
    private Vector3f speed;
    private long ttl;
    private final int animFrames;
    
    public Particle(final Mesh mesh,
                    final Vector3f speed,
                    final long ttl,
                    final long updateTextureMillis) {
        super(mesh);
        this.speed = new Vector3f(speed);
        this.ttl = ttl;
        this.updateTextureMillis = updateTextureMillis;
        this.currentAnimTimeMillis = 0;
        final Texture texture = this.getMesh().getMaterial().getAlbedo();
        this.animFrames = texture.getWidth() * texture.getHeight();
    }

    public Particle(final Particle baseParticle) {
        super(baseParticle.getMesh());
        final Vector3f aux = baseParticle.getPosition();
        setPosition(aux.x, aux.y, aux.z);
        setRotation(baseParticle.getRotation());
        setScale(baseParticle.getScale());
        this.speed = new Vector3f(baseParticle.speed);
        this.ttl = baseParticle.geTtl();
        this.updateTextureMillis = baseParticle.getUpdateTextureMillis();
        this.currentAnimTimeMillis = 0;
        this.animFrames = baseParticle.getAnimFrames();
    }

    public int getAnimFrames() {
        return this.animFrames;
    }

    public Vector3f getSpeed() {
        return this.speed;
    }

    public long getUpdateTextureMillis() {
        return this.updateTextureMillis;
    }

    public void setSpeed(final Vector3f speed) {
        this.speed = speed;
    }

    public long geTtl() {
        return this.ttl;
    }

    public void setTtl(final long ttl) {
        this.ttl = ttl;
    }

    public void setUpdateTextureMills(final long updateTextureMillis) {
        this.updateTextureMillis = updateTextureMillis;
    }

    public long updateTTL(final long elapsedTime) {
        this.ttl -= elapsedTime;
        this.currentAnimTimeMillis += elapsedTime;
        if (this.currentAnimTimeMillis >= this.getUpdateTextureMillis() && this.animFrames > 0) {
            this.currentAnimTimeMillis = 0;
            super.setTextPos((this.getTextPos() + 1) % this.animFrames);
        }
        return this.ttl;
    }
    
}