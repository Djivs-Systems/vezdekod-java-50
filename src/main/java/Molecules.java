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

public class Molecules extends Window {
    private final Texture logo = Texture.load("logo.png");

    public Molecules() {
        super(800, 800, "Molecules", true, "Cambria Math", 46);
        setIcon("molecules.png");
    }

    @Override
    protected void onMouseButton(int button, int action, int mods) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_RELEASE) {
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            molecules.add(new Molecule(cursorX, cursorY, 80 + rand.nextInt(20), rand.nextInt(0xFFFFFF), 4 + rand.nextInt(2)));
        }
    }

    static class Molecule {
        double speedX, speedY;
        double x;
        double y;
        final int color;
        final int radius;

        public Molecule(double x, double y, double speed, int color, int radius) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.radius = radius;
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            this.speedX = speed * (rand.nextBoolean() ? -1 : 1);
            this.speedY = speed * (rand.nextBoolean() ? -1 : 1);
        }

        public void tick(Canvas canvas, double elapsed, boolean cursorOver, double cursorX, double cursorY, double width, double height) {
            x += speedX * elapsed;
            y += speedY * elapsed;

            if (cursorOver) {
                if (speedX > 0 && (x >= cursorX - radius && x <= cursorX + radius)) {
                    speedX *= -1;
                }
            }
            if (x < 0) {
                speedX *= -1;
                x = 0;
            }
            if (x > width) {
                speedX *= -1;
                x = width;
            }
            if (y < 0) {
                y = 0;
                speedY *= -1;
            }
            if (y > height) {
                y = height;
                speedY *= -1;
            }
        }

        public double radiusX(Canvas canvas, double angle) {
            return x + radius * Math.cos(angle);//(x / (double) width - 0.5) * 2.0;
        }

        public double radiusY(Canvas canvas, double angle) {
            return y + radius * Math.sin(angle);//(y / (double) height - 0.5) * 2.0;
        }
    }

    List<Molecule> molecules = new ArrayList<>();

    @Override
    protected void onFrame(double elapsed) {
        canvas.drawGrid(0xCCCCCC, 40);

        canvas.drawTexture(logo, width - 64, height - 64, 64, 64);

        for (Molecule molecule : molecules) {
            molecule.tick(canvas, elapsed, cursorOver, cursorX, cursorY, width, height);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        canvas.draw(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR, buffer -> {
            for (Molecule molecule : molecules) {
                double step = Math.PI / 360;
                for (double angle = 0; angle <= 2 * Math.PI; angle += step) {
                    buffer.pos(molecule.x, molecule.y, 0).color(molecule.color).endVertex();
                    buffer.pos(molecule.radiusX(canvas, angle), molecule.radiusY(canvas, angle), 0).color(molecule.color).endVertex();
                    buffer.pos(molecule.radiusX(canvas, angle + step), molecule.radiusY(canvas, angle + step), 0).color(molecule.color).endVertex();
                }
            }
        });

        if (cursorOver) {
            canvas.drawLine(0, cursorX, 0, cursorX, height, 1);
        }
    }

    public static void main(String[] args) {
        new Molecules().show();
    }
}
