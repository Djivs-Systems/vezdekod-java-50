import lib.math.Series;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static lib.math.RenderMaths.factorial;

public class Pi {
    private static final Series PI = (n, x) -> 1.0 / (n * n);

    private static final Series PI_FAST = (n, x) -> factorial(4 * n) * (1103.0 + 26390 * n) / pow(factorial(n), 4) / pow(396, 4 * n);

    public static void main(String[] args) {
        double sum = 2 * sqrt(2) / 9801 * PI_FAST.sum(0, 2).apply(0);
        System.out.println("PI = " + 1.0 / sum);
        System.out.println("PI = " + Math.PI);
    }
}
