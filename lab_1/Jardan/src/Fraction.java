public class Fraction implements Comparable<Fraction> {

    private final long numerator;
    private final long denominator;

    public Fraction(long num) {
        this(num, 1);
    }

    public Fraction(long num, long den) {
        if (den == 0)
            throw new IllegalArgumentException("Знаменатель не может быть 0");

        if (num == 0) {
            numerator = 0;
            denominator = 1;
            return;
        }

        long g = gcd(Math.abs(num), Math.abs(den));
        num /= g;
        den /= g;

        if (den < 0) {
            num = -num;
            den = -den;
        }

        numerator = num;
        denominator = den;
    }

    public Fraction(double value) {
        long scale = 1_000_000L;
        long num = Math.round(value * scale);
        long den = scale;
        long g = gcd(Math.abs(num), Math.abs(den));
        num /= g;
        den /= g;

        if (den < 0) {
            num = -num;
            den = -den;
        }

        numerator = num;
        denominator = den;
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    public boolean isZero() {
        return numerator == 0;
    }

    public Fraction add(Fraction o) {
        return new Fraction(
                numerator * o.denominator + o.numerator * denominator,
                denominator * o.denominator
        );
    }

    public Fraction subtract(Fraction o) {
        return new Fraction(
                numerator * o.denominator - o.numerator * denominator,
                denominator * o.denominator
        );
    }

    public Fraction multiply(Fraction o) {
        return new Fraction(
                numerator * o.numerator,
                denominator * o.denominator
        );
    }

    public Fraction divide(Fraction o) {
        if (o.isZero())
            throw new ArithmeticException("Деление на ноль");

        return new Fraction(
                numerator * o.denominator,
                denominator * o.numerator
        );
    }

    public Fraction negate() {
        return new Fraction(-numerator, denominator);
    }

    public Fraction abs() {
        return new Fraction(Math.abs(numerator), denominator);
    }

    @Override
    public int compareTo(Fraction o) {
        long left = numerator * o.denominator;
        long right = o.numerator * denominator;
        return Long.compare(left, right);
    }

    @Override
    public String toString() {
        if (denominator == 1) return String.valueOf(numerator);
        return numerator + "/" + denominator;
    }
}
