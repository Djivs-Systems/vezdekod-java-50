package main.java;

import lib.Window;
import lib.render.Texture;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.sql.Array;
import java.util.concurrent.ThreadLocalRandom;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Game extends Window {
    private double thickness = 3;
    private final int circle_increase_step = 10;
    private final int small_circles_r = 15;
    private final double g = 9.8;

    private final Texture backgroundTexture = Texture.load("back.png");

    Circle mainCircle, cursorCircle;

    ArrayList<Circle> circlesArray;
    ArrayList<Integer> circlesDirections;

    public Game() {
        super(800, 600, "Vezdekod :3", true, "Arial", 30);
        mainCircle = new Circle(new Point2D.Double(400, 300), 200);
        cursorCircle = new Circle(new Point2D.Double(400, 300), small_circles_r);
        circlesArray = new ArrayList<Circle>();
        circlesDirections = new ArrayList<Integer>();
    }

    class Circle {
        private int radius;
        private int color;
        private int direction = -1;
        private double speed = 1;
        private double k = 1;
        private Point2D.Double center;
        private ArrayList<Point2D.Double> pointsArray;
        private final int angles_amount = 100;
        private final int screenHeight = 600;

        public Circle(Point2D.Double circle_center, int radius) {
            setColor(0);
            pointsArray = new ArrayList<Point2D.Double>();

            this.radius = radius;
            this.center = circle_center;
            fillPointsArray();
        }

        public void setColor(int color) {
            this.color = color;
        }

        public void setK(double k) {
            this.k = k;
        }

        public void setRadius(int radius) {
            if (radius <= 0 || radius * 2 > screenHeight)
                return;
            this.radius = radius;
            fillPointsArray();
        }

        public void setCenter(Point2D.Double circle_center) {
            if(circle_center.y < radius)
                circle_center.y = radius;
            this.center = circle_center;
            fillPointsArray();
        }

        public void setDirection(int direction) {
            if (direction != 1 && direction != -1)
                return;
            this.direction = direction;
        }

        public void setSpeed(double speed) {
            if (speed < 0 && speed != -1)
                return;
            this.speed = speed;
        }

        public double getK() {
            return k;
        }

        public double getSpeed() {
            return speed;
        }

        public int getDirection() {
            return direction;
        }

        public int getRadius() {
            return radius;
        }
        public Point2D.Double getCenter() {
            return center;
        }
        public int getColor() {
            return color;
        }

        public void drawOnCanvas(lib.render.Canvas canvas, double thickness, boolean fill) {
            for (int i = 1; i < angles_amount; ++i) {
                if (fill)
                    canvas.drawTriangle(center.x, center.y, pointsArray.get(i).x, pointsArray.get(i).y,pointsArray.get(i-1).x, pointsArray.get(i-1).y, color);
                else
                canvas.drawLine(color, pointsArray.get(i).x, pointsArray.get(i).y,pointsArray.get(i-1).x, pointsArray.get(i-1).y, thickness);
            }
            if (fill)
                canvas.drawTriangle(center.x, center.y, pointsArray.get(0).x, pointsArray.get(0).y,pointsArray.get(angles_amount-1).x, pointsArray.get(angles_amount-1).y, color);
            else
                canvas.drawLine(color, pointsArray.get(angles_amount-1).x, pointsArray.get(angles_amount-1).y,pointsArray.get(0).x, pointsArray.get(0).y, thickness);
        }

        private void fillPointsArray() {
            if (pointsArray.size() > 0)
                pointsArray.clear();
            double rotating_angle = 360.0/angles_amount;
            double passed_angle = 0;
            double x, y;
            pointsArray.add(new Point2D.Double(center.x - radius, center.y));
            for (int i = 0; i < angles_amount - 1; ++i) {
                passed_angle += rotating_angle;
                x = center.x - Math.cos(Math.toRadians(passed_angle)) * radius;
                y = center.y - Math.sin(Math.toRadians(passed_angle)) * radius;
                pointsArray.add(new Point2D.Double(x, y));
            }
        }

    }

    private boolean areCirclesCrossing(Circle c1, Circle c2) {
        if (c1.getRadius() < c2.getRadius()) {
            Circle c = c1;
            c1 = c2;
            c2 = c;
        }
        double dist = Math.sqrt(Math.pow(c1.getCenter().x - c2.getCenter().x, 2) +  Math.pow(c1.getCenter().y - c2.getCenter().y, 2));
        return ((dist <= c1.getRadius() + c2.getRadius()) && (dist > c1.getRadius() - c2.getRadius())) || ((c1.getCenter() == c2.getCenter()) && (c1.getRadius() == c2.getRadius()));
    }
    private void processCursorMainCirclesCrossing() {
        if (areCirclesCrossing(mainCircle, cursorCircle)) {
            if (cursorCircle.getColor() == 0)
                cursorCircle.setColor(-50);
        } else if (cursorCircle.getColor() == -50) {
            cursorCircle.setColor(0);
        }
    }
    void processCustomCircleCrossings(Circle c) {

        if (areCirclesCrossing(c, mainCircle)) {
            System.out.println(areCirclesCrossing(c, mainCircle));
            c.setColor(-50);
        } else if ((c.getCenter() != cursorCircle.getCenter()) && areCirclesCrossing(c, cursorCircle)) {
            c.setColor(56793);
        }
        for (Circle circle : circlesArray) {
            if (circle == c)
                continue;
            else {
                if (areCirclesCrossing(c, circle)) {
                    circle.setColor(89456);
                    c.setColor(89456);
                }
            }
        }
    }

    private void moveCircles() {
        for (Circle circle : circlesArray) {
            if (circle.getSpeed() == -1)
                continue;
            double x = circle.getCenter().x;
            double y = circle.getCenter().y - circle.getDirection() * circle.getSpeed() * circle.getK();
            circle.setCenter(new Point2D.Double(x, y));
            if (circle.getDirection() == -1 && y >= 600 - circle.getRadius()) {
                circle.setK(circle.getK() - 0.02);
                if (circle.getK() == 0) {
                    circle.setSpeed(-1);
                    return;
                }
                circle.setDirection(-circle.getDirection());
            }
            circle.setSpeed(circle.getSpeed() - circle.getDirection() * g);
            if (circle.speed == 0) {
                circle.setDirection(-circle.getDirection());
            }
            processCustomCircleCrossings(circle);
        }
    }
    @Override
    protected void onFrame(double elapsed) {
        canvas.drawTexture(backgroundTexture, 0, 0, width, height, width, height);
        mainCircle.drawOnCanvas(canvas, thickness, false);
        for (Circle cirlce : circlesArray) {
            cirlce.drawOnCanvas(canvas, thickness, true);
        }
        moveCircles();
        if (cursorOver)
            cursorCircle.drawOnCanvas(canvas, thickness, true);
    }

    @Override
    protected void onKeyButton(int key, int scancode, int action, int mods) {
        if (key == GLFW.GLFW_KEY_UP && action == GLFW.GLFW_PRESS) {
            mainCircle.setRadius(mainCircle.getRadius() + circle_increase_step);
            processCursorMainCirclesCrossing();
        } else if (key == GLFW.GLFW_KEY_DOWN && action == GLFW.GLFW_PRESS) {
            mainCircle.setRadius(mainCircle.getRadius() - circle_increase_step);
            processCursorMainCirclesCrossing();
        }
    }

    @Override
    protected void onScroll(double dx, double dy) {
        switch ((int) dy) {
            case -1:
                mainCircle.setRadius(mainCircle.getRadius() - circle_increase_step);
                break;
            case 1:
                mainCircle.setRadius(mainCircle.getRadius() + circle_increase_step);
                break;
            default:
                return;
        }
        processCursorMainCirclesCrossing();
    }

    @Override
    protected void onCursorMoved(double x, double y) {
        cursorX = x;
        cursorY = y;
        cursorCircle.setCenter(new Point2D.Double(cursorX, cursorY));
        processCursorMainCirclesCrossing();
    }

    @Override
    protected void onMouseButton(int button, int action, int mods) {
        if ((action == 1) && (button == 0)) {
            Circle c = new Circle(new Point2D.Double(cursorX, cursorY), small_circles_r);
            circlesDirections.add(-1);
            circlesArray.add(c);
            processCustomCircleCrossings(c);
        }
    }

    public static void main(String[] args) {
        new Game().show();
    }
};