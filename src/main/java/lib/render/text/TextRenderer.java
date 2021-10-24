package lib.render.text;

import lib.render.Canvas;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

public class TextRenderer {
    private final int[] colorCode = new int[32];
    public final int fontHeight;
    protected double posX, posY;
    private int textColor;
    private final StringCache stringCache;

    public TextRenderer(String fontName, int fontSize, boolean antiAlias) {
        fontHeight = fontSize / 2;
        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i >> 0 & 1) * 170 + j;

            if (i == 6) {
                k += 85;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
        this.stringCache = new StringCache(colorCode);
        this.stringCache.setDefaultFont(fontName, fontSize, antiAlias);
    }

    private static boolean isFormatColor(char colorChar) {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }

    private static boolean isFormatSpecial(char formatChar) {
        return formatChar >= 'k' && formatChar <= 'o' || formatChar >= 'K' && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R';
    }

    public static String getFormatFromString(String text) {
        StringBuilder s = new StringBuilder();
        int i = -1;
        int j = text.length();

        while ((i = text.indexOf(167, i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = text.charAt(i + 1);

                if (isFormatColor(c0)) {
                    s = new StringBuilder("\u00a7" + c0);
                } else if (isFormatSpecial(c0)) {
                    s.append("\u00a7").append(c0);
                }
            }
        }

        return s.toString();
    }

    public int drawStringWithShadow(Canvas canvas, String text, double x, double y, int color) {
        return drawString(canvas, text, x, y, color, true);
    }

    public int drawCenteredString(Canvas canvas, String text, double x, double y, int color, boolean dropShadow) {
        return drawString(canvas, text, x - getStringWidth(text) / 2F, y, color, dropShadow);
    }

    public int drawString(Canvas canvas, String text, double x, double y, int color, boolean dropShadow) {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        int i;

        if (dropShadow) {
            i = renderString(canvas, text, x + 1.0, y + 1.0, color, true);
            i = Math.max(i, renderString(canvas, text, x, y, color, false));
        } else {
            i = renderString(canvas, text, x, y, color, false);
        }

        return i;
    }

    private int renderString(Canvas canvas, String text, double x, double y, int color, boolean shadow) {
        if (text != null) {
            if ((color & 0xfc000000) == 0) {
                color |= 0xff000000;
            }
            if (shadow) {
                color = (color & 0xfcfcfc) >> 2 | color & 0xff000000;
            }
            this.posY = y;
            return (int) (this.posX = x + this.stringCache.renderString(canvas, text, x, y, color, shadow));
        } else {
            return 0;
        }
    }

    public int getStringWidth(String text) {
        return this.stringCache.getStringWidth(text);
    }

    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(String text, int width, boolean reverse) {
        return this.stringCache.trimStringToWidth(text, width, reverse);
    }

    private String trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    public void drawSplitString(Canvas canvas, String str, int x, int y, int wrapWidth, int textColor) {
        this.textColor = textColor;
        renderSplitString(canvas, trimStringNewline(str), x, y, wrapWidth, false);
    }

    private void renderSplitString(Canvas canvas, String str, int x, int y, int wrapWidth, boolean addShadow) {
        for (String s : listFormattedStringToWidth(str, wrapWidth)) {
            renderString(canvas, s, x, y, this.textColor, addShadow);
            y += this.fontHeight;
        }
    }

    public int getWordWrappedHeight(String str, int maxLength) {
        return this.fontHeight * this.listFormattedStringToWidth(str, maxLength).size();
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Arrays.asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    String wrapFormattedStringToWidth(String str, int wrapWidth) {
        int i = this.sizeStringToWidth(str, wrapWidth);

        if (str.length() <= i) {
            return str;
        } else {
            String s = str.substring(0, i);
            char c0 = str.charAt(i);
            boolean flag = c0 == ' ' || c0 == '\n';
            String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
            return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
        }
    }

    private int sizeStringToWidth(String str, int wrapWidth) {
        return this.stringCache.sizeStringToWidth(str, wrapWidth);
    }

    public int getColorCode(char character) {
        int i = "0123456789abcdef".indexOf(character);
        return i >= 0 && i < this.colorCode.length ? this.colorCode[i] : -1;
    }
}