#include <iostream>
#include <iomanip>
#include <cmath>

double f(double x) {
    return x * x - 6 * x;
}

void goldenSectionMethod(double a, double b, int steps) {
    const double phi = (sqrt(5) - 1) / 2; 
    double lambda1, lambda2;

    for (int i = 0; i < steps; ++i) {
        lambda1 = a + (b - a) * (0.382); 
        lambda2 = a + (b - a) * (0.618); 

        double f_lambda1 = f(lambda1);
        double f_lambda2 = f(lambda2);

        std::cout << "Шаг " << i + 1 << ":\n";
        std::cout << "λ1 = " << std::fixed << std::setprecision(5) << lambda1 << "\n";
        std::cout << "λ2 = " << std::fixed << std::setprecision(5) << lambda2 << "\n";
        std::cout << "f(λ1) = " << f_lambda1 << "\n";
        std::cout << "f(λ2) = " << f_lambda2 << "\n";

        if (f_lambda1 > f_lambda2) {
            a = lambda1; 
        } else {
            b = lambda2;
        }
    }

    double x = (a + b) / 2;
    double f_x = f(x);

    std::cout << "\nРезультаты вычислений:\n";
    std::cout << "x = " << std::fixed << std::setprecision(5) << x << "\n";
    std::cout << "f(x) = " << f_x << "\n";
}

int main() {
    double a = 0.0;
    double b = 5.0;
    int steps = 4;

    goldenSectionMethod(a, b, steps);

    return 0;
}