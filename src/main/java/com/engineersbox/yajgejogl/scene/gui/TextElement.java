package com.engineersbox.yajgejogl.scene.gui;

import com.engineersbox.yajgejogl.resources.assets.gui.font.CharInfo;
import com.engineersbox.yajgejogl.resources.assets.gui.font.FontTexture;
import com.engineersbox.yajgejogl.resources.assets.material.Material;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import com.engineersbox.yajgejogl.util.ListUtils;
import com.jogamp.opengl.GL2;

import java.util.ArrayList;
import java.util.List;

public class TextElement extends SceneElement {

    private static final float ZPOS = 0.0f;
    private static final int VERTICES_PER_QUAD = 4;

    private final FontTexture fontTexture;
    private String text;
    private final GL2 gl;

    public TextElement(final GL2 gl,
                       final String text,
                       final FontTexture fontTexture) {
        super();
        this.gl = gl;
        this.text = text;
        this.fontTexture = fontTexture;
        super.setMesh(buildMesh());
    }

    private Mesh buildMesh() {
        final List<Float> positions = new ArrayList<>();
        final List<Float> texCoords = new ArrayList<>();
        final float[] normals = new float[0];
        final List<Integer> indices = new ArrayList<>();
        final char[] characters = this.text.toCharArray();
        final int numChars = characters.length;

        float startx = 0;
        for (int i = 0; i < numChars; i++) {
            final CharInfo charInfo = this.fontTexture.getCharInfo(characters[i]);

            // Left Top vertex
            positions.add(startx);
            positions.add(0.0f);
            positions.add(TextElement.ZPOS);
            texCoords.add((float) charInfo.startX() / (float) this.fontTexture.getWidth());
            texCoords.add(0.0f);
            indices.add(i * TextElement.VERTICES_PER_QUAD);

            // Left Bottom vertex
            positions.add(startx);
            positions.add((float) this.fontTexture.getHeight());
            positions.add(TextElement.ZPOS);
            texCoords.add((float) charInfo.startX() / (float) this.fontTexture.getWidth());
            texCoords.add(1.0f);
            indices.add(i * TextElement.VERTICES_PER_QUAD + 1);

            // Right Bottom vertex
            positions.add(startx + charInfo.width());
            positions.add((float) this.fontTexture.getHeight());
            positions.add(TextElement.ZPOS);
            texCoords.add((float) (charInfo.startX() + charInfo.width()) / this.fontTexture.getWidth());
            texCoords.add(1.0f);
            indices.add(i * TextElement.VERTICES_PER_QUAD + 2);

            // Right Top vertex
            positions.add(startx + charInfo.width());
            positions.add(0.0f);
            positions.add(TextElement.ZPOS);
            texCoords.add((float) (charInfo.startX() + charInfo.width()) / this.fontTexture.getWidth());
            texCoords.add(0.0f);
            indices.add(i * TextElement.VERTICES_PER_QUAD + 3);

            // To-left and Bottom-right indicies
            indices.add(i * TextElement.VERTICES_PER_QUAD);
            indices.add(i * TextElement.VERTICES_PER_QUAD + 2);

            startx += charInfo.width();
        }

        final Mesh mesh = new Mesh(
                this.gl,
                ListUtils.floatListToArray(positions),
                ListUtils.floatListToArray(texCoords),
                normals,
                ListUtils.intListToArray(indices)
        );
        mesh.setMaterial(new Material(this.fontTexture.getTexture()));
        return mesh;
    }

    public String getText() {
        return this.text;
    }

    public void setText(final String text) {
        this.text = text;
        this.getMesh().deleteBuffers();
        this.setMesh(buildMesh());
    }
}