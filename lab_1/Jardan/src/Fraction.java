public class Fraction {
    private long numerator;     // Числитель
    private long denominator;   // Знаменатель

    public Fraction(long num, long den) {
        if (den == 0) {
            throw new IllegalArgumentException("Знаменатель не может быть 0");
        }
        long g = gcd(Math.abs(num), Math.abs(den));
        numerator = num / g;
        denominator = den / g;
        // Приведение к положительному знаменателю
        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }
    }

    public Fraction(double value) {
        this((long)(value * 1000000), 1000000);
    }

    // Конструктор копирования
    public Fraction(Fraction other) {
        this.numerator = other.numerator;
        this.denominator = other.denominator;
    }

    // Статический метод НОД (Евклидов алгоритм)
    private static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    // Геттеры
    public long getNumerator() {
        return numerator;
    }

    public long getDenominator() {
        return denominator;
    }

    // Арифметические операции
    public Fraction add(Fraction other) {
        long newNum = numerator * other.denominator + other.numerator * denominator;
        long newDen = denominator * other.denominator;
        return new Fraction(newNum, newDen);
    }

    public Fraction subtract(Fraction other) {
        long newNum = numerator * other.denominator - other.numerator * denominator;
        long newDen = denominator * other.denominator;
        return new Fraction(newNum, newDen);
    }

    public Fraction multiply(Fraction other) {
        long newNum = numerator * other.numerator;
        long newDen = denominator * other.denominator;
        return new Fraction(newNum, newDen);
    }

    public Fraction divide(Fraction other) {
        if (other.numerator == 0) {
            throw new ArithmeticException("Деление на ноль");
        }
        long newNum = numerator * other.denominator;
        long newDen = denominator * other.numerator;
        return new Fraction(newNum, newDen);
    }

    public Fraction negate() {
        return new Fraction(-numerator, denominator);
    }

    // Сравнение с нулём
    public boolean isZero() {
        return numerator == 0;
    }

    // Сравнение с EPS
    public boolean isNearZero(double eps) {
        return Math.abs(toDouble()) < eps;
    }

    // Преобразование в double
    public double toDouble() {
        return (double) numerator / denominator;
    }

    // Сравнение дробей
    public int compareTo(Fraction other) {
        long diffNum = numerator * other.denominator - other.numerator * denominator;
        long diffDen = denominator * other.denominator;
        if (diffNum * diffDen < 0) return -1;
        if (diffNum * diffDen > 0) return 1;
        return 0;
    }

    // Абсолютное значение
    public Fraction abs() {
        return new Fraction(Math.abs(numerator), denominator);
    }

    // Возведение в степень (для положительных целых степеней)
    public Fraction pow(int exp) {
        if (exp < 0) {
            return new Fraction(1).divide(pow(-exp));
        }
        Fraction result = new Fraction(1);
        Fraction base = new Fraction(this);
        while (exp > 0) {
            if ((exp & 1) == 1) {
                result = result.multiply(base);
            }
            base = base.multiply(base);
            exp >>= 1;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Fraction)) return false;
        Fraction other = (Fraction) obj;
        return numerator == other.numerator && denominator == other.denominator;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(numerator) ^ Long.hashCode(denominator);
    }

    // Строковое представление
    @Override
    public String toString() {
        if (denominator == 1) {
            return Long.toString(numerator);
        }
        return numerator + "/" + denominator;
    }

    // Форматированный вывод для матрицы (число с 6 знаками)
    public String toFormattedString(int width) {
        if (denominator == 1 && Math.abs(numerator) < 1000) {
            return String.format("%" + width + "d", numerator);
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.000000");
        return String.format("%" + width + "s", df.format(toDouble()));
    }

    // Вывод как смешанная дробь (целая + дробная часть)
    public String toMixedString() {
        if (denominator == 1) {
            return Long.toString(numerator);
        }
        long whole = numerator / denominator;
        long remNum = Math.abs(numerator % denominator);
        if (remNum == 0) {
            return Long.toString(whole);
        }
        return whole + " " + remNum + "/" + denominator;
    }
}
