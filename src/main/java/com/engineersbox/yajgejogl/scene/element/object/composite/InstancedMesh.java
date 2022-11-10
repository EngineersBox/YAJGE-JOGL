package com.engineersbox.yajgejogl.scene.element.object.composite;

import com.engineersbox.yajgejogl.rendering.view.Transform;
import com.engineersbox.yajgejogl.resources.assets.material.Texture;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.util.ArrayUtils;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class InstancedMesh extends Mesh {

    private static final Logger LOGGER = LogManager.getLogger(InstancedMesh.class);
    private static final int VECTOR4F_SIZE_BYTES = 4;
    private static final int MAT4F_SIZE_FLOATS = 4 * 4;
    private static final int MAT4F_SIZE_BYTES = MAT4F_SIZE_FLOATS;
    private static final int INSTANCE_SIZE_BYTES = MAT4F_SIZE_BYTES + 3;
    private static final int INSTANCE_SIZE_FLOATS = MAT4F_SIZE_FLOATS + 3;

    private final int numInstances;
    private final IntBuffer instanceDataVBO;
    private FloatBuffer instanceDataBuffer;
    private final GL2 gl;

    public InstancedMesh(final GL2 gl,
                         final float[] positions,
                         final float[] texCoords,
                         final float[] normals,
                         final int[] indices,
                         final int numInstances) {
        super(
                gl,
                positions,
                texCoords,
                normals,
                indices,
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0),
                ArrayUtils.createFilledArray(Mesh.MAX_WEIGHTS * positions.length / 3, 0f)
        );
        this.gl = gl;
        this.numInstances = numInstances;
        gl.glBindVertexArray(this.vaoId.get(0));
        this.instanceDataVBO = Buffers.newDirectIntBuffer(1);
        gl.glGenBuffers(1, this.instanceDataVBO);
        this.vboIdList.add(this.instanceDataVBO);
        this.instanceDataBuffer = Buffers.newDirectFloatBuffer(numInstances * INSTANCE_SIZE_FLOATS);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, this.instanceDataVBO.get(0));
        int start = 5;
        int strideStart = 0;

        // Model matrix
        for (int i = 0; i < 4; i++) {
            gl.glVertexAttribPointer(start, 4, GL2.GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            gl.glVertexAttribDivisor(start, 1);
            gl.glEnableVertexAttribArray(start);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        // Texture offsets
        gl.glVertexAttribPointer(start, 2, GL2.GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        gl.glVertexAttribDivisor(start, 1);
        gl.glEnableVertexAttribArray(start);
        strideStart += Buffers.SIZEOF_FLOAT * 2;
        start++;

        // Selected or Scaling (for particles)
        gl.glVertexAttribPointer(start, 1, GL2.GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        gl.glVertexAttribDivisor(start, 1);
        gl.glEnableVertexAttribArray(start);

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        gl.glBindVertexArray(0);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        this.instanceDataBuffer = null;
    }

    public void renderListInstanced(final List<SceneElement> sceneElements,
                                    final Transform transform,
                                    final Matrix4f viewMatrix) {
        renderListInstanced(sceneElements, false, transform, viewMatrix);
    }

    public void renderListInstanced(final List<SceneElement> sceneElements,
                                    final boolean billBoard,
                                    final Transform transform,
                                    final Matrix4f viewMatrix) {
        startRender();
        final int chunkSize = this.numInstances;
        final int length = sceneElements.size();
        for (int i = 0; i < length; i += chunkSize) {
            final int end = Math.min(length, i + chunkSize);
            renderChunkInstanced(
                    sceneElements.subList(i, end),
                    billBoard,
                    transform,
                    viewMatrix
            );
        }
        endRender();
    }

    private void renderChunkInstanced(final List<SceneElement> sceneElements,
                                      final boolean billBoard,
                                      final Transform transform,
                                      final Matrix4f viewMatrix) {
        this.instanceDataBuffer.clear();
        final Texture texture = getMaterial().getAlbedo();
        for (int i = 0; i < sceneElements.size(); i++) {
            final SceneElement sceneElement = sceneElements.get(i);
            final Matrix4f modelMatrix = transform.buildModelMatrix(sceneElement);
            if (viewMatrix != null && billBoard) {
                viewMatrix.transpose3x3(modelMatrix);
            }
            modelMatrix.get(INSTANCE_SIZE_FLOATS * i, this.instanceDataBuffer);
            if (texture != null) {
                final int col = sceneElement.getTextPos() % texture.getCols();
                final int row = sceneElement.getTextPos() / texture.getCols();
                final float textXOffset = (float) col / texture.getCols();
                final float textYOffset = (float) row / texture.getRows();
                final int buffPos = INSTANCE_SIZE_FLOATS * i + MAT4F_SIZE_FLOATS;
                this.instanceDataBuffer.put(buffPos, textXOffset);
                this.instanceDataBuffer.put(buffPos + 1, textYOffset);
            }

            final int buffPos = INSTANCE_SIZE_FLOATS * i + MAT4F_SIZE_FLOATS + 2;
            final float selectedScale = sceneElement.isSelected() ? 1 : 0;
            this.instanceDataBuffer.put(
                    buffPos,
                    billBoard ? sceneElement.getScale() : selectedScale
            );
        }

        this.gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, this.instanceDataVBO.get(0));
        this.gl.glBufferData(
                GL2.GL_ARRAY_BUFFER,
                (long) this.numInstances * INSTANCE_SIZE_FLOATS,
                this.instanceDataBuffer,
                GL2.GL_DYNAMIC_READ
        );
        this.gl.glDrawElementsInstanced(
                GL2.GL_TRIANGLES,
                vertexCount(),
                GL2.GL_UNSIGNED_INT,
                0,
                sceneElements.size()
        );
        this.gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
    }
}
