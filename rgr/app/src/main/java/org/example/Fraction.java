package org.example;

public class Fraction {
    private long num;
    private long den;

    public static final Fraction ZERO = new Fraction(0);
    public static final Fraction ONE = new Fraction(1);

    public Fraction(long num, long den) {
        if (den == 0) throw new ArithmeticException("Division by zero");
        this.num = num;
        this.den = den;
        normalize();
    }

    public Fraction(long num) {
        this(num, 1);
    }

    public Fraction neg() {
        return new Fraction(-num, den);
    }

    public Fraction inverse() {
        if (num == 0) throw new ArithmeticException("Zero inverse");
        return new Fraction(den, num);
    }

    public boolean isZero() {
        return num == 0;
    }

    public boolean isNegative() {
        return num < 0;
    }

    public Fraction abs() {
        return new Fraction(Math.abs(num), den);
    }

    public Fraction add(Fraction o) {
        return new Fraction(
                safeMul(num, o.den) + safeMul(o.num, den),
                safeMul(den, o.den)
        );
    }

    public Fraction sub(Fraction o) {
        return new Fraction(
                safeMul(num, o.den) - safeMul(o.num, den),
                safeMul(den, o.den)
        );
    }

    public Fraction mul(Fraction o) {
        return new Fraction(
                num * o.num,
                den * o.den
        );
    }

    public Fraction div(Fraction o) {
        if (o.isZero())
            throw new ArithmeticException("Деление на ноль");

        return new Fraction(
                num * o.den,
                den * o.num
        );
    }

    private long safeMul(long a, long b) {

        if (a == 0 || b == 0) {
            return 0;
        }

        if (a > Long.MAX_VALUE / b || a < Long.MIN_VALUE / b) {
            throw new ArithmeticException("Overflow in fraction multiplication");
        }

        return a * b;
    }

    private void normalize() {
        if (den < 0) {
            num = -num;
            den = -den;
        }
        long gcd = gcd(Math.abs(num), Math.abs(den));
        num /= gcd;
        den /= gcd;
    }

    private long gcd(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    public int compareTo(Fraction o) {
        return Long.compare(
                safeMul(this.num, o.den),
                safeMul(o.num, this.den)
        );
    }

    @Override
    public String toString() {
        return den == 1 ? String.valueOf(num) : num + "/" + den;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Fraction)) return false;
        Fraction o = (Fraction) obj;
        return num == o.num && den == o.den;
    }
}
