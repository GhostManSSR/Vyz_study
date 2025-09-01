#include <iostream>
#include <vector>
#include <cmath>
#include <iomanip>

using namespace std;

vector<double> gauss(vector<vector<double>> A, vector<double> b) {
    int n = A.size();

    for (int i = 0; i < n; i++) {
        double maxEl = abs(A[i][i]);
        int maxRow = i;
        for (int k = i + 1; k < n; k++) {
            if (abs(A[k][i]) > maxEl) {
                maxEl = abs(A[k][i]);
                maxRow = k;
            }
        }

        for (int k = i; k < n; k++) {
            double tmp = A[maxRow][k];
            A[maxRow][k] = A[i][k];
            A[i][k] = tmp;
        }
        double tmp = b[maxRow];
        b[maxRow] = b[i];
        b[i] = tmp;

        for (int k = i + 1; k < n; k++) {
            double c = -A[k][i] / A[i][i];
            for (int j = i; j < n; j++) {
                if (i == j) {
                    A[k][j] = 0;
                } else {
                    A[k][j] += c * A[i][j];
                }
            }
            b[k] += c * b[i];
        }
    }

    vector<double> x(n);
    for (int i = n - 1; i >= 0; i--) {
        x[i] = b[i] / A[i][i];
        for (int k = i - 1; k >= 0; k--) {
            b[k] -= A[k][i] * x[i];
        }
    }
    return x;
}

int main() {
    vector<double> x = {0, 1, 2, 3};
    vector<double> y = {0, 1, 4, 9};

    vector<vector<double>> A(3, vector<double>(3, 0));
    vector<double> b(3, 0);

    for (int i = 0; i < x.size(); i++) {
        double xi = x[i];
        double sqrt_xi = sqrt(xi);

        A[0][0] += 1;
        A[0][1] += xi;
        A[0][2] += sqrt_xi;
        b[0] += y[i];

        A[1][0] += xi;
        A[1][1] += xi * xi;
        A[1][2] += xi * sqrt_xi;
        b[1] += y[i] * xi;

        A[2][0] += sqrt_xi;
        A[2][1] += xi * sqrt_xi;
        A[2][2] += xi;
        b[2] += y[i] * sqrt_xi;
    }

    vector<double> coeffs = gauss(A, b);

    cout << "Коэффициенты: a = " << coeffs[0] << ", b = " << coeffs[1] << ", c = " << coeffs[2] << endl;

    cout << "Проверка аппроксимации:" << endl;
    for (int i = 0; i < x.size(); i++) {
        double approx = coeffs[0] + coeffs[1] * x[i] + coeffs[2] * sqrt(x[i]);
        cout << "x = " << x[i] << ", y = " << y[i] << ", аппроксимация = " << approx << endl;
    }

    return 0;
}