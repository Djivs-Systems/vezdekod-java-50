package lib.vertex;

import lib.vertex.VertexFormatElement.ElementType;
import lib.vertex.VertexFormatElement.ElementUsage;

public class DefaultVertexFormats {
    public static final VertexFormatElement POSITION_3F = new VertexFormatElement(0, ElementType.FLOAT, ElementUsage.POSITION, 3);
    public static final VertexFormatElement COLOR_4UB = new VertexFormatElement(0, ElementType.UNSIGNED_BYTE, ElementUsage.COLOR, 4);
    public static final VertexFormatElement TEX_2F = new VertexFormatElement(0, ElementType.FLOAT, ElementUsage.UV, 2);
    public static final VertexFormatElement TEX_2S = new VertexFormatElement(1, ElementType.SHORT, ElementUsage.UV, 2);
    public static final VertexFormatElement NORMAL_3B = new VertexFormatElement(0, ElementType.BYTE, ElementUsage.NORMAL, 3);
    public static final VertexFormatElement PADDING_1B = new VertexFormatElement(0, ElementType.BYTE, ElementUsage.PADDING, 1);

    public static final VertexFormat POSITION = new VertexFormat(POSITION_3F);
    public static final lib.vertex.VertexFormat POSITION_COLOR = new VertexFormat(POSITION_3F, COLOR_4UB);
    public static final VertexFormat POSITION_TEX = new VertexFormat(POSITION_3F, TEX_2F);
    public static final VertexFormat POSITION_NORMAL = new VertexFormat(POSITION_3F, NORMAL_3B, PADDING_1B);
    public static final VertexFormat POSITION_TEX_COLOR = new VertexFormat(POSITION_3F, TEX_2F, COLOR_4UB);
    public static final VertexFormat POSITION_TEX_NORMAL = new VertexFormat(POSITION_3F, TEX_2F, NORMAL_3B, PADDING_1B);
    public static final VertexFormat POSITION_TEX_LIGHTMAP_COLOR = new VertexFormat(POSITION_3F, TEX_2F, TEX_2S, COLOR_4UB);
    public static final VertexFormat POSITION_TEX_COLOR_NORMAL = new VertexFormat(POSITION_3F, TEX_2F, COLOR_4UB, NORMAL_3B, PADDING_1B);
}
