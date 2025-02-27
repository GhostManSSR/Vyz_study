#include <iostream>
#include <vector>
#include <cmath>
#include <stdexcept>

using namespace std;

const double EPSILON = 1e-6;
const double DELTA = 1e-5;

vector<double> gauss(vector<vector<double>> A, vector<double> b) {
    int n = A.size();
    for (int i = 0; i < n; ++i) {
        A[i].push_back(b[i]);
    }

    for (int i = 0; i < n; ++i) {
        int pivot = i;
        for (int j = i + 1; j < n; ++j) {
            if (abs(A[j][i]) > abs(A[pivot][i])) {
                pivot = j;
            }
        }
        swap(A[i], A[pivot]);

        if (abs(A[i][i]) < EPSILON) {
            throw runtime_error("Ошибка: матрица Якоби вырождена (система несовместна или метод не сходится).");
        }

        for (int j = i + 1; j <= n; ++j) {
            A[i][j] /= A[i][i];
        }

        for (int j = 0; j < n; ++j) {
            if (j != i) {
                double factor = A[j][i];
                for (int k = i; k <= n; ++k) {
                    A[j][k] -= factor * A[i][k];
                }
            }
        }
    }

    vector<double> x(n);
    for (int i = 0; i < n; ++i) {
        x[i] = A[i][n];
        if (abs(x[i]) < EPSILON) x[i] = 0;
    }
    return x;
}

vector<double> computeFx(const vector<vector<double>>& A, const vector<double>& b, const vector<double>& x) {
    int n = A.size();
    vector<double> fx(n, 0.0);
    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < n; ++j) {
            fx[i] += A[i][j] * x[j];
        }
        fx[i] -= b[i];
    }
    return fx;
}

vector<vector<double>> computeJacobianNumerically(const vector<vector<double>>& A, const vector<double>& b, const vector<double>& x) {
    int n = x.size();
    vector<vector<double>> J(n, vector<double>(n));

    vector<double> fx = computeFx(A, b, x);

    for (int i = 0; i < n; ++i) {
        vector<double> x_temp = x;
        x_temp[i] += DELTA;
        vector<double> fx_delta = computeFx(A, b, x_temp);

        for (int j = 0; j < n; ++j) {
            J[j][i] = (fx_delta[j] - fx[j]) / DELTA;
        }
    }
    return J;
}

vector<double> newtonMethod(const vector<vector<double>>& A, const vector<double>& b, vector<double> x0, int maxIterations = 100) {
    for (int iter = 0; iter < maxIterations; ++iter) {
        vector<double> fx = computeFx(A, b, x0);
        vector<vector<double>> J = computeJacobianNumerically(A, b, x0);

        double norm = 0;
        for (double f : fx) norm += f * f;
        if (sqrt(norm) < EPSILON) break;

        try {
            vector<double> y = gauss(J, fx);
            for (int i = 0; i < x0.size(); ++i) {
                x0[i] -= y[i];
                if (isnan(x0[i]) || isinf(x0[i])) {
                    throw runtime_error("Ошибка: решение ушло в бесконечность.");
                }
            }

            norm = 0;
            for (double yi : y) norm += yi * yi;
            if (sqrt(norm) < EPSILON) {
                break;
            }
        } catch (const runtime_error& e) {
            cerr << "Метод Ньютона не сошелся: " << e.what() << endl;
            exit(1);
        }
    }
    return x0;
}

int main() {
    int n;
    cout << "Введите количество переменных (и уравнений): ";
    cin >> n;

    vector<vector<double>> A(n, vector<double>(n));
    vector<double> b(n);
    vector<double> x0(n, 1.0);

    cout << "Введите коэффициенты матрицы A:" << endl;
    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < n; ++j) {
            cout << "A[" << i + 1 << "][" << j + 1 << "]: ";
            cin >> A[i][j];
        }
    }

    cout << "Введите вектор свободных членов b:" << endl;
    for (int i = 0; i < n; ++i) {
        cout << "b[" << i + 1 << "]: ";
        cin >> b[i];
    }

    try {
        vector<double> solution = newtonMethod(A, b, x0);
        cout << "Решение: " << endl;
        int count = 0;
        for (double sol : solution) {
            if (abs(sol) < EPSILON) sol = 0;
            cout << "x" << count << " = " << sol << endl;
            count++;
        }
        cout << endl;
    } catch (const exception& e) {
        cerr << "Ошибка: " << e.what() << endl;
    }

    return 0;
}