import lib.Window;
import lib.render.Texture;

import static java.lang.Math.*;
import static lib.math.RenderMaths.format;

public class UnitCircle extends Window {
    private final Texture logo = Texture.load("logo.png");

    public UnitCircle() {
        super(800, 800, "Unit Circle", true, "Cambria Math", 46);
        setIcon("gravity.png");
    }

    double originX = width / 2.0, originY = height / 2.0;

    @Override
    protected void onFrame(double elapsed) {
        double grid = 10.0;
        canvas.drawGrid(0xCCCCCC, 4 * grid);
        canvas.drawAxes(0x000000);

        double radius = 4 * 4 * grid;

        canvas.drawCircle(0xAADDAA, originX, originY, radius, 2.0);

        if (cursorOver) {
            double dx = cursorX - originX;
            double dy = cursorY - originY;
            double length = Math.sqrt(dx * dx + dy * dy);
            double angle = Math.atan2(dy, -dx) + Math.PI;

            double arcX = originX + dx / length * radius;
            double arcY = originY + dy / length * radius;

            canvas.fillCircleSegment(0x66AADDAA, originX, originY, radius, -angle, 0.0);
            canvas.drawLine(0xDD8888, arcX, arcY, originX, arcY, 2.0);
            canvas.drawLine(0x8888DD, arcX, arcY, arcX, originY, 2.0);
            canvas.drawLine(0x88DD88, arcX, arcY, originX, originY, 2.0);
            canvas.fillCircle(0, arcX, arcY, 4.0);

            canvas.drawText(0, 10.0, 15.0, "Angle = " + (int) toDegrees(angle), true);
            canvas.drawText(0, 10.0, 15.0 + canvas.font.fontHeight + 5.0, "§1sin = " + format(sin(angle)), true);
            canvas.drawText(0, 10.0, 15.0 + 2.0 * canvas.font.fontHeight + 10.0, "§4cos = " + format(cos(angle)), true);
            canvas.drawText(0, arcX + 10.0, arcY + 15.0, "(§4" + format(dx / length) + "§0, §1" + format(-dy / length) + "§0)", true);
        }

        canvas.drawTexture(logo, width - 64, height - 64, 64, 64, 64, 64);
    }

    public static void main(String[] args) {
        new UnitCircle().show();
    }
}
