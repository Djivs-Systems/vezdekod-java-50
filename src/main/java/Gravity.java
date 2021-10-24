import lib.Window;
import lib.render.Texture;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Gravity extends Window {
    private final Texture logo = Texture.load("logo.png");

    public Gravity() {
        super(800, 800, "Gravity", true, "Cambria Math", 46);
    }

    List<Dust> dusts = new ArrayList<>();

    @Override
    protected void onMouseButton(int button, int action, int mods) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (action == GLFW.GLFW_RELEASE) {
                dusts.add(new Dust(cursorX, cursorY));
            }
        }
    }

    @Override
    protected void onFrame(double elapsed) {
        canvas.drawTexture(logo, width - 64, height - 64, 64, 64, 64, 64);

        for (Dust dust : dusts) {
            dust.draw();
        }

        for (int i = 0; i < dusts.size(); i++) {
            Dust d1 = dusts.get(i);
            if (d1.m <= 0) {
                continue;
            }
            for (int j = i + 1; j < dusts.size(); j++) {
                Dust d2 = dusts.get(j);
                if (d2.m <= 0) {
                    continue;
                }
                double dx = d2.x - d1.x;
                double dy = d2.y - d1.y;
                double r2 = dx * dx + dy * dy;
                double G = 1.0;
                double M = d1.m + d2.m;
                double d = d1.radius() + d2.radius();

                if (r2 < d * d) {
                    /*d1.m = M;
                    d2.m = 0;
                    double px = d1.m * d1.vx + d2.m * d2.vx;
                    double py = d1.m * d1.vy + d2.m * d2.vy;
                    d1.vx = px / (d1.m + d2.m);
                    d1.vy = py / (d1.m + d2.m);
                    d1.x = d1.radius() > d2.radius() ? d1.x : d2.x;
                    d1.y = d1.radius() > d2.radius() ? d1.y : d2.y;*/
                    continue;
                }
                double alpha = Math.atan2(dy, dx);
                double a1 = G * d2.m / r2;
                double a2 = G * d1.m / r2;
                double a1x = a1 * Math.cos(alpha);
                double a1y = a1 * Math.sin(alpha);
                double a2x = a2 * Math.cos(alpha);
                double a2y = a2 * Math.sin(alpha);
                d1.vx += a1x;
                d2.vx -= a2x;
                d1.vy += a1y;
                d2.vy -= a2y;
            }
        }
        for (Dust d : dusts) {
            d.x += d.vx;
            d.y += d.vy;
        }
        dusts.removeIf(d -> d.m <= 0);
        canvas.drawText(0, 10.0, 10.0, "FPS: " + (int)(1.0 / elapsed), true);
    }

    public static void main(String[] args) {
        Gravity gravity = new Gravity();
        gravity.show();
    }

    class Dust {
        double x = 0.0;
        double y = 0.0;
        double vx = 0.0;
        double vy = 0.0;
        double m = 1.0;

        public Dust(double x, double y) {
            this.x = x;
            this.y = y;
        }

        double radius() {
            return 8.0 * Math.pow(m, 1 / 3.0);
        }

        void draw() {
            canvas.fillCircle(0, x, y, radius());
        }
    }
}
