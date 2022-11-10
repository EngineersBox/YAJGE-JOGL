package com.engineersbox.yajgejogl.scene.element;

import com.engineersbox.yajgejogl.resources.assets.material.Material;
import com.engineersbox.yajgejogl.resources.loader.OBJLoader;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import com.engineersbox.yajgejogl.resources.assets.material.Texture;
import com.jogamp.opengl.GL2;
import org.joml.Vector4f;

public class Skybox extends SceneElement {

    public Skybox(final GL2 gl,
                  final String objModel,
                  final String textureFile) {
        super();
        final Mesh skyBoxMesh = OBJLoader.load(gl, objModel);
        skyBoxMesh.setMaterial(new Material(new Texture(gl, textureFile), 0.0f));
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }

    public Skybox(final GL2 gl,
                  final String objModel,
                  final Vector4f colour)  {
        super();
        final Mesh skyBoxMesh = OBJLoader.load(gl, objModel);
        skyBoxMesh.setMaterial(new Material(colour, 0));
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }
}
