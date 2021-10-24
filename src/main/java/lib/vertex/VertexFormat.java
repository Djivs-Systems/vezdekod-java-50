package lib.vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class VertexFormat {
    private static final Logger LOGGER = Logger.getLogger("VertexFormat");
    private final List<VertexFormatElement> elements = new ArrayList<>();
    private final List<Integer> offsets = new ArrayList<>();
    private final List<Integer> uvOffsetsById = new ArrayList<>();
    private int size;
    private int colorElementOffset = -1;
    private int normalElementOffset = -1;
    private int hashCode;

    public VertexFormat(VertexFormat format) {
        this();

        for (int i = 0; i < format.getElementCount(); ++i) {
            this.addElement(format.getElement(i));
        }

        this.size = format.getByteSize();
    }

    public VertexFormat() {
    }

    public VertexFormat(VertexFormatElement... elements) {
        for (VertexFormatElement element : elements) {
            addElement(element);
        }
    }

    public void clear() {
        this.elements.clear();
        this.offsets.clear();
        this.colorElementOffset = -1;
        this.uvOffsetsById.clear();
        this.normalElementOffset = -1;
        this.size = 0;
        this.hashCode = 0;
    }

    @SuppressWarnings("incomplete-switch")
    public VertexFormat addElement(VertexFormatElement element) {
        if (element.isPositionElement() && hasPosition()) {
            LOGGER.warning("VertexFormat error: Trying to add a position VertexFormatElement when one already exists, ignoring.");
        } else {
            this.elements.add(element);
            this.offsets.add(this.size);

            switch (element.getUsage()) {
                case NORMAL:
                    this.normalElementOffset = this.size;
                    break;
                case COLOR:
                    this.colorElementOffset = this.size;
                    break;
                case UV:
                    this.uvOffsetsById.add(element.getIndex(), this.size);
            }

            this.size += element.getSize();
            this.hashCode = 0;
        }
        return this;
    }

    public boolean hasNormal() {
        return this.normalElementOffset >= 0;
    }

    public int getNormalOffset() {
        return this.normalElementOffset;
    }

    public boolean hasColor() {
        return this.colorElementOffset >= 0;
    }

    public int getColorOffset() {
        return this.colorElementOffset;
    }

    public boolean hasUvOffset(int id) {
        return this.uvOffsetsById.size() - 1 >= id;
    }

    public int getUvOffsetById(int id) {
        return this.uvOffsetsById.get(id);
    }

    public String toString() {
        StringBuilder s = new StringBuilder("format: " + this.elements.size() + " elements: ");

        for (int i = 0; i < this.elements.size(); ++i) {
            s.append(this.elements.get(i).toString());

            if (i != this.elements.size() - 1) {
                s.append(" ");
            }
        }

        return s.toString();
    }

    private boolean hasPosition() {
        int i = 0;

        for (int j = this.elements.size(); i < j; ++i) {
            VertexFormatElement vertexformatelement = this.elements.get(i);

            if (vertexformatelement.isPositionElement()) {
                return true;
            }
        }

        return false;
    }

    public int getIntegerSize() {
        return this.getByteSize() / 4;
    }

    public int getByteSize() {
        return this.size;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public int getElementCount() {
        return this.elements.size();
    }

    public VertexFormatElement getElement(int index) {
        return this.elements.get(index);
    }

    public int getOffset(int index) {
        return this.offsets.get(index);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            VertexFormat vertexformat = (VertexFormat) other;

            if (this.size != vertexformat.size) {
                return false;
            } else if (!this.elements.equals(vertexformat.elements)) {
                return false;
            } else {
                return this.offsets.equals(vertexformat.offsets);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (this.hashCode != 0) return this.hashCode;
        int i = this.elements.hashCode();
        i = 31 * i + this.offsets.hashCode();
        i = 31 * i + this.size;
        this.hashCode = i;
        return i;
    }
}