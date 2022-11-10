package com.engineersbox.yajgejogl.resources.loader;

import com.engineersbox.yajgejogl.resources.assets.cache.AssetCache;
import com.engineersbox.yajgejogl.scene.element.object.composite.InstancedMesh;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import com.engineersbox.yajgejogl.scene.element.object.primitive.obj.Face;
import com.engineersbox.yajgejogl.scene.element.object.primitive.obj.IdxGroup;
import com.engineersbox.yajgejogl.util.ListUtils;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OBJLoader {

    private static final AssetCache<String, Mesh> OBJ_MESH_CACHE = new AssetCache<>(20);
    private static final Logger LOGGER = LogManager.getLogger(OBJLoader.class);

    private OBJLoader() {
        throw new IllegalStateException("Utility class");
    }

    public static Mesh load(final GL2 gl,
                            final String fileName) {
        return OBJLoader.load(gl, fileName, 1);
    }

    public static Mesh load(final GL2 gl,
                            final String fileName,
                            final int instances) {
//        final Optional<Mesh> cachedMesh = OBJLoader.OBJ_MESH_CACHE.query(fileName);
//        if (cachedMesh.isPresent()) {
//            OBJLoader.LOGGER.trace("[OBJ MESH CACHE] Entry found, returning cached entry");
//            return cachedMesh.get();
//        }
        final List<String> lines = ResourceLoader.loadResourceAsStringLines(fileName);

        final List<Vector3f> vertices = new ArrayList<>();
        final List<Vector2f> textures = new ArrayList<>();
        final List<Vector3f> normals = new ArrayList<>();
        final List<Face> faces = new ArrayList<>();

        for (final String line : lines) {
            final String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v" -> {
                    // Geometric vertex
                    final Vector3f vec3f = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    vertices.add(vec3f);
                }
                case "vt" -> {
                    // Texture coordinate
                    final Vector2f vec2f = new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]));
                    textures.add(vec2f);
                }
                case "vn" -> {
                    // Vertex normal
                    final Vector3f vec3fNorm = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    normals.add(vec3fNorm);
                }
                case "f" -> {
                    final Face face = new Face(Arrays.copyOfRange(tokens, 1, tokens.length));
                    faces.add(face);
                }
                default -> {
                }
                // Ignore other lines
            }
        }
        final Mesh mesh = OBJLoader.reorderLists(
                gl,
                vertices,
                textures,
                normals,
                faces,
                instances
        );
//        OBJLoader.LOGGER.trace("[OBJ MESH CACHE] No entry found, requesting cache population for \"{}\"", fileName);
//        OBJLoader.OBJ_MESH_CACHE.request(fileName, mesh);
        return mesh;
    }

    private static Mesh reorderLists(final GL2 gl,
                                     final List<Vector3f> positions,
                                     final List<Vector2f> texCoords,
                                     final List<Vector3f> normal,
                                     final List<Face> faces,
                                     final int instances) {
        final List<Integer> indices = new ArrayList<>();
        final float[] posArr = new float[positions.size() * 3];
        for (int i = 0; i < positions.size(); i++) {
            posArr[i * 3] = positions.get(i).x;
            posArr[i * 3 + 1] = positions.get(i).y;
            posArr[i * 3 + 2] = positions.get(i).z;
        }
        final float[] textCoordArr = new float[positions.size() * 2];
        final float[] normArr = new float[positions.size() * 3];

        for (final Face face : faces) {
            final IdxGroup[] faceVertexIndices = face.getFaceVertexIndices();
            for (final IdxGroup indValue : faceVertexIndices) {
                OBJLoader.processFaceVertex(
                        indValue,
                        texCoords,
                        normal,
                        indices,
                        textCoordArr,
                        normArr
                );
            }
        }
        final int[] indicesArr = ListUtils.intListToArray(indices);
        final Mesh mesh;
        if (instances > 1) {
            mesh = new InstancedMesh(gl, posArr, textCoordArr, normArr, indicesArr, instances);
        } else {
            mesh = new Mesh(gl, posArr, textCoordArr, normArr, indicesArr);
        }
        mesh.setFaceFormat(switch (faces.get(0).getFaceVertexIndices().length) {
            case 3 -> GL2.GL_TRIANGLES;
            case 4 -> GL2.GL_QUADS;
            default -> GL2.GL_POLYGON;
        });
        return mesh;
    }

    private static void processFaceVertex(final IdxGroup indices,
                                          final List<Vector2f> texCoords,
                                          final List<Vector3f> normals,
                                          final List<Integer> indicesList,
                                          final float[] texCoordArr,
                                          final float[] normArr) {
        final int posIndex = indices.idxPos;
        indicesList.add(posIndex);

        if (indices.idxTextCoord >= 0) {
            final Vector2f textCoord = texCoords.get(indices.idxTextCoord);
            texCoordArr[posIndex * 2] = textCoord.x;
            texCoordArr[posIndex * 2 + 1] = 1 - textCoord.y;
        }
        if (indices.idxVecNormal >= 0) {
            final Vector3f vecNorm = normals.get(indices.idxVecNormal);
            normArr[posIndex * 3] = vecNorm.x;
            normArr[posIndex * 3 + 1] = vecNorm.y;
            normArr[posIndex * 3 + 2] = vecNorm.z;
        }
    }

}
