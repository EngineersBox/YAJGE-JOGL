package com.engineersbox.yajgejogl;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class GenerateHeightMap {
    final static int x_size = 256;
    final static int y_size = 256;
    int offset;


    private Random random;
    private final int gradientSizeTable = 256;
    private final float[] gradients = new float[this.gradientSizeTable * 3];
    float[][] heights = new float[GenerateHeightMap.y_size][GenerateHeightMap.x_size];

    // Original Ken Perlin Permutation array from <https://en.wikipedia.org/wiki/Perlin_noise>
    static int permutation[] =
            {151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36,
                    103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0,
                    26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56,
                    87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166,
                    77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55,
                    46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132,
                    187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109,
                    198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126,
                    255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183,
                    170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43,
                    172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112,
                    104, 218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162,
                    241, 81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106,
                    157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205,
                    93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180};

    // generate buffered image here for heightmap
    public BufferedImage GenerateHeightMap(final int seed, final int offset, final String outputPath) {
        this.offset = offset;
        this.heights = generatePerlinNoise(generateWhiteNoise(GenerateHeightMap.x_size, GenerateHeightMap.y_size, seed), 8, 0.3f);

        System.out.println(this.heights);
        final BufferedImage output = new BufferedImage(GenerateHeightMap.y_size, GenerateHeightMap.x_size, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < GenerateHeightMap.y_size; ++i) {
            for (int j = 0; j < GenerateHeightMap.x_size; ++j) {
                final float temp = (this.heights[i][j]);
                final Color temp_color = new Color(temp, temp, temp, 1f);
                //Color temp_color = new Color(0.5f, 0.5f, 0.5f, 0.5f);
                output.setRGB(i, j, temp_color.getRGB());
            }
        }
        final File outputfile = new File(outputPath);
        try {
            ImageIO.write(output, "png", outputfile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return output;
    }

    public static float[][] generateWhiteNoise(final int width, final int height, final int seed) {
        final Random random = new Random(seed);
        final float[][] noise = new float[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                noise[i][j] = (float) random.nextDouble() % 1;
            }
        }

        return noise;
    }

    public float[][] GenerateSmoothNoise(final float[][] baseNoise, final int octave) {
        final int width = baseNoise.length;
        final int height = baseNoise[0].length;

        final float[][] smoothNoise = new float[width][height];

        final int samplePeriod = (int) Math.pow(2, octave); // calculates 2 ^ k // wave lenght
        final float sampleFrequency = 1.0f / samplePeriod;

        for (int i = 0; i < width; i++) {
            //calculate the horizontal sampling indices
            final int sample_i0 = (i / samplePeriod) * samplePeriod;
            final int sample_i1 = (sample_i0 + samplePeriod) % width; //wrap around
            final float horizontal_blend = (i - sample_i0) * sampleFrequency;

            for (int j = 0; j < height; j++) {
                //calculate the vertical sampling indices
                final int sample_j0 = (j / samplePeriod) * samplePeriod;
                final int sample_j1 = (sample_j0 + samplePeriod) % height; //wrap around
                final float vertical_blend = (j - sample_j0) * sampleFrequency;

                //blend the top two corners
                final float top = Interpolate(baseNoise[sample_i0][sample_j0],
                        baseNoise[sample_i1][sample_j0], horizontal_blend);

                //blend the bottom two corners
                final float bottom = Interpolate(baseNoise[sample_i0][sample_j1],
                        baseNoise[sample_i1][sample_j1], horizontal_blend);

                //final blend
                smoothNoise[i][j] = Interpolate(top, bottom, vertical_blend);
            }
        }

        return smoothNoise;
    }

    public float[][] generatePerlinNoise(final float[][] baseNoise, final int octaveCount, final float noisePersistance) {
        final int width = baseNoise.length;
        final int height = baseNoise[0].length;

        final float[][][] smoothNoise = new float[octaveCount][][]; //an array of 2D arrays containing

        //generate smooth noise
        for (int i = 0; i < octaveCount; i++) {
            smoothNoise[i] = GenerateSmoothNoise(baseNoise, i);
        }

        final float[][] perlinNoise = new float[width][height];
        float amplitude = 1.0f;
        float totalAmplitude = 0.0f;

        //blend noise together
        for (int octave = octaveCount - 1; octave >= 0; octave--) {
            amplitude *= noisePersistance;
            totalAmplitude += amplitude;

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    perlinNoise[i][j] += smoothNoise[octave][i][j] * amplitude;
                }
            }
        }

        //normalisation
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                perlinNoise[i][j] /= totalAmplitude;
            }
        }

        return perlinNoise;
    }

    private static float Interpolate(final float x0, final float x1, final float alpha) {
        return x0 * (1 - alpha) + alpha * x1;
    }


}
