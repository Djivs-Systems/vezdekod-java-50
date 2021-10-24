package lib.buffer;

import lib.math.RenderMaths;
import lib.vertex.VertexFormat;
import lib.vertex.VertexFormatElement;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.BitSet;

public class BufferBuilder {
    private ByteBuffer byteBuffer;
    private IntBuffer rawIntBuffer;
    private FloatBuffer rawFloatBuffer;
    private int vertexCount;
    private VertexFormatElement vertexFormatElement;
    private int vertexFormatIndex;
    private boolean noColor;
    private int drawMode;
    private double xOffset;
    private double yOffset;
    private double zOffset;
    private VertexFormat vertexFormat;
    private boolean isDrawing;

    public BufferBuilder(int capacity) {
        this.byteBuffer = ByteBuffer.allocateDirect(capacity * 4).order(ByteOrder.nativeOrder());
        this.rawIntBuffer = byteBuffer.asIntBuffer();
        this.rawFloatBuffer = byteBuffer.asFloatBuffer();
    }

    private static float getDistanceSq(FloatBuffer buf, float x, float y, float z, int integerSize, int offset) {
        float x0 = buf.get(offset + integerSize * 0 + 0);
        float y0 = buf.get(offset + integerSize * 0 + 1);
        float z0 = buf.get(offset + integerSize * 0 + 2);
        float x1 = buf.get(offset + integerSize * 1 + 0);
        float y1 = buf.get(offset + integerSize * 1 + 1);
        float z1 = buf.get(offset + integerSize * 1 + 2);
        float x2 = buf.get(offset + integerSize * 2 + 0);
        float y2 = buf.get(offset + integerSize * 2 + 1);
        float z2 = buf.get(offset + integerSize * 2 + 2);
        float x3 = buf.get(offset + integerSize * 3 + 0);
        float y3 = buf.get(offset + integerSize * 3 + 1);
        float z3 = buf.get(offset + integerSize * 3 + 2);
        float dx = (x0 + x1 + x2 + x3) * 0.25F - x;
        float dy = (y0 + y1 + y2 + y3) * 0.25F - y;
        float dz = (z0 + z1 + z2 + z3) * 0.25F - z;
        return dx * dx + dy * dy + dz * dz;
    }

    private void growBuffer(int increaseAmount) {
        if (RenderMaths.roundUp(increaseAmount, 4) / 4 > this.rawIntBuffer.remaining() || this.vertexCount * this.vertexFormat.getByteSize() + increaseAmount > this.byteBuffer.capacity()) {
            int oldCapacity = this.byteBuffer.capacity();
            int newCapacity = oldCapacity + RenderMaths.roundUp(increaseAmount, 0x200000);
            System.out.println("Needed to grow BufferBuilder buffer: Old size " + oldCapacity + " bytes, new size " + newCapacity + " bytes.");
            int currentVertex = this.rawIntBuffer.position();
            ByteBuffer replacement = ByteBuffer.allocateDirect(newCapacity).order(ByteOrder.nativeOrder());
            this.byteBuffer.position(0);
            replacement.put(this.byteBuffer);
            replacement.rewind();
            this.byteBuffer = replacement;
            this.rawFloatBuffer = this.byteBuffer.asFloatBuffer().asReadOnlyBuffer();
            this.rawIntBuffer = this.byteBuffer.asIntBuffer();
            this.rawIntBuffer.position(currentVertex);
        }
    }

    public void sortVertexData(float cameraX, float cameraY, float cameraZ) {
        int i = this.vertexCount / 4;
        final float[] distances = new float[i];

        for (int j = 0; j < i; ++j) {
            distances[j] = getDistanceSq(this.rawFloatBuffer, (float) ((double) cameraX + this.xOffset), (float) ((double) cameraY + this.yOffset), (float) ((double) cameraZ + this.zOffset), this.vertexFormat.getIntegerSize(), j * this.vertexFormat.getByteSize());
        }

        Integer[] indexes = new Integer[i];

        for (int k = 0; k < indexes.length; ++k) {
            indexes[k] = k;
        }

        Arrays.sort(indexes, (a, b) -> Float.compare(distances[b], distances[a]));
        BitSet bitset = new BitSet();
        int l = this.vertexFormat.getByteSize();
        int[] aint = new int[l];

        for (int i1 = bitset.nextClearBit(0); i1 < indexes.length; i1 = bitset.nextClearBit(i1 + 1)) {
            int j1 = indexes[i1];

            if (j1 != i1) {
                this.rawIntBuffer.limit(j1 * l + l);
                this.rawIntBuffer.position(j1 * l);
                this.rawIntBuffer.get(aint);
                int k1 = j1;

                for (int l1 = indexes[j1]; k1 != i1; l1 = indexes[l1]) {
                    this.rawIntBuffer.limit(l1 * l + l);
                    this.rawIntBuffer.position(l1 * l);
                    IntBuffer intbuffer = this.rawIntBuffer.slice();
                    this.rawIntBuffer.limit(k1 * l + l);
                    this.rawIntBuffer.position(k1 * l);
                    this.rawIntBuffer.put(intbuffer);
                    bitset.set(k1);
                    k1 = l1;
                }

                this.rawIntBuffer.limit(i1 * l + l);
                this.rawIntBuffer.position(i1 * l);
                this.rawIntBuffer.put(aint);
            }

            bitset.set(i1);
        }
        this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
        this.rawIntBuffer.position(this.getBufferSize());
    }

    public BufferBuilder.State getVertexState() {
        this.rawIntBuffer.rewind();
        int i = this.getBufferSize();
        this.rawIntBuffer.limit(i);
        int[] aint = new int[i];
        this.rawIntBuffer.get(aint);
        this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
        this.rawIntBuffer.position(i);
        return new State(aint, new VertexFormat(this.vertexFormat));
    }

    public void setVertexState(BufferBuilder.State state) {
        this.rawIntBuffer.clear();
        this.growBuffer(state.getRawBuffer().length * 4);
        this.rawIntBuffer.put(state.getRawBuffer());
        this.vertexCount = state.getVertexCount();
        this.vertexFormat = new VertexFormat(state.getVertexFormat());
    }

    private int getBufferSize() {
        return this.vertexCount * this.vertexFormat.getIntegerSize();
    }

    public void reset() {
        this.vertexCount = 0;
        this.vertexFormatElement = null;
        this.vertexFormatIndex = 0;
    }

    public void begin(int glMode, VertexFormat format) {
        if (this.isDrawing) {
            throw new IllegalStateException("Already building!");
        } else {
            this.isDrawing = true;
            this.reset();
            this.drawMode = glMode;
            this.vertexFormat = format;
            this.vertexFormatElement = format.getElement(this.vertexFormatIndex);
            this.noColor = false;
            this.byteBuffer.limit(this.byteBuffer.capacity());
        }
    }

    public BufferBuilder tex(double u, double v) {
        int i = this.vertexCount * this.vertexFormat.getByteSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);

        switch (this.vertexFormatElement.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float) u);
                this.byteBuffer.putFloat(i + 4, (float) v);
                break;
            case UNSIGNED_INT:
            case INT:
                this.byteBuffer.putInt(i, (int) u);
                this.byteBuffer.putInt(i + 4, (int) v);
                break;
            case UNSIGNED_SHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) ((int) v));
                this.byteBuffer.putShort(i + 2, (short) ((int) u));
                break;
            case UNSIGNED_BYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) ((int) v));
                this.byteBuffer.put(i + 1, (byte) ((int) u));
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public BufferBuilder lightmap(int sky, int block) {
        int i = this.vertexCount * this.vertexFormat.getByteSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);

        switch (this.vertexFormatElement.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float) sky);
                this.byteBuffer.putFloat(i + 4, (float) block);
                break;
            case UNSIGNED_INT:
            case INT:
                this.byteBuffer.putInt(i, sky);
                this.byteBuffer.putInt(i + 4, block);
                break;
            case UNSIGNED_SHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) block);
                this.byteBuffer.putShort(i + 2, (short) sky);
                break;
            case UNSIGNED_BYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) block);
                this.byteBuffer.put(i + 1, (byte) sky);
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public void putBrightness4(int vertex0, int vertex1, int vertex2, int vertex3) {
        int i = (this.vertexCount - 4) * this.vertexFormat.getIntegerSize() + this.vertexFormat.getUvOffsetById(1) / 4;
        int j = this.vertexFormat.getByteSize() >> 2;
        this.rawIntBuffer.put(i, vertex0);
        this.rawIntBuffer.put(i + j, vertex1);
        this.rawIntBuffer.put(i + j * 2, vertex2);
        this.rawIntBuffer.put(i + j * 3, vertex3);
    }

    public void putPosition(double x, double y, double z) {
        int i = this.vertexFormat.getIntegerSize();
        int j = (this.vertexCount - 4) * i;

        for (int k = 0; k < 4; ++k) {
            int l = j + k * i;
            int i1 = l + 1;
            int j1 = i1 + 1;
            this.rawIntBuffer.put(l, Float.floatToRawIntBits((float) (x + this.xOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(l))));
            this.rawIntBuffer.put(i1, Float.floatToRawIntBits((float) (y + this.yOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(i1))));
            this.rawIntBuffer.put(j1, Float.floatToRawIntBits((float) (z + this.zOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(j1))));
        }
    }

    public int getColorIndex(int vertexIndex) {
        return ((this.vertexCount - vertexIndex) * this.vertexFormat.getByteSize() + this.vertexFormat.getColorOffset()) / 4;
    }

    public void putColorMultiplier(float red, float green, float blue, int vertexIndex) {
        int i = this.getColorIndex(vertexIndex);
        int j = -1;

        if (!this.noColor) {
            j = this.rawIntBuffer.get(i);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                int k = (int) ((float) (j & 255) * red);
                int l = (int) ((float) (j >> 8 & 255) * green);
                int i1 = (int) ((float) (j >> 16 & 255) * blue);
                j = j & -16777216;
                j = j | i1 << 16 | l << 8 | k;
            } else {
                int j1 = (int) ((float) (j >> 24 & 255) * red);
                int k1 = (int) ((float) (j >> 16 & 255) * green);
                int l1 = (int) ((float) (j >> 8 & 255) * blue);
                j = j & 255;
                j = j | j1 << 24 | k1 << 16 | l1 << 8;
            }
        }

        this.rawIntBuffer.put(i, j);
    }

    private void putColor(int color, int vertex) {
        int index = this.getColorIndex(vertex);
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        int alpha = color >> 24 & 255;
        this.putColorRGBA(index, red, green, blue, alpha);
    }

    public void putColorRGB_F(float red, float green, float blue, int vertexIndex) {
        int i = this.getColorIndex(vertexIndex);
        int r = RenderMaths.clamp((int) (red * 255.0F), 0, 255);
        int g = RenderMaths.clamp((int) (green * 255.0F), 0, 255);
        int b = RenderMaths.clamp((int) (blue * 255.0F), 0, 255);
        this.putColorRGBA(i, r, g, b, 255);
    }

    public void putColorRGBA(int index, int red, int green, int blue, int alpha) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            this.rawIntBuffer.put(index, alpha << 24 | blue << 16 | green << 8 | red);
        } else {
            this.rawIntBuffer.put(index, red << 24 | green << 16 | blue << 8 | alpha);
        }
    }

    public void noColor() {
        this.noColor = true;
    }

    public BufferBuilder color(int color) {
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        int alpha = color >> 24 & 255;
        return color(red, green, blue, alpha == 0 ? 255 : alpha);
    }

    public BufferBuilder color(float red, float green, float blue, float alpha) {
        return this.color((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F), (int) (alpha * 255.0F));
    }

    public BufferBuilder color(int red, int green, int blue, int alpha) {
        if (!this.noColor) {
            int i = this.vertexCount * this.vertexFormat.getByteSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);

            switch (this.vertexFormatElement.getType()) {
                case FLOAT:
                    this.byteBuffer.putFloat(i, (float) red / 255.0F);
                    this.byteBuffer.putFloat(i + 4, (float) green / 255.0F);
                    this.byteBuffer.putFloat(i + 8, (float) blue / 255.0F);
                    this.byteBuffer.putFloat(i + 12, (float) alpha / 255.0F);
                    break;
                case UNSIGNED_INT:
                case INT:
                    this.byteBuffer.putFloat(i, (float) red);
                    this.byteBuffer.putFloat(i + 4, (float) green);
                    this.byteBuffer.putFloat(i + 8, (float) blue);
                    this.byteBuffer.putFloat(i + 12, (float) alpha);
                    break;
                case UNSIGNED_SHORT:
                case SHORT:
                    this.byteBuffer.putShort(i, (short) red);
                    this.byteBuffer.putShort(i + 2, (short) green);
                    this.byteBuffer.putShort(i + 4, (short) blue);
                    this.byteBuffer.putShort(i + 6, (short) alpha);
                    break;
                case UNSIGNED_BYTE:
                case BYTE:

                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                        this.byteBuffer.put(i, (byte) red);
                        this.byteBuffer.put(i + 1, (byte) green);
                        this.byteBuffer.put(i + 2, (byte) blue);
                        this.byteBuffer.put(i + 3, (byte) alpha);
                    } else {
                        this.byteBuffer.put(i, (byte) alpha);
                        this.byteBuffer.put(i + 1, (byte) blue);
                        this.byteBuffer.put(i + 2, (byte) green);
                        this.byteBuffer.put(i + 3, (byte) red);
                    }
            }

            this.nextVertexFormatIndex();
        }
        return this;
    }

    public void addVertexData(int[] data) {
        this.growBuffer(data.length * 4 + this.vertexFormat.getByteSize());//Forge, fix MC-122110
        this.rawIntBuffer.position(this.getBufferSize());
        this.rawIntBuffer.put(data);
        this.vertexCount += data.length / this.vertexFormat.getIntegerSize();
    }

    public void endVertex() {
        ++this.vertexCount;
        this.growBuffer(this.vertexFormat.getByteSize());
    }

    public BufferBuilder pos(double x, double y, double z) {
        int i = this.vertexCount * this.vertexFormat.getByteSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);

        switch (this.vertexFormatElement.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float) (x + this.xOffset));
                this.byteBuffer.putFloat(i + 4, (float) (y + this.yOffset));
                this.byteBuffer.putFloat(i + 8, (float) (z + this.zOffset));
                break;
            case UNSIGNED_INT:
            case INT:
                this.byteBuffer.putInt(i, Float.floatToRawIntBits((float) (x + this.xOffset)));
                this.byteBuffer.putInt(i + 4, Float.floatToRawIntBits((float) (y + this.yOffset)));
                this.byteBuffer.putInt(i + 8, Float.floatToRawIntBits((float) (z + this.zOffset)));
                break;
            case UNSIGNED_SHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) ((int) (x + this.xOffset)));
                this.byteBuffer.putShort(i + 2, (short) ((int) (y + this.yOffset)));
                this.byteBuffer.putShort(i + 4, (short) ((int) (z + this.zOffset)));
                break;
            case UNSIGNED_BYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) ((int) (x + this.xOffset)));
                this.byteBuffer.put(i + 1, (byte) ((int) (y + this.yOffset)));
                this.byteBuffer.put(i + 2, (byte) ((int) (z + this.zOffset)));
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public void putNormal(float x, float y, float z) {
        int nx = (byte) ((int) (x * 127.0F)) & 255;
        int ny = (byte) ((int) (y * 127.0F)) & 255;
        int nz = (byte) ((int) (z * 127.0F)) & 255;
        int l = nx | ny << 8 | nz << 16;
        int i1 = this.vertexFormat.getByteSize() >> 2;
        int j1 = (this.vertexCount - 4) * i1 + this.vertexFormat.getNormalOffset() / 4;
        this.rawIntBuffer.put(j1, l);
        this.rawIntBuffer.put(j1 + i1, l);
        this.rawIntBuffer.put(j1 + i1 * 2, l);
        this.rawIntBuffer.put(j1 + i1 * 3, l);
    }

    private void nextVertexFormatIndex() {
        ++this.vertexFormatIndex;
        this.vertexFormatIndex %= this.vertexFormat.getElementCount();
        this.vertexFormatElement = this.vertexFormat.getElement(this.vertexFormatIndex);

        if (this.vertexFormatElement.getUsage() == VertexFormatElement.ElementUsage.PADDING) {
            this.nextVertexFormatIndex();
        }
    }

    public BufferBuilder normal(float x, float y, float z) {
        int i = this.vertexCount * this.vertexFormat.getByteSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);

        switch (this.vertexFormatElement.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, x);
                this.byteBuffer.putFloat(i + 4, y);
                this.byteBuffer.putFloat(i + 8, z);
                break;
            case UNSIGNED_INT:
            case INT:
                this.byteBuffer.putInt(i, (int) x);
                this.byteBuffer.putInt(i + 4, (int) y);
                this.byteBuffer.putInt(i + 8, (int) z);
                break;
            case UNSIGNED_SHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) ((int) (x * Short.MAX_VALUE) & 65535));
                this.byteBuffer.putShort(i + 2, (short) ((int) (y * Short.MAX_VALUE) & 65535));
                this.byteBuffer.putShort(i + 4, (short) ((int) (z * Short.MAX_VALUE) & 65535));
                break;
            case UNSIGNED_BYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) ((int) (x * 127) & 255));
                this.byteBuffer.put(i + 1, (byte) ((int) (y * 127) & 255));
                this.byteBuffer.put(i + 2, (byte) ((int) (z * 127) & 255));
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public void setTranslation(double x, double y, double z) {
        this.xOffset = x;
        this.yOffset = y;
        this.zOffset = z;
    }

    public void finishDrawing() {
        if (!this.isDrawing) {
            throw new IllegalStateException("Not building!");
        } else {
            this.isDrawing = false;
            this.byteBuffer.position(0);
            this.byteBuffer.limit(this.getBufferSize() * 4);
        }
    }

    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public int getDrawMode() {
        return this.drawMode;
    }

    public void putColor4(int argb) {
        for (int i = 0; i < 4; ++i) {
            this.putColor(argb, i + 1);
        }
    }

    public void putColorRGB(float red, float green, float blue) {
        for (int i = 0; i < 4; ++i) {
            this.putColorRGB_F(red, green, blue, i + 1);
        }
    }

    public boolean isColorDisabled() {
        return this.noColor;
    }

    public void putBulkData(ByteBuffer buffer) {
        growBuffer(buffer.limit() + this.vertexFormat.getByteSize());
        this.byteBuffer.position(this.vertexCount * this.vertexFormat.getByteSize());
        this.byteBuffer.put(buffer);
        this.vertexCount += buffer.limit() / this.vertexFormat.getByteSize();
    }

    public static class State {
        private final int[] stateRawBuffer;
        private final VertexFormat stateVertexFormat;

        public State(int[] buffer, VertexFormat format) {
            this.stateRawBuffer = buffer;
            this.stateVertexFormat = format;
        }

        public int[] getRawBuffer() {
            return this.stateRawBuffer;
        }

        public int getVertexCount() {
            return this.stateRawBuffer.length / this.stateVertexFormat.getIntegerSize();
        }

        public VertexFormat getVertexFormat() {
            return this.stateVertexFormat;
        }
    }
}
