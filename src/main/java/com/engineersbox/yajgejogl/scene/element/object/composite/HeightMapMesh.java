package com.engineersbox.yajgejogl.scene.element.object.composite;

import com.engineersbox.yajgejogl.resources.assets.material.Material;
import com.engineersbox.yajgejogl.util.ListUtils;
import com.jogamp.opengl.GL2;
import com.engineersbox.yajgejogl.resources.assets.material.Texture;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HeightMapMesh {

    private static final int MAX_CHANNEL_VALUE = 255;
    private static final int MAX_COLOUR = HeightMapMesh.MAX_CHANNEL_VALUE * HeightMapMesh.MAX_CHANNEL_VALUE * HeightMapMesh.MAX_CHANNEL_VALUE;
    public static final float STARTX = -0.5f;
    public static final float STARTZ = -0.5f;

    private final float minY;
    private final float maxY;
    private final Mesh mesh;
    private final float[][] heightArray;
    private final GL2 gl;

    public HeightMapMesh(final GL2 gl,
                         final float minY,
                         final float maxY,
                         final BufferedImage heightMapImage,
                         final int width,
                         final int height,
                         final String textureFile,
                         final int texInc) {
        this.gl = gl;
        this.minY = minY;
        this.maxY = maxY;
        this.heightArray = new float[height][width];
        final Texture texture = new Texture(gl, textureFile);
        this.mesh = buildMesh(width, height, heightMapImage, texInc);
        this.mesh.setBoundingRadius(Math.max(width, height) * 0.5f);
        this.mesh.setMaterial(new Material(texture, 0.0f));
    }

    private Mesh buildMesh(final int width,
                           final int height,
                           final BufferedImage heightMapImage,
                           final int texInc) {
        final float incX = HeightMapMesh.getXLength() / (width - 1);
        final float incZ = HeightMapMesh.getZLength() / (height - 1);
        final List<Float> positions = new ArrayList<>();
        final List<Float> texCoords = new ArrayList<>();
        final List<Integer> indices = new ArrayList<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                positions.add(HeightMapMesh.STARTX + col * incX);
                final float currentHeight = getHeight(col, row, heightMapImage);
                this.heightArray[row][col] = currentHeight;
                positions.add(currentHeight);
                positions.add(HeightMapMesh.STARTZ + row * incZ);

                texCoords.add((float) texInc * (float) col / (float) width);
                texCoords.add((float) texInc * (float) row / (float) height);

                if (col < width - 1 && row < height - 1) {
                    final int leftTop = row * width + col;
                    final int leftBottom = (row + 1) * width + col;
                    final int rightBottom = (row + 1) * width + col + 1;
                    final int rightTop = row * width + col + 1;

                    indices.add(leftTop);
                    indices.add(leftBottom);
                    indices.add(rightTop);

                    indices.add(rightTop);
                    indices.add(leftBottom);
                    indices.add(rightBottom);
                }
            }
        }
        final float[] posArr = ListUtils.floatListToArray(positions);
        return new Mesh(
                this.gl,
                posArr,
                ListUtils.floatListToArray(texCoords),
                HeightMapMesh.calcNormals(posArr, width, height),
                ListUtils.intListToArray(indices)
        );
    }

    public Mesh getMesh() {
        return this.mesh;
    }

    public float getHeight(final int row, final int col) {
        if ((row >= 0 && row < this.heightArray.length)
                && (col >= 0 && col < this.heightArray[row].length)) {
            return this.heightArray[row][col];
        }
        return 0;
    }

    public static float getXLength() {
        return Math.abs(-HeightMapMesh.STARTX * 2);
    }

    public static float getZLength() {
        return Math.abs(-HeightMapMesh.STARTZ * 2);
    }

    private static float[] calcNormals(final float[] posArr,
                                       final int width,
                                       final int height) {
        final Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        Vector3f v4 = new Vector3f();
        final Vector3f v12 = new Vector3f();
        final Vector3f v23 = new Vector3f();
        final Vector3f v34 = new Vector3f();
        final Vector3f v41 = new Vector3f();
        final List<Float> normals = new ArrayList<>();
        Vector3f normal = new Vector3f();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (row > 0 && row < height - 1 && col > 0 && col < width - 1) {
                    HeightMapMesh.assignVertexPosition(v0, row, col, width, posArr);
                    HeightMapMesh.assignVertexPosition(v1, row, col - 1, width, posArr);
                    v1 = v1.sub(v0);
                    HeightMapMesh.assignVertexPosition(v2, row + 1, col, width, posArr);
                    v2 = v2.sub(v0);
                    HeightMapMesh.assignVertexPosition(v3, row, col + 1, width, posArr);
                    v3 = v3.sub(v0);
                    HeightMapMesh.assignVertexPosition(v4, row - 1, col, width, posArr);
                    v4 = v4.sub(v0);

                    HeightMapMesh.crossNorm(v1, v2, v12);
                    HeightMapMesh.crossNorm(v2, v3, v23);
                    HeightMapMesh.crossNorm(v3, v4, v34);
                    HeightMapMesh.crossNorm(v4, v1, v41);

                    normal = v12.add(v23).add(v34).add(v41);
                    normal.normalize();
                } else {
                    normal.x = 0;
                    normal.y = 1;
                    normal.z = 0;
                }
                normal.normalize();
                normals.add(normal.x);
                normals.add(normal.y);
                normals.add(normal.z);
            }
        }
        return ListUtils.floatListToArray(normals);
    }

    private static void assignVertexPosition(final Vector3f vec,
                                             final int row,
                                             final int col,
                                             final int width,
                                             final float[] posArr) {
        final int idx = row * width * 3 + col * 3;
        vec.x = posArr[idx];
        vec.y = posArr[idx + 1];
        vec.z = posArr[idx + 2];
    }

    private static void crossNorm(final Vector3f a,
                                  final Vector3f b,
                                  final Vector3f c) {
        a.cross(b, c);
        c.normalize();
    }

    private float getHeight(final int x,
                            final int z,
                            final BufferedImage buffer) {
        return this.minY
                + Math.abs(this.maxY - this.minY)
                * ((float) buffer.getRGB(x, z) / (float) HeightMapMesh.MAX_COLOUR);
    }

    public float positionHeight(final int x,
                              final int z) {
        if (x < 0 || x > width()) {
            throw new ArrayIndexOutOfBoundsException("X value is not in range");
        } else if (z < 0 || z > width()) {
            throw new ArrayIndexOutOfBoundsException("X value is not in range");
        }
        return this.heightArray[z][x];
    }

    public int width() {
        return this.heightArray[0].length;
    }

    public int height() {
        return this.heightArray.length;
    }
}
