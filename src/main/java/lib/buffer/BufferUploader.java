package lib.buffer;

import lib.vertex.VertexFormat;
import lib.vertex.VertexFormatElement;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.List;

public class BufferUploader {
    public void draw(BufferBuilder buffer) {
        if (buffer.getVertexCount() > 0) {
            VertexFormat format = buffer.getVertexFormat();
            int size = format.getByteSize();
            ByteBuffer data = buffer.getByteBuffer();
            List<VertexFormatElement> elements = format.getElements();

            for (int i = 0; i < elements.size(); ++i) {
                VertexFormatElement element = elements.get(i);
                data.position(format.getOffset(i));
                element.getUsage().preDraw(format, i, size, data);
            }

            GL11.glDrawArrays(buffer.getDrawMode(), 0, buffer.getVertexCount());
            int i = 0;

            for (int e = elements.size(); i < e; ++i) {
                VertexFormatElement element = elements.get(i);
                element.getUsage().postDraw(format, i, size, data);
            }
        }

        buffer.reset();
    }
}