package lib.vertex;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;

public class VertexBuffer {
    private final VertexFormat format;
    private int buffer;
    private int count;

    public VertexBuffer(VertexFormat format) {
        this.format = format;
        this.buffer = GL15.glGenBuffers();
    }

    public VertexFormat getFormat() {
        return format;
    }

    public void bindBuffer() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.buffer);
    }

    public void bufferData(ByteBuffer data) {
        bindBuffer();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, 35044);
        unbindBuffer();
        count = data.limit() / format.getByteSize();
    }

    public void drawArrays(int mode) {
        GL11.glDrawArrays(mode, 0, count);
    }

    public void unbindBuffer() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void deleteGlBuffers() {
        if (buffer >= 0) {
            GL15.glDeleteBuffers(buffer);
            buffer = -1;
        }
    }
}