package lib.math;

import static java.lang.Math.pow;
import static lib.math.RenderMaths.factorial;

public interface Series {
    Series SIN = (n, x) -> pow(-1, n) * pow(x, 2 * n + 1) / factorial(2 * n + 1);
    Series COS = (n, x) -> pow(-1, n) * pow(x, 2 * n) / factorial(2 * n);
    Series EXP = (n, x) -> pow(x, n) / factorial(n);

    double term(long n, double x);

    default Function sum(long from, long to) {
        return x -> sum(x, from, to);
    }

    default double sum(double x, long from, long to) {
        double sum = 0;
        for (long n = from; n <= to; n++) {
            double term = term(n, x);
            sum += term;
        }
        return sum;
    }
}
