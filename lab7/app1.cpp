#include <iostream>
#include <vector>
#include <iomanip>

using namespace std;

vector<vector<double>> dividedDifferences(const vector<double>& x, const vector<double>& y) {
    int n = x.size();
    vector<vector<double>> f(n, vector<double>(n));

    for (int i = 0; i < n; ++i) {
        f[i][0] = y[i];
    }

    for (int j = 1; j < n; ++j) {
        for (int i = 0; i < n - j; ++i) {
            f[i][j] = (f[i + 1][j - 1] - f[i][j - 1]) / (x[i + j] - x[i]);
        }
    }

    return f;
}

double newtonForwardInterpolation(double value, const vector<double>& x, const vector<double>& y) {
    int n = x.size();
    vector<vector<double>> f = dividedDifferences(x, y);

    double result = f[0][0];
    double term = 1.0;

    for (int i = 1; i < n; ++i) {
        for (int j = 0; j < i; ++j) {
            term *= (value - x[j]);
        }
        result += term * f[0][i];
        term = 1.0;
    }

    return result;
}

double newtonBackwardInterpolation(double value, const vector<double>& x, const vector<double>& y) {
    int n = x.size();
    vector<vector<double>> f = dividedDifferences(x, y);

    double result = f[n - 1][0];
    double term = 1.0;

    for (int i = 1; i < n; ++i) {
        for (int j = 0; j < i; ++j) {
            term *= (value - x[n - 1 - j]);
        }
        result += term * f[n - 1 - i][i];
        term = 1.0;
    }

    return result;
}

double myFunction(double x) {
    return 1.0 / x;
}


int main() {
    vector<double> x = {1, 2, 3, 4, 5};
    vector<double> y = {1, 0.5, 0.333333, 0.25, 0.2};

    double value = 2.5;

    cout << fixed << setprecision(5);

    cout << "Значение функции в точке " << value << " (интерполяция вперед): " << newtonForwardInterpolation(value, x, y) << endl;
    cout << "Значение функции в точке " << value << " (интерполяция назад): " << newtonBackwardInterpolation(value, x, y) << endl;

    return 0;
}