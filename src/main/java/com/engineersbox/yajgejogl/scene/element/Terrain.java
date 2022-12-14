package com.engineersbox.yajgejogl.scene.element;

import com.engineersbox.yajgejogl.resources.loader.ResourceLoader;
import com.engineersbox.yajgejogl.resources.loader.TextureLoader;
import com.engineersbox.yajgejogl.scene.element.object.composite.HeightMapMesh;
import com.engineersbox.yajgejogl.scene.element.object.primitive.Box2D;
import com.jogamp.opengl.GL2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Terrain {

    private final Logger LOGGER = LogManager.getLogger(Terrain.class);

    private final SceneElement[] sceneElements;
    private final int terrainSize;
    private final int verticesPerCol;
    private final int verticesPerRow;

    public HeightMapMesh getHeightMapMesh() {
        return heightMapMesh;
    }

    private final HeightMapMesh heightMapMesh;
    private final Box2D[][] boundingBoxes;

    public Terrain(final GL2 gl,
                   final int terrainSize,
                   final float scale,
                   final float minY,
                   final float maxY,
                   final String heightMap,
                   final String textureFile,
                   final int textInc) {
        this(
                gl,
                terrainSize,
                scale,
                minY,
                maxY,
                TextureLoader.loadBufferedImage(heightMap),
                textureFile,
                textInc
        );
    }

    public Terrain(final GL2 gl,
                   final int terrainSize,
                   final float scale,
                   final float minY,
                   final float maxY,
                   final BufferedImage heightMap,
                   final String textureFile,
                   final int textInc) {
        this.terrainSize = terrainSize;
        this.sceneElements = new SceneElement[terrainSize * terrainSize];

        final BufferedImage image = heightMap;
        final int width = image.getWidth();
        final int height = image.getHeight();

        this.verticesPerCol = width - 1;
        this.verticesPerRow = height - 1;
        this.heightMapMesh = new HeightMapMesh(
                gl,
                minY,
                maxY,
                image,
                width,
                height,
                textureFile,
                textInc
        );
        this.boundingBoxes = new Box2D[terrainSize][terrainSize];
        for (int row = 0; row < terrainSize; row++) {
            for (int col = 0; col < terrainSize; col++) {
                final float xDisplacement = (col - ((float) terrainSize - 1) / (float) 2) * scale * HeightMapMesh.getXLength();
                final float zDisplacement = (row - ((float) terrainSize - 1) / (float) 2) * scale * HeightMapMesh.getZLength();

                final SceneElement terrainChunk = new SceneElement(this.heightMapMesh.getMesh());
                terrainChunk.setScale(scale);
                terrainChunk.setPosition(xDisplacement, 0, zDisplacement);
                this.sceneElements[row * terrainSize + col] = terrainChunk;

                this.boundingBoxes[row][col] = getBoundingBox(terrainChunk);
            }
        }
    }

    public float getHeight(final Vector3f position) {
        Box2D boundingBox = null;
        boolean found = false;
        SceneElement terrainChunk = null;
        for (int row = 0; row < this.terrainSize && !found; row++) {
            for (int col = 0; col < this.terrainSize && !found; col++) {
                terrainChunk = this.sceneElements[row * this.terrainSize + col];
                boundingBox = this.boundingBoxes[row][col];
                found = boundingBox.contains(position.x, position.z);
            }
        }

        if (found) {
            final Vector3f[] triangle = getTriangle(position, boundingBox, terrainChunk);
            return interpolateHeight(triangle[0], triangle[1], triangle[2], position.x, position.z);
        }

        return Float.MIN_VALUE;
    }

    protected Vector3f[] getTriangle(final Vector3f position,
                                     final Box2D boundingBox,
                                     final SceneElement terrainChunk) {
        final float cellWidth = boundingBox.width / (float) this.verticesPerCol;
        final float cellHeight = boundingBox.height / (float) this.verticesPerRow;
        final int col = (int) ((position.x - boundingBox.x) / cellWidth);
        final int row = (int) ((position.z - boundingBox.y) / cellHeight);

        final Vector3f[] triangle = new Vector3f[3];
        triangle[1] = new Vector3f(
                boundingBox.x + col * cellWidth,
                getWorldHeight(row + 1, col, terrainChunk),
                boundingBox.y + (row + 1) * cellHeight
        );
        triangle[2] = new Vector3f(
                boundingBox.x + (col + 1) * cellWidth,
                getWorldHeight(row, col + 1, terrainChunk),
                boundingBox.y + row * cellHeight
        );

        if (position.z < getDiagonalZCoord(triangle[1].x, triangle[1].z, triangle[2].x, triangle[2].z, position.x)) {
            triangle[0] = new Vector3f(
                    boundingBox.x + col * cellWidth,
                    getWorldHeight(row, col, terrainChunk),
                    boundingBox.y + row * cellHeight
            );
        } else {
            triangle[0] = new Vector3f(
                    boundingBox.x + (col + 1) * cellWidth,
                    getWorldHeight(row + 2, col + 1, terrainChunk),
                    boundingBox.y + (row + 1) * cellHeight
            );
        }

        return triangle;
    }

    protected static float getDiagonalZCoord(final float x1,
                                             final float z1,
                                             final float x2,
                                             final float z2,
                                             final float x) {
        return ((z1 - z2) / (x1 - x2)) * (x - x1) + z1;
    }

    protected float getWorldHeight(final int row,
                                   final int col,
                                   final SceneElement gameItem) {
        final float y = this.heightMapMesh.getHeight(row, col);
        return y * gameItem.getScale() + gameItem.getPosition().y;
    }

    protected static float interpolateHeight(final Vector3f pA,
                                             final Vector3f pB,
                                             final Vector3f pC,
                                             final float x,
                                             final float z) {
        final float a = (pB.y - pA.y) * (pC.z - pA.z) - (pC.y - pA.y) * (pB.z - pA.z);
        final float b = (pB.z - pA.z) * (pC.x - pA.x) - (pC.z - pA.z) * (pB.x - pA.x);
        final float c = (pB.x - pA.x) * (pC.y - pA.y) - (pC.x - pA.x) * (pB.y - pA.y);
        final float d = -(a * pA.x + b * pA.y + c * pA.z);
        return (-d - a * x - c * z) / b;
    }

    private static Box2D getBoundingBox(final SceneElement terrainChunk) {
        final float scale = terrainChunk.getScale();
        final Vector3f position = terrainChunk.getPosition();

        final float topLeftX = HeightMapMesh.STARTX * scale + position.x;
        final float topLeftZ = HeightMapMesh.STARTZ * scale + position.z;
        final float width = Math.abs(HeightMapMesh.STARTX * 2) * scale;
        final float height = Math.abs(HeightMapMesh.STARTZ * 2) * scale;
        return new Box2D(topLeftX, topLeftZ, width, height);
    }

    public SceneElement[] getSceneElements() {
        return this.sceneElements;
    }

    public Pair<Vector2f, Vector2f> positions() {
        Vector2f start = null;
        Vector2f end = null;
        for (final Box2D[] row : this.boundingBoxes) {
            for (final Box2D col : row) {
                if (start == null) {
                    start = new Vector2f(col.x, col.y);
                } else {
                    start = new Vector2f(
                            Math.min(col.x, start.x),
                            Math.min(col.y, start.y)
                    );
                }
                if (end == null) {
                    end = new Vector2f(col.x + col.width, col.y + col.height);
                } else {
                    end = new Vector2f(
                            Math.max(col.x + col.width, end.x),
                            Math.max(col.y + col.height, end.y)
                    );
                }
            }
        }
        return ImmutablePair.of(start, end);
    }
}
