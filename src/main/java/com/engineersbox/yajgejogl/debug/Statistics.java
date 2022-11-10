package com.engineersbox.yajgejogl.debug;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.GLBuffers;

import java.nio.IntBuffer;

public class Statistics {

    public enum Stat {
        VERTICES_SUBMITTED(0, GL2.GL_VERTICES_SUBMITTED_ARB),
        TRIANGLES_SUBMITTED(-1, 0, true, Stat.VERTICES_SUBMITTED, 0.3f),
        PRIMITIVES_SUBMITTED(1, GL2.GL_PRIMITIVES_SUBMITTED_ARB),
        VERTEX_SHADER_INVOCATIONS(2, GL2.GL_VERTEX_SHADER_INVOCATIONS_ARB),
        TESS_CONTROL_SHADER_PATCHES(3, GL2.GL_TESS_CONTROL_SHADER_PATCHES_ARB),
        TESS_EVALUATION_SHADER_INVOCATIONS(4, GL2.GL_TESS_EVALUATION_SHADER_INVOCATIONS_ARB),
        GEOMETRY_SHADER_INVOCATIONS(5, GL3.GL_GEOMETRY_SHADER_INVOCATIONS),
        GEOMETRY_SHADER_PRIMITIVES_EMITTED(6, GL2.GL_GEOMETRY_SHADER_PRIMITIVES_EMITTED_ARB),
        FRAGMENT_SHADER_INVOCATIONS(7, GL2.GL_FRAGMENT_SHADER_INVOCATIONS_ARB),
        COMPUTE_SHADER_INVOCATIONS(8, GL2.GL_COMPUTE_SHADER_INVOCATIONS_ARB),
        CLIPPING_INPUT_PRIMITIVES(9, GL2.GL_CLIPPING_INPUT_PRIMITIVES_ARB),
        CLIPPING_OUTPUT_PRIMITIVES(10, GL2.GL_CLIPPING_OUTPUT_PRIMITIVES_ARB);

        private int index;
        private int target;
        private boolean composite;
        private Stat ref;
        private float factor;

        Stat(final int index,
             final int target) {
            this(index, target, false, null, 0);
        }

        Stat(final int index,
             final int target,
             final boolean composite,
             final Stat ref,
             final float factor) {
            this.index = index;
            this.target = target;
            this.composite = composite;
            this.ref = ref;
            this.factor = factor;
        }
    }
    public static final int MAX = 11;

    private static final String QUERY_EXTENSION_ARB = "GL_ARB_pipeline_statistics_query";

    private final boolean extensionAvailable;
    private final GL2 gl;
    private final IntBuffer queryName;
    private IntBuffer queryResult;
    private boolean running;

    public Statistics(final GL2 gl) {
        this.gl = gl;
        this.extensionAvailable = gl.isExtensionAvailable(Statistics.QUERY_EXTENSION_ARB);
        this.queryName = GLBuffers.newDirectIntBuffer(Statistics.MAX);
        this.running = false;
    }

    public void init() {
        if (!this.extensionAvailable) {
            return;
        }
        this.gl.glGenQueries(Statistics.MAX, this.queryName);

        final int[] queryCounterBits = new int[Statistics.MAX];

        for (final Stat stat : Stat.values()) {
            if (stat.composite) {
                 continue;
            }
            this.gl.glGetQueryiv(
                    stat.target,
                    GL2.GL_QUERY_COUNTER_BITS,
                    queryCounterBits,
                    stat.index
            );
        }

        boolean validated = true;
        for (int i = 0; i < queryCounterBits.length; ++i) {
            validated = validated && queryCounterBits[i] >= 18;
        }

        if (!validated) {
            throw new GLException("Unable to initialise statistics queries");
        }
    }

    public void begin() {
        if (this.running) {
            throw new IllegalStateException("Statistics query already running");
        }
        for (final Stat stat : Stat.values()) {
            if (stat.composite) {
                continue;
            }
            this.gl.glBeginQuery(
                    stat.target,
                    this.queryName.get(stat.index)
            );
        }
        this.running = true;
    }

    public void end() {
        if (!this.running) {
            throw new IllegalStateException("Statistics query is not running");
        }
        this.queryResult = GLBuffers.newDirectIntBuffer(Statistics.MAX);
        for (final Stat stat : Stat.values()) {
            if (stat.composite) {
                continue;
            }
            this.gl.glEndQuery(stat.target);
            this.gl.glGetQueryObjectuiv(
                    queryName.get(stat.index),
                    GL2.GL_QUERY_RESULT,
                    this.queryResult
            );
        }
        this.running = false;
    }

    public int getResult(final Stat statistic) {
        final int result;
        if (statistic.composite) {
            result = (int) (this.queryResult.get(statistic.ref.index) * statistic.factor);
        } else {
            result = this.queryResult.get(statistic.index);
        }
        return result;
    }

}
