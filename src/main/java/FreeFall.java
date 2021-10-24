package main.java;

import lib.Window;
import lib.render.Canvas;
import lib.render.Texture;
import lib.vertex.DefaultVertexFormats;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FreeFall extends Window {
    private final Texture logo = Texture.load("logo.png");
    private boolean enableFriction;
    private boolean negativeGravity;

    public FreeFall() {
        super(800, 800, "Free Fall", true, "Cambria Math", 46);
        setIcon("gravity.png");
    }

    @Override
    protected void onKeyButton(int key, int scancode, int action, int mods) {
        super.onKeyButton(key, scancode, action, mods);
        if (key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_RELEASE) {
            negativeGravity = !negativeGravity;
        }
        if (key == GLFW.GLFW_KEY_TAB && action == GLFW.GLFW_RELEASE) {
            enableFriction = !enableFriction;
        }
    }

    @Override
    protected void onMouseButton(int button, int action, int mods) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_RELEASE) {
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            balls.add(new Ball(cursorX, cursorY, 80 + rand.nextInt(20), rand.nextInt(0xFFFFFF), 8 + rand.nextInt(8)));
        }
    }

    class Ball {
        double speedX, speedY;
        double x;
        double y;
        final int color;
        final int radius;

        public Ball(double x, double y, double speed, int color, int radius) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.radius = radius;
        }

        public void tick(double elapsed) {
            double friction = enableFriction ? (1.0 - radius / 30.0) : 0;
            double g = 9.8 * 200 * (1.0 - friction);
            speedY += g * elapsed * (negativeGravity ? -1 : 1);
            y += speedY * elapsed;

            double loss = 0.25;

            if (x < radius) {
                speedX *= -1;
                x = radius;
            }
            if (x > width - radius) {
                speedX *= -1;
                x = width - radius;
            }
            if (y < radius) {
                y = radius;
                speedY *= -(1.0 - loss);
            }
            if (y > height - radius) {
                y = height - radius;
                speedY *= -(1.0 - loss);
            }
        }

        public double radiusX(Canvas canvas, double angle) {
            return x + radius * Math.cos(angle);
        }

        public double radiusY(Canvas canvas, double angle) {
            return y + radius * Math.sin(angle);
        }
    }

    List<Ball> balls = new ArrayList<>();

    @Override
    protected void onFrame(double elapsed) {
        canvas.drawTexture(logo, width - 64, height - 64, 64, 64, 64, 64);

        for (Ball ball : balls) {
            ball.tick(elapsed);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        canvas.draw(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR, buffer -> {
            for (Ball ball : balls) {
                double step = Math.PI / 360;
                for (double angle = 0; angle <= 2 * Math.PI; angle += step) {
                    buffer.pos(ball.x, ball.y, 0).color(ball.color).endVertex();
                    buffer.pos(ball.radiusX(canvas, angle), ball.radiusY(canvas, angle), 0).color(ball.color).endVertex();
                    buffer.pos(ball.radiusX(canvas, angle + step), ball.radiusY(canvas, angle + step), 0).color(ball.color).endVertex();
                }
            }
        });
    }

    public static void main(String[] args) {
        new FreeFall().show();
    }
}
