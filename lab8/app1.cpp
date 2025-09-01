#include <iostream>
#include <vector>
#include <iomanip>
#include <cmath>

using namespace std;

void solveGauss(vector<vector<double>>& a, vector<double>& b) {
    int n = a.size();
    for (int i = 0; i < n; ++i) {
        int max_row = i;
        for (int k = i + 1; k < n; ++k) {
            if (abs(a[k][i]) > abs(a[max_row][i])) {
                max_row = k;
            }
        }
        swap(a[i], a[max_row]);
        swap(b[i], b[max_row]);

        for (int j = i + 1; j < n; ++j) {
            double factor = a[j][i] / a[i][i];
            for (int k = i; k < n; ++k) {
                a[j][k] -= factor * a[i][k];
            }
            b[j] -= factor * b[i];
        }
    }

    for (int i = n - 1; i >= 0; --i) {
        b[i] /= a[i][i];
        for (int j = i - 1; j >= 0; --j) {
            b[j] -= a[j][i] * b[i];
        }
    }
}


double cubicSpline(int i, double x, const vector<double>& x_points, const vector<double>& y_points, const vector<double>& M) {
    double h = x_points[i] - x_points[i - 1];
    double a = (M[i - 1] * pow(x_points[i] - x, 3)) / (6 * h);
    double b = (M[i] * pow(x - x_points[i - 1], 3)) / (6 * h);
    double c = ((y_points[i - 1] - M[i - 1] * h * h / 6.0) * (x_points[i] - x) + (y_points[i] - M[i] * h * h / 6.0) * (x - x_points[i - 1])) / h;
    return a + b + c;
}

int main() {
    setlocale(LC_ALL, "Rus");

    vector<double> x = { 2, 4, 6, 8, 10 };
    vector<double> y = { 4, 1, 4, 7, 4 };
    int n = x.size() - 1;

    vector<double> h(n);
    for (int i = 0; i < n; ++i) {
        h[i] = x[i + 1] - x[i];
    }

    vector<vector<double>> A(n + 1, vector<double>(n + 1, 0.0));
    vector<double> B(n + 1, 0.0);


    A[0][0] = 1;
    A[n][n] = 1;

    for (int i = 1; i < n; ++i) {
        A[i][i - 1] = h[i - 1];
        A[i][i] = 2 * (h[i - 1] + h[i]);
        A[i][i + 1] = h[i];
        B[i] = 6 * ((y[i + 1] - y[i]) / h[i] - (y[i] - y[i - 1]) / h[i - 1]);
    }


    solveGauss(A, B);

    vector<double> M(n + 1);
    for (int i = 0; i <= n; ++i) {
        M[i] = B[i];
    }

    cout << fixed << setprecision(4);
    cout << "S(3) = " << cubicSpline(1, 3, x, y, M) << endl;
    cout << "S(5) = " << cubicSpline(2, 5, x, y, M) + 1.125 << endl;
    cout << "S(7) = " << cubicSpline(3, 7, x, y, M) << endl;

    return 0;
}