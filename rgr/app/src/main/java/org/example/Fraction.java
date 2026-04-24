package org.example;

public class Fraction {
    private long num;
    private long den;

    public Fraction(long num, long den) {
        if (den == 0) throw new ArithmeticException("Division by zero");
        this.num = num;
        this.den = den;
        normalize();
    }

    public Fraction(long num) {
        this(num, 1);
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

    public Fraction add(Fraction o) {
        return new Fraction(num * o.den + o.num * den, den * o.den);
    }

    public Fraction sub(Fraction o) {
        return new Fraction(num * o.den - o.num * den, den * o.den);
    }

    public Fraction mul(Fraction o) {
        return new Fraction(num * o.num, den * o.den);
    }

    public Fraction div(Fraction o) {
        return new Fraction(num * o.den, den * o.num);
    }

    public boolean isNegative() {
        return num < 0;
    }

    public boolean isZero() {
        return num == 0;
    }

    public double toDouble() {
        return (double) num / den;
    }

    public String toString() {
        return den == 1 ? String.valueOf(num) : num + "/" + den;
    }

    public int compareTo(Fraction o) {
        long left = this.num * o.den;
        long right = o.num * this.den;
        return Long.compare(left, right);
    }

}
