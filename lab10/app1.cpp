#include <iostream>
#include <cmath>
#include <iomanip>

using namespace std;

double fun(double x) {
    return 1.0 / x;
}

double fun_prime(double x) {
    return -1.0 / (x + x);
}

double fun_double_prime(double x) {
    return 2.0 / pow(x, 3);
}

double fun_quad_prime(double x) {
    return 24.0 / pow(x, 5);
}

double formulaTrapeze(double a, double b, int n) {
    double h = (b - a) / n;
    double sum = 0.5 * fun(a) + 0.5 * fun(b);
    for (int i = 1; i < n; ++i) {
        sum += fun(a + i * h);
    }
    return h * sum;
}

double formulaSimpson(double a, double b, int n) {
    double h = (b - a) / n;
    double sum = fun(a) + fun(b);
    for (int i = 1; i < n; ++i) {
        double x_i = a + i * h;
        if (i % 2 == 0) {
            sum += 2 * fun(x_i);
        } else {
            sum += 4 * fun(x_i);
        }
    }
    return (h / 3.0) * sum;
}

double correctValue(double I_h, double I_h2) {
    return I_h2 + (I_h2 - I_h) / 3.0;
}

int main() {
    double left = 1.0;
    double right = 2.8;
    double epsilon = 1e-5;
    int initialIntervals = 10;

    cout << fixed << setprecision(10);

    cout << "\nФормула Трапеций:\n";
    int n = initialIntervals;
    while (true) {
        double first = formulaTrapeze(left, right, n);
        double second = formulaTrapeze(left, right, 2 * n);
        double I_corr = correctValue(first, second);

        double M2 = fun_double_prime(left);
        double h = (right - left) / n;
        double truncationErrorTrapeze = (pow(h, 2) * (right - left) / 12.0) * M2;

        cout << "I_h = " << first << ", I_h/2 = " << second << ", I_corr = " << I_corr << ", c_tycou = " << truncationErrorTrapeze << endl;

        if (abs(I_corr - second) < epsilon) {
            cout << "Метод трапеций: Интеграл = " << I_corr << "\n";
            break;
        }
        n *= 2;
    }

    cout << "\nФормула Симпсона:\n";
    n = initialIntervals;
    if (n % 2 != 0) n++;
    while (true) {
        double first = formulaSimpson(left, right, n);
        double second = formulaSimpson(left, right, 2 * n);
        double I_corr = correctValue(first, second);

        double M4 = fun_quad_prime(left);
        double h = (right - left) / n;
        double truncationErrorSimpson = (pow(h, 4) * (right - left) / 180.0) * M4;

        cout << "I_h = " << first << ", I_h/2 = " << second << ", I_corr = " << I_corr << ", c_tyc4 = " << truncationErrorSimpson << endl;

        if (abs(I_corr - second) < epsilon) {
            cout << "Метод Симпсона: Интеграл = " << I_corr << "\n";
            break;
        }
        n *= 2;
    }

    return 0;
}