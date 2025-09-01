#include <iostream>
#include <vector>
#include <complex>
#include <cmath>

const int n = 4;
const double T = 4.0; 
const double h = 1.0; 

std::vector<std::complex<double>> computeCoefficients(const std::vector<double>& y) {
    std::vector<std::complex<double>> A(n, 0.0);

    for (int j = -n / 2 + 1; j <= n / 2; ++j) {
        for (int k = 0; k < n; ++k) {
            double angle = -2 * M_PI * k * j / n;
            A[j + n / 2] += y[k] * std::complex<double>(cos(angle), sin(angle));
        }
        A[j + n / 2] /= n;
    }

    return A;
}

std::complex<double> interpolate(const std::vector<std::complex<double>>& A, double x) {
    std::complex<double> result = 0.0;

    for (int j = -n / 2 + 1; j <= n / 2; ++j) {
        double angle = 2 * M_PI * j * x / T;
        result += A[j + n / 2] * std::complex<double>(cos(angle), sin(angle));
    }

    return result;
}

int main() {
    std::vector<double> y(n);
    std::cout << "Введите значения y (4 числа): ";
    for (int i = 0; i < n; ++i) {
        std::cin >> y[i];
    }

    std::vector<std::complex<double>> A = computeCoefficients(y);

    double x_interpolate = 1.5;
    std::complex<double> y_interpolated = interpolate(A, x_interpolate);

    std::cout << "Интерполированное значение y(" << x_interpolate << ") = "
              << y_interpolated.real() << " + " << y_interpolated.imag() << "i" << std::endl;

    return 0;
}