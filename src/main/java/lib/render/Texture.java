package lib.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;

public class Texture {
    private final int id = GL11.glGenTextures();
    public final int width, height;

    public Texture(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void setParameter(int name, int value) {
        glTexParameteri(GL_TEXTURE_2D, name, value);
    }

    public void upload(int width, int height, ByteBuffer data) {
        upload(GL_RGBA8, width, height, GL11.GL_RGBA, data);
    }

    public void upload(int internalFormat, int width, int height, int format, ByteBuffer data) {
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL11.GL_UNSIGNED_BYTE, data);
    }

    public void save(Path path) {
        bind();
        save(path, width, height);
    }

    public static void save(Path path, int width, int height) {
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        int size = width * height;
        IntBuffer output = BufferUtils.createIntBuffer(size);
        int[] data = new int[size];
        glGetTexImage(GL_TEXTURE_2D, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, output);
        output.get(data);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, width, height, data, 0, width);

        try (OutputStream stream = Files.newOutputStream(path)) {
            ImageIO.write(img, "png", stream);
            System.out.println("Exported png to: " + path.toAbsolutePath());
        } catch (IOException exc) {
            throw new RuntimeException("Unable to save texture to file", exc);
        }
    }

    @Override
    protected void finalize() {
        glDeleteTextures(id);
    }

    public static Texture create(int width, int height, ByteBuffer data) {
        Texture texture = new Texture(width, height);
        texture.bind();
        texture.setParameter(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        texture.setParameter(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        texture.setParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        texture.setParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        texture.upload(width, height, data);
        return texture;
    }

    public static Texture load(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer image = STBImage.stbi_load(path, w, h, comp, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture from file: " + path, new Throwable(STBImage.stbi_failure_reason()));
            }
            return create(w.get(), h.get(), image);
        }
    }
}
