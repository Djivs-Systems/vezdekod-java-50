package lib.math;

public class Vec2 {
    public final double x, y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 multiply(double scalar) {
        return new Vec2(x * scalar, y * scalar);
    }

    public Vec2 add(double dx, double dy) {
        return new Vec2(x + dx, y + dy);
    }

    public Vec2 add(Vec2 other) {
        return add(other.x, other.y);
    }

    public Vec2 subtract(double dx, double dy) {
        return add(-dx, -dy);
    }

    public Vec2 subtract(Vec2 other) {
        return add(-other.x, -other.y);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public Vec2 normalize() {
        double length = length();
        return new Vec2(x / length, y / length);
    }

    public static Vec2 rotation(double angle) {
        return rotation(angle, 1.0);
    }

    public static Vec2 rotation(double angle, double length) {
        return new Vec2(Math.cos(angle) * length, Math.sin(angle) * length);
    }
}
