package com.engineersbox.yajgejogl.scene.element.object.composite;

import com.engineersbox.yajgejogl.resources.assets.material.Material;
import com.engineersbox.yajgejogl.resources.assets.material.Texture;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.util.ArrayUtils;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Mesh {

    public static final int MAX_WEIGHTS = 4;

    protected final IntBuffer vaoId;
    protected final List<IntBuffer> vboIdList;
    private final int vertexCount;
    private Material material;
    private float boundingRadius;
    private final GL2 gl;
    private final float[] positions;
    private final float[] texCoords;
    private final float[] normals;
    private final int[] indices;

    public int getFaceFormat() {
        return this.faceFormat;
    }

    public void setFaceFormat(final int format) {
        this.faceFormat = format;
    }

    private int faceFormat;

    public Mesh(final GL2 gl,
                final float[] positions,
                final float[] texCoords,
                final float[] normals,
                final int[] indices) {
        this(
                gl,
                positions,
                texCoords,
                normals,
                indices,
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0),
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0.0f)
        );
    }

    public Mesh(final GL2 gl,
                final float[] positions,
                final float[] texCoords,
                final float[] normals,
                final int[] indices,
                final int[] jointIndices,
                final float[] weights) {
        this.gl = gl;
        this.faceFormat = GL2.GL_TRIANGLES;
        this.positions = positions;
        this.texCoords = texCoords;
        this.normals = normals;
        this.indices = indices;

        this.vertexCount = indices.length;
        this.vboIdList = new ArrayList<>();
        this.vaoId = Buffers.newDirectIntBuffer(1);
        gl.glGenVertexArrays(1, this.vaoId);
        gl.glBindVertexArray(this.vaoId.get(0));

        allocateFloatBuffer(0, 3, positions);
        allocateFloatBuffer(1, 2, texCoords);

        final IntBuffer vboId = Buffers.newDirectIntBuffer(1);
        gl.glGenBuffers(1, vboId);
        this.vboIdList.add(vboId);
        final long size;
        FloatBuffer normalsBuffer = Buffers.newDirectFloatBuffer(normals.length);
        if (normalsBuffer.capacity() > 0) {
            normalsBuffer.put(normals).flip();
            size = (long) normals.length * Buffers.SIZEOF_FLOAT;
        } else {
            normalsBuffer = Buffers.newDirectFloatBuffer(positions.length);
            size = (long) positions.length * Buffers.SIZEOF_FLOAT;
        }
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboId.get(0));
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, size, normalsBuffer, GL2.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL2.GL_FLOAT, false, 0, 0);

        allocateFloatBuffer(3, 4, weights);
        allocateIntBuffer(4, 4, jointIndices);
        allocateIndexBuffer(indices);

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        gl.glBindVertexArray(0);
    }

    private void allocateFloatBuffer(final int index,
                                     final int size,
                                     final float[] values) {
        final IntBuffer vboId = Buffers.newDirectIntBuffer(1);
        this.gl.glGenBuffers(1, vboId);
        this.vboIdList.add(vboId);
        final FloatBuffer buffer = Buffers.newDirectFloatBuffer(values.length);
        buffer.put(values).flip();
        this.gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboId.get(0));
        this.gl.glBufferData(GL2.GL_ARRAY_BUFFER, (long) values.length * Buffers.SIZEOF_FLOAT, buffer, GL2.GL_STATIC_DRAW);
        this.gl.glEnableVertexAttribArray(index);
        this.gl.glVertexAttribPointer(index, size, GL2.GL_FLOAT, false, 0, 0);
    }

    private void allocateIndexBuffer(final int[] indices) {
        final IntBuffer vboId = Buffers.newDirectIntBuffer(1);
        this.gl.glGenBuffers(1, vboId);
        this.vboIdList.add(vboId);
        final IntBuffer indicesBuffer = Buffers.newDirectIntBuffer(indices.length);
        indicesBuffer.put(indices).flip();
        this.gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, vboId.get(0));
        this.gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, (long) indices.length * Buffers.SIZEOF_INT, indicesBuffer, GL2.GL_STATIC_DRAW);
    }

    private void allocateIntBuffer(final int index,
                                   final int size,
                                   final int[] values) {
        allocateIndexBuffer(values);
        this.gl.glEnableVertexAttribArray(index);
        this.gl.glVertexAttribPointer(index, size, GL2.GL_FLOAT, false, 0, 0);
    }

    protected void startRender() {
        final Texture texture = this.material != null ? this.material.getAlbedo() : null;
        if (texture != null) {
            this.gl.glActiveTexture(GL2.GL_TEXTURE0);
            texture.bind(this.gl);
        }
        final Texture normalMap = this.material != null ? this.material.getNormalMap() : null;
        if (normalMap != null) {
            this.gl.glActiveTexture(GL2.GL_TEXTURE1);
            normalMap.bind(this.gl);
        }
        this.gl.glBindVertexArray(getVaoId());
    }

    protected void endRender() {
        this.gl.glBindVertexArray(0);
        this.gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
    }

    public void renderList(final List<SceneElement> sceneElements,
                           final Consumer<SceneElement> consumer) {
        startRender();
        sceneElements.stream()
                .filter((final SceneElement sceneElement) -> (sceneElement.frustumCullingEnabled() && sceneElement.isInsideFrustum()) || !sceneElement.frustumCullingEnabled())
                .forEach((final SceneElement sceneElement) -> {
                    consumer.accept(sceneElement);
                    this.gl.glDrawElements(this.faceFormat, vertexCount(), GL2.GL_UNSIGNED_INT, 0);
                });
        endRender();
    }

    public void render() {
        startRender();
        this.gl.glDrawElements(this.faceFormat, vertexCount(), GL2.GL_UNSIGNED_INT, 0);
        endRender();
    }

    public void cleanUp() {
        this.gl.glDisableVertexAttribArray(0);
        this.gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        for (final IntBuffer vboId : this.vboIdList) {
            this.gl.glDeleteBuffers(1, vboId);
        }
        final Texture texture = this.material.getAlbedo();
        if (texture != null) {
            texture.destroy(this.gl);
        }
        this.gl.glBindVertexArray(0);
        this.gl.glDeleteVertexArrays(1, this.vaoId);
    }

    public void deleteBuffers() {
        this.gl.glDisableVertexAttribArray(0);

        this.gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        for (final IntBuffer vboId : this.vboIdList) {
            this.gl.glDeleteBuffers(1, vboId);
        }

        this.gl.glBindVertexArray(0);
        this.gl.glDeleteVertexArrays(1, this.vaoId);
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(final Material material) {
        this.material = material;
    }

    public int getVaoId() {
        return this.vaoId.get(0);
    }

    public int vertexCount() {
        return this.vertexCount;
    }

    public float getBoundingRadius() {
        return this.boundingRadius;
    }

    public void setBoundingRadius(final float boundingRadius) {
        this.boundingRadius = boundingRadius;
    }

    public List<Vector3f> getGroupedVertices() {
        final List<Vector3f> vertices = new ArrayList<>();
        for (int i = 0; i < this.positions.length; i += 3) {
            vertices.add(new Vector3f(
                    this.positions[i],
                    this.positions[i + 1],
                    this.positions[i + 2]
            ));
        }
        return vertices;
    }

    public int[] getIndices() {
        return this.indices;
    }

    public List<Vector3f> getGroupedNormals() {
        final List<Vector3f> groupedNormals = new ArrayList<>();
        for (int i = 0; i < this.normals.length; i += 3) {
            groupedNormals.add(new Vector3f(
                    this.normals[i],
                    this.normals[i + 1],
                    this.normals[i + 2]
            ));
        }
        return groupedNormals;
    }

    public int triangleCount() {
        return this.indices.length / 3;
    }
}
