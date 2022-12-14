package com.engineersbox.yajgejogl.resources.loader;

import com.engineersbox.yajgejogl.animation.AnimVertex;
import com.engineersbox.yajgejogl.animation.AnimatedFrame;
import com.engineersbox.yajgejogl.resources.assets.material.Material;
import com.engineersbox.yajgejogl.scene.animation.AnimatedSceneElement;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import com.engineersbox.yajgejogl.scene.element.object.md5.anim.MD5AnimModel;
import com.engineersbox.yajgejogl.scene.element.object.md5.frame.MD5BaseFrame;
import com.engineersbox.yajgejogl.scene.element.object.md5.frame.MD5BaseFrameData;
import com.engineersbox.yajgejogl.scene.element.object.md5.frame.MD5Frame;
import com.engineersbox.yajgejogl.scene.element.object.md5.hierarchy.MD5HierarchyData;
import com.engineersbox.yajgejogl.scene.element.object.md5.joint.MD5JointData;
import com.engineersbox.yajgejogl.scene.element.object.md5.model.MD5Model;
import com.engineersbox.yajgejogl.scene.element.object.md5.primitive.MD5Mesh;
import com.engineersbox.yajgejogl.scene.element.object.md5.primitive.MD5Triangle;
import com.engineersbox.yajgejogl.scene.element.object.md5.primitive.MD5Vertex;
import com.engineersbox.yajgejogl.scene.element.object.md5.primitive.MD5Weight;
import com.engineersbox.yajgejogl.util.FileUtils;
import com.engineersbox.yajgejogl.util.ListUtils;
import com.engineersbox.yajgejogl.util.MD5Utils;
import com.jogamp.opengl.GL2;
import com.engineersbox.yajgejogl.resources.assets.material.Texture;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MD5Loader {

    public static AnimatedSceneElement process(final GL2 gl,
                                               final MD5Model md5Model,
                                               final MD5AnimModel animModel,
                                               final Vector4f defaultColour) {
        final List<Matrix4f> invJointMatrices = calcInJointMatrices(md5Model);
        final List<AnimatedFrame> animatedFrames = processAnimationFrames(md5Model, animModel, invJointMatrices);
        final List<Mesh> meshes = new ArrayList<>();
        for (final MD5Mesh md5Mesh : md5Model.getMeshes()) {
            final Mesh mesh = generateMesh(gl, md5Model, md5Mesh);
            handleTexture(gl, mesh, md5Mesh, defaultColour);
            meshes.add(mesh);
        }
        return new AnimatedSceneElement(
                meshes.toArray(Mesh[]::new),
                animatedFrames,
                invJointMatrices
        );
    }

    private static List<Matrix4f> calcInJointMatrices(final MD5Model md5Model) {
        final List<Matrix4f> result = new ArrayList<>();
        final List<MD5JointData> joints = md5Model.getJointInfo().getJoints();
        for (final MD5JointData joint : joints) {
            final Matrix4f mat = new Matrix4f()
                    .translate(joint.getPosition())
                    .rotate(joint.getOrientation())
                    .invert();
            result.add(mat);
        }
        return result;
    }

    private static Mesh generateMesh(final GL2 gl,
                                     final MD5Model md5Model,
                                     final MD5Mesh md5Mesh) {
        final List<AnimVertex> vertices = new ArrayList<>();
        final List<Integer> indices = new ArrayList<>();

        for (final MD5Vertex md5Vertex : md5Mesh.getVertices()) {
            final AnimVertex vertex = new AnimVertex();
            vertices.add(vertex);

            vertex.position = new Vector3f();
            vertex.textCoords = md5Vertex.getTextCoords();

            final int startWeight = md5Vertex.getStartWeight();
            final int numWeights = md5Vertex.getWeightCount();

            vertex.jointIndices = new int[numWeights];
            Arrays.fill(vertex.jointIndices, -1);
            vertex.weights = new float[numWeights];
            Arrays.fill(vertex.weights, -1);
            for (int i = startWeight; i < startWeight + numWeights; i++) {
                final MD5Weight weight = md5Mesh.getWeights().get(i);
                final MD5JointData joint = md5Model.getJointInfo().getJoints().get(weight.getJointIndex());
                final Vector3f rotatedPos = new Vector3f(weight.getPosition()).rotate(joint.getOrientation());
                final Vector3f acumPos = new Vector3f(joint.getPosition()).add(rotatedPos);
                acumPos.mul(weight.getBias());
                vertex.position.add(acumPos);
                vertex.jointIndices[i - startWeight] = weight.getJointIndex();
                vertex.weights[i - startWeight] = weight.getBias();
            }
        }

        for (final MD5Triangle md5Triangle : md5Mesh.getTriangles()) {
            indices.add(md5Triangle.getVertex0());
            indices.add(md5Triangle.getVertex1());
            indices.add(md5Triangle.getVertex2());

            // Normals
            final AnimVertex v0 = vertices.get(md5Triangle.getVertex0());
            final AnimVertex v1 = vertices.get(md5Triangle.getVertex1());
            final AnimVertex v2 = vertices.get(md5Triangle.getVertex2());
            final Vector3f pos0 = v0.position;
            final Vector3f pos1 = v1.position;
            final Vector3f pos2 = v2.position;

            final Vector3f normal = (new Vector3f(pos2).sub(pos0)).cross(new Vector3f(pos1).sub(pos0));

            v0.normal.add(normal);
            v1.normal.add(normal);
            v2.normal.add(normal);
        }

        for (final AnimVertex v : vertices) {
            v.normal.normalize();
        }

        return createMesh(gl, vertices, indices);
    }

    private static List<AnimatedFrame> processAnimationFrames(final MD5Model md5Model, final MD5AnimModel animModel, final List<Matrix4f> invJointMatrices) {
        return animModel.getFrames()
                .stream()
                .map((final MD5Frame frame) -> processAnimationFrame(md5Model, animModel, frame, invJointMatrices))
                .toList();
    }

    private static AnimatedFrame processAnimationFrame(final MD5Model md5Model, final MD5AnimModel animModel, final MD5Frame frame, final List<Matrix4f> invJointMatrices) {
        final AnimatedFrame result = new AnimatedFrame();
        final MD5BaseFrame baseFrame = animModel.getBaseFrame();
        final List<MD5HierarchyData> hierarchyList = animModel.getHierarchy().getHierarchyDataList();
        final List<MD5JointData> joints = md5Model.getJointInfo().getJoints();
        final float[] frameData = frame.getFrameData();
        for (int i = 0; i < joints.size(); i++) {
            final MD5JointData joint = joints.get(i);
            final MD5BaseFrameData baseFrameData = baseFrame.getFrameDataList().get(i);
            final Vector3f position = baseFrameData.getPosition();
            Quaternionf orientation = baseFrameData.getOrientation();

            final int flags = hierarchyList.get(i).getFlags();
            int startIndex = hierarchyList.get(i).getStartIndex();

            if ((flags & 1) > 0) {
                position.x = frameData[startIndex++];
            }
            if ((flags & 2) > 0) {
                position.y = frameData[startIndex++];
            }
            if ((flags & 4) > 0) {
                position.z = frameData[startIndex++];
            }
            if ((flags & 8) > 0) {
                orientation.x = frameData[startIndex++];
            }
            if ((flags & 16) > 0) {
                orientation.y = frameData[startIndex++];
            }
            if ((flags & 32) > 0) {
                orientation.z = frameData[startIndex++];
            }
            orientation = MD5Utils.calculateQuaternion(orientation.x, orientation.y, orientation.z);

            final Matrix4f translateMat = new Matrix4f().translate(position);
            final Matrix4f rotationMat = new Matrix4f().rotate(orientation);
            Matrix4f jointMat = translateMat.mul(rotationMat);
            if (joint.getParentIndex() > -1) {
                final Matrix4f parentMatrix = result.getLocalJointMatrices()[joint.getParentIndex()];
                jointMat = new Matrix4f(parentMatrix).mul(jointMat);
            }

            result.setMatrix(i, jointMat, invJointMatrices.get(i));
        }

        return result;
    }

    private static Mesh createMesh(final GL2 gl,
                                   final List<AnimVertex> vertices,
                                   final List<Integer> indices) {
        final List<Float> positions = new ArrayList<>();
        final List<Float> textCoords = new ArrayList<>();
        final List<Float> normals = new ArrayList<>();
        final List<Integer> jointIndices = new ArrayList<>();
        final List<Float> weights = new ArrayList<>();

        for (final AnimVertex vertex : vertices) {
            positions.add(vertex.position.x);
            positions.add(vertex.position.y);
            positions.add(vertex.position.z);

            textCoords.add(vertex.textCoords.x);
            textCoords.add(vertex.textCoords.y);

            normals.add(vertex.normal.x);
            normals.add(vertex.normal.y);
            normals.add(vertex.normal.z);

            final int numWeights = vertex.weights.length;
            for (int i = 0; i < Mesh.MAX_WEIGHTS; i++) {
                if (i < numWeights) {
                    jointIndices.add(vertex.jointIndices[i]);
                    weights.add(vertex.weights[i]);
                } else {
                    jointIndices.add(-1);
                    weights.add(-1.0f);
                }
            }
        }

        return new Mesh(
                gl,
                ListUtils.floatListToArray(positions),
                ListUtils.floatListToArray(textCoords),
                ListUtils.floatListToArray(normals),
                ListUtils.intListToArray(indices),
                ListUtils.intListToArray(jointIndices),
                ListUtils.floatListToArray(weights)
        );
    }

    private static void handleTexture(final GL2 gl,
                                      final Mesh mesh,
                                      final MD5Mesh md5Mesh,
                                      final Vector4f defaultColour) {
        final String texturePath = md5Mesh.getTexture();
        if (texturePath == null || texturePath.isEmpty()) {
            mesh.setMaterial(new Material(defaultColour, 1));
            return;
        }
        final Texture texture = new Texture(gl, texturePath);
        final Material material = new Material(texture);

        final int pos = texturePath.lastIndexOf(".");
        if (pos > 0) {
            final String basePath = texturePath.substring(0, pos);
            final String extension = texturePath.substring(pos);
            final String normalMapFileName = basePath + "_local" + extension;
            if (FileUtils.fileExists(normalMapFileName)) {
                final Texture normalMap = new Texture(gl, normalMapFileName);
                material.setNormalMap(normalMap);
            }
        }
        mesh.setMaterial(material);
    }
}
