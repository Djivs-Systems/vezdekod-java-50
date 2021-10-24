package lib.buffer;

import lib.vertex.VertexBuffer;

public class VertexBufferUploader extends BufferUploader {
    private VertexBuffer vertexBuffer;

    @Override
    public void draw(BufferBuilder buffer) {
        buffer.reset();
        this.vertexBuffer.bufferData(buffer.getByteBuffer());
    }

    public void setVertexBuffer(VertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }
}