package lib.math;

public class RenderMaths {
    public static final double EPS = 0.0000001;
    public static final double ONE_MIN_EPS = 1.0 - EPS;

    public static int clamp(int num, int min, int max) {
        return num < min ? min : Math.min(num, max);
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double clamp(double num, double min, double max) {
        return num < min ? min : Math.min(num, max);
    }

    public static int roundUp(int number, int interval) {
        if (interval == 0) {
            return 0;
        } else if (number == 0) {
            return interval;
        } else {
            if (number < 0) {
                interval *= -1;
            }

            int i = number % interval;
            return i == 0 ? number : number + interval - i;
        }
    }

    public static double integrate(Function function, double start, double end, double precision) {
        double result = 0;
        int n = (int)((end - start) / precision);

        for(int i = 0; i < n; i++) {
            double value = function.apply(start + precision * (i + 0.5));
            if (value == value) { //Omit NaN values
                result += value;
            }
        }

        return result * precision;
    }

    public static long factorial(long n) {
        long result = 1;

        for (int i = 2; i <= n; i++) {
            result *= i;
        }

        return result;
    }

    public static double format(double value) {
        return format(value, 3);
    }

    public static double format(double value, int zeroes) {
        double mod = Math.pow(10, Math.max(0, zeroes));
        if (value != value) {
            return value;
        }
        return Math.round((int)(value * mod * 10) / 10.0) / mod;
    }
}
