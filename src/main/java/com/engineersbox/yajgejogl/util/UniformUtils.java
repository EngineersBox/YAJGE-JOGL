package com.engineersbox.yajgejogl.util;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLUniformData;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;

public class UniformUtils {

    public UniformUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static void validateUniformValue(final Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Uniform value cannot be null");
        }
    }

    public static GLUniformData from(final String name,
                                     final Matrix4f mat) {
        validateUniformValue(mat);
        return new GLUniformData(
                name,
                4, 4,
                mat.get(Buffers.newDirectFloatBuffer(16))
        );
    }

    public static GLUniformData from(final String name,
                                     final Matrix4f mat,
                                     final int index) {
        validateUniformValue(mat);
        return from(name + "[" + index + "]", mat);
    }

    public static GLUniformData from(final String name,
                                     final Matrix4f[] matrices) {
        validateUniformValue(matrices);
        final int length = matrices.length;
        final FloatBuffer fb = Buffers.newDirectFloatBuffer(16 * length);
        for (int i = 0; i < length; i++) {
            matrices[i].get(16 * i, fb);
        }
        return new GLUniformData(
                name,
                16 * length,
                fb
        );
    }

    public static GLUniformData from(final String name,
                                     final Vector2f vec) {
        validateUniformValue(vec);
        return new GLUniformData(
                name,
                2,
                vec.get(Buffers.newDirectFloatBuffer(2))
        );
    }

    public static GLUniformData from(final String name,
                                     final float v0,
                                     final float v1) {
        return from(
                name,
                new Vector2f(v0, v1)
        );
    }

    public static GLUniformData from(final String name,
                                     final Vector3f vec) {
        validateUniformValue(vec);
        return new GLUniformData(
                name,
                3,
                vec.get(Buffers.newDirectFloatBuffer(3))
        );
    }

    public static GLUniformData from(final String name,
                                     final float v0,
                                     final float v1,
                                     final float v2) {
        return from(
                name,
                new Vector3f(v0, v1, v2)
        );
    }

    public static GLUniformData from(final String name,
                                     final Vector4f vec) {
        validateUniformValue(vec);
        return new GLUniformData(
                name,
                4,
                vec.get(Buffers.newDirectFloatBuffer(4))
        );
    }

    public static GLUniformData from(final String name,
                                     final float v0,
                                     final float v1,
                                     final float v2,
                                     final float v3) {
        return from(
                name,
                new Vector4f(v0, v1, v2, v3)
        );
    }

    public static GLUniformData from(final String name,
                                     final boolean condition) {
        return new GLUniformData(
                name,
                condition ? 1 : 0
        );
    }
}
