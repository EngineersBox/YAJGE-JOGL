package com.engineersbox.yajgejogl.resources.loader;

import com.engineersbox.yajgejogl.resources.assets.cache.AssetCache;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;
import java.util.Optional;

public class TextureLoader {

    private static final AssetCache<String, Texture> TEXTURE_CACHE = new AssetCache<>(20);

    private static final Logger LOGGER = LogManager.getLogger(TextureLoader.class);

    private TextureLoader() {
        throw new IllegalStateException("Utility class");
    }

    public static Texture load(final GL2 gl,
                               final String path) {
        final Optional<Texture> cachedTexture = TextureLoader.TEXTURE_CACHE.query(path);
        if (cachedTexture.isPresent()) {
            TextureLoader.LOGGER.trace("[TEXTURE CACHE] Entry found, returning cached entry");
            return cachedTexture.get();
        }
        try (final InputStream stream = ResourceLoader.loadResourceAsStream(path)) {
            final BufferedImage image = ImageIO.read(stream);
            final Texture texture = AWTTextureIO.newTexture(gl.getGLProfile(), image, false);
            TextureLoader.LOGGER.trace("[TEXTURE CACHE] No entry found, requesting cache population for \"{}\"", path);
            TextureLoader.TEXTURE_CACHE.request(path, texture);
            return texture;
        } catch (final IOException e) {
            TextureLoader.LOGGER.error(e);
            return null;
        }
    }

    public static BufferedImage loadBufferedImage(final String path) {
        try (final InputStream stream = ResourceLoader.loadResourceAsStream(path)) {
            return ImageIO.read(stream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteBuffer loadBuffered(final String path) {
        try {
            return convertImageData(loadBufferedImage(path));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ComponentColorModel glAlphaColorModel = new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            new int[]{8, 8, 8, 8},
            true,
            false,
            ComponentColorModel.TRANSLUCENT,
            DataBuffer.TYPE_BYTE
    );
    private static final ComponentColorModel glColorModel = new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            new int[]{8, 8, 8, 0},
            false,
            false,
            ComponentColorModel.OPAQUE,
            DataBuffer.TYPE_BYTE
    );

    public static ByteBuffer convertImageData(final BufferedImage bufferedImage) throws IOException {
        try {
            int width = 2;
            int height = 2;
            for (; width < bufferedImage.getWidth(); width *= 2);
            for (; height < bufferedImage.getHeight(); height *= 2);
            final WritableRaster raster;
            final BufferedImage texImage;
            if (bufferedImage.getColorModel().hasAlpha()) {
                raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 4, null);
                texImage = new BufferedImage(TextureLoader.glAlphaColorModel, raster, false, new Hashtable<>());
            } else {
                raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 3, null);
                texImage = new BufferedImage(TextureLoader.glColorModel, raster, false, new Hashtable<>());
            }
            texImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
            final byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();
            final ByteBuffer imageBuffer = Buffers.newDirectByteBuffer(data.length);
            imageBuffer.order(ByteOrder.nativeOrder());
            imageBuffer.put(data, 0, data.length);
            return imageBuffer.flip();
        } catch (final Exception e) {
            throw new IOException("Unable to convert data  for texture " + e);
        }
    }

}
