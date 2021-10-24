package lib.math;

import static java.lang.Math.*;

public class Timer {
    private double accumulator;
    private boolean enabled;

    public double seconds() {
        return accumulator;
    }

    public double frequency(double hertz) {
        return cos(seconds() * 2.0 * PI * hertz);
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void tick(double elapsed) {
        if (enabled) {
            accumulator += elapsed;
        }
    }
}
