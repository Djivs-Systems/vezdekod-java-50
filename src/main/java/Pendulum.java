import lib.Window;
import lib.render.Texture;
import org.lwjgl.glfw.GLFW;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static lib.math.RenderMaths.format;

public class Pendulum extends Window {
    private final Texture logo = Texture.load("logo.png");

    public Pendulum() {
        super(800, 800, "Pendulum", true, "Cambria Math", 46);
        setIcon("pendulum.png");
    }

    @Override
    protected void onScroll(double dx, double dy) {
        length += dy * 10.0;
    }

    boolean mousePressed;

    @Override
    protected void onMouseButton(int button, int action, int mods) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            mousePressed = action != GLFW.GLFW_RELEASE;
        }
    }

    double originX = width / 2.0;
    double originY = height / 2.0;

    double angle = Math.PI / 40.0;
    double gravity = 1.0;
    double velocity = 0.0;
    double length = 400.0;

    @Override
    protected void onFrame(double elapsed) {
        canvas.drawTexture(logo, width - 64, height - 64, 64, 64, 64, 64);

        if (mousePressed) {
            velocity = 0;
            double dx = cursorX - originX;
            double dy = cursorY - originY;
            length = Math.sqrt(dx * dx + dy * dy);
            angle = Math.atan2(-dy, dx) + Math.PI / 2.0;
        } else {
            double force = gravity * sin(angle);
            double acceleration = -force / length;

            velocity += acceleration;
            angle += velocity;
        }

        double bobX = length * sin(angle) + originX;
        double bobY = length * cos(angle) + originY;

        //velocity *= 0.9999;

        if (mousePressed) {
            canvas.drawCircle(0, originX, originY, length, 1.0);
        }

        canvas.fillCircle(0, originX, originY, 2.0);
        canvas.drawLine(0, originX, originY, bobX, bobY, 2.0);
        canvas.fillCircle(0, bobX, bobY, 22.0);
        canvas.fillCircle(0xFFAA00, bobX, bobY, 20.0);

        drawLabel(0, "§6α = " + format((int) (Math.toDegrees(angle) % 360.0)));
        drawLabel(1, "§6ω = " + format(velocity / elapsed));
        drawLabel(2, "§6v = " + format(velocity * length / elapsed));
    }

    private void drawLabel(int index, String text) {
        canvas.drawText(0, 10, 15 + index * (canvas.font.fontHeight + 5), text, true);
    }

    public static void main(String[] args) {
        new Pendulum().show();
    }
}
