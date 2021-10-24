package lib.vertex;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.util.logging.Logger;

public class VertexFormatElement {
    private static final Logger LOGGER = Logger.getLogger("VertexFormatElement");
    private final ElementType type;
    private final ElementUsage usage;
    private final int index;
    private final int elementCount;

    public VertexFormatElement(int indexIn, ElementType typeIn, ElementUsage usageIn, int count) {
        if (this.isFirstOrUV(indexIn, usageIn)) {
            this.usage = usageIn;
        } else {
            LOGGER.warning("Multiple vertex elements of the same type other than UVs are not supported. Forcing type to UV.");
            this.usage = ElementUsage.UV;
        }

        this.type = typeIn;
        this.index = indexIn;
        this.elementCount = count;
    }

    private boolean isFirstOrUV(int p_177372_1_, ElementUsage p_177372_2_) {
        return p_177372_1_ == 0 || p_177372_2_ == ElementUsage.UV;
    }

    public final ElementType getType() {
        return this.type;
    }

    public final ElementUsage getUsage() {
        return this.usage;
    }

    public final int getElementCount() {
        return this.elementCount;
    }

    public final int getIndex() {
        return this.index;
    }

    public String toString() {
        return this.elementCount + "," + this.usage.getDisplayName() + "," + this.type.getDisplayName();
    }

    public final int getSize() {
        return this.type.getSize() * this.elementCount;
    }

    public final boolean isPositionElement() {
        return this.usage == ElementUsage.POSITION;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            VertexFormatElement vertexformatelement = (VertexFormatElement) other;

            if (this.elementCount != vertexformatelement.elementCount) {
                return false;
            } else if (this.index != vertexformatelement.index) {
                return false;
            } else if (this.type != vertexformatelement.type) {
                return false;
            } else {
                return this.usage == vertexformatelement.usage;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int i = this.type.hashCode();
        i = 31 * i + this.usage.hashCode();
        i = 31 * i + this.index;
        i = 31 * i + this.elementCount;
        return i;
    }

    public enum ElementType {
        FLOAT(4, "Float", GL11.GL_FLOAT),
        UNSIGNED_BYTE(1, "Unsigned Byte", GL11.GL_UNSIGNED_BYTE),
        BYTE(1, "Byte", GL11.GL_BYTE),
        UNSIGNED_SHORT(2, "Unsigned Short", GL11.GL_UNSIGNED_SHORT),
        SHORT(2, "Short", GL11.GL_SHORT),
        UNSIGNED_INT(4, "Unsigned Int", GL11.GL_UNSIGNED_INT),
        INT(4, "Int", GL11.GL_INT);

        private final int size;
        private final String displayName;
        private final int glConstant;

        ElementType(int size, String displayName, int glConstant) {
            this.size = size;
            this.displayName = displayName;
            this.glConstant = glConstant;
        }

        public int getSize() {
            return this.size;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public int getGlConstant() {
            return this.glConstant;
        }
    }

    public enum ElementUsage {
        POSITION("Position"),
        NORMAL("Normal"),
        COLOR("Vertex Color"),
        UV("UV"),
        PADDING("Padding"),
        GENERIC("Generic");

        private final String displayName;

        ElementUsage(String displayName) {
            this.displayName = displayName;
        }

        public void preDraw(VertexFormat format, int element, int stride, java.nio.ByteBuffer buffer) {
            VertexFormatElement attr = format.getElement(element);
            int count = attr.getElementCount();
            int constant = attr.getType().getGlConstant();
            buffer.position(format.getOffset(element));
            switch (this) {
                case POSITION:
                    GL11.glVertexPointer(count, constant, stride, buffer);
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    break;
                case NORMAL:
                    if (count != 3) {
                        throw new IllegalArgumentException("Normal attribute should have the size 3: " + attr);
                    }
                    GL11.glNormalPointer(constant, stride, buffer);
                    GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);;
                    break;
                case COLOR:
                    GL11.glColorPointer(count, constant, stride, buffer);
                    GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
                    break;
                case UV:
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + attr.getIndex());
                    GL11.glTexCoordPointer(count, constant, stride, buffer);
                    GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    GL13.glActiveTexture(GL13.GL_TEXTURE0);
                    break;
                case PADDING:
                    break;
                case GENERIC:
                    GL20.glEnableVertexAttribArray(attr.getIndex());
                    GL20.glVertexAttribPointer(attr.getIndex(), count, constant, false, stride, buffer);
                    break;
                default:
                    throw new RuntimeException("Unimplemented attribute upload: " + getDisplayName());
            }
        }

        public void postDraw(VertexFormat format, int element, int stride, java.nio.ByteBuffer buffer) {
            VertexFormatElement attr = format.getElement(element);
            switch (this) {
                case POSITION:
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                    break;
                case NORMAL:
                    GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
                    break;
                case COLOR:
                    GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                    break;
                case UV:
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + attr.getIndex());
                    GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    GL13.glActiveTexture(GL13.GL_TEXTURE0);
                    break;
                case PADDING:
                    break;
                case GENERIC:
                    GL20.glDisableVertexAttribArray(attr.getIndex());
                    break;
                default:
                    throw new RuntimeException("Unimplemented attribute upload: " + getDisplayName());
            }
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }
}