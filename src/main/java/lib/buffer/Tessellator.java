package lib.buffer;

import lib.vertex.VertexFormat;

import java.util.function.Consumer;

public enum Tessellator {
    DEFAULT(0x200000);

    private final BufferUploader uploader = new BufferUploader();
    private final BufferBuilder buffer;

    Tessellator(int capacity) {
        buffer = new BufferBuilder(capacity);
    }

    public void draw(int mode, VertexFormat format, Consumer<BufferBuilder> builder) {
        buffer.begin(mode, format);
        builder.accept(buffer);
        buffer.finishDrawing();
        uploader.draw(buffer);
    }

    public void draw() {
        buffer.finishDrawing();
        uploader.draw(buffer);
    }

    public BufferBuilder getBuffer() {
        return this.buffer;
    }
}
