#include <iostream>
#include <iomanip>
#include <cmath>
#include <vector>

#define EPSILON 1e-8  // Точность
using namespace std;

double determinant(vector<vector<double>>& matrix, int n) {
    vector<vector<double>> temp = matrix;
    double det = 1;
    for (int i = 0; i < n; i++) {
        if (temp[i][i] == 0) return 0;
        det *= temp[i][i];
    }
    return det;
}

bool isSingular(vector<vector<double>>& matrix, int n) {
    return determinant(matrix, n) == 0;
}

bool isLinearlyDependent(vector<vector<double>>& matrix, int n) {
    for (int i = 0; i < n - 1; i++) {
        for (int j = i + 1; j < n; j++) {
            if (matrix[i][0] == 0) continue;
            double ratio = matrix[j][0] / matrix[i][0];
            bool dependent = true;
            for (int k = 1; k < n; k++) {
                if (fabs(matrix[j][k] - ratio * matrix[i][k]) > 1e-6) {
                    dependent = false;
                    break;
                }
            }
            if (dependent) return true;
        }
    }
    return false;
}

bool isDiagonallyDominant(vector<vector<double>>& A, int n) {
    for (int i = 0; i < n; i++) {
        double sum = 0;
        for (int j = 0; j < n; j++) {
            if (i != j) sum += fabs(A[i][j]);
        }
        if (fabs(A[i][i]) < sum) return false;
    }
    return true;
}

int seidelMethod(vector<vector<double>>& A, vector<double>& b, vector<double>& x, int n) {
    vector<double> prev_x(n);
    bool stop;
    int iter = 0;
    const int maxiter = 1000;

    if (!isDiagonallyDominant(A, n)) {
        cout << "Матрица не является диагонально преобладающей, возможна расходимость." << endl;
    }

    do {
        stop = true;
        for (int i = 0; i < n; i++) {
            prev_x[i] = x[i];
            double sum = 0;
            for (int j = 0; j < n; j++) {
                if (i != j) sum += A[i][j] * x[j];
            }
            x[i] = (b[i] - sum) / A[i][i];
        }

        for (int i = 0; i < n; i++) {
            if (fabs(x[i] - prev_x[i]) > EPSILON) {
                stop = false;
                break;
            }
        }
        iter++;

        cout << "Итерация " << iter << ": ";
        for (int i = 0; i < n; i++) {
            cout << "x[" << i + 1 << "] = " << x[i] << " ";
        }
        cout << endl;

        if (iter > maxiter) {
            cout << "Метод Зейделя не сошёлся за " << iter << " итераций." << endl;
            return 1;
        }
    } while (!stop);

    cout << "Метод Зейделя сошёлся за " << iter << " итераций." << endl;
    return 0;
}

int main() {
    setlocale(LC_ALL, "ru");
    while (true) {
        int choice;
        cout << "1. Ввести пример для решения" << endl;
        cout << "2. Выход" << endl;
        cin >> choice;
        if (choice == 1) {
            try {
                int n;
                cout << "Количество уравнений и переменных (N): ";
                cin >> n;

                vector<vector<double>> matrix(n, vector<double>(n));
                vector<double> b(n), solution(n, 0);
                
                cout << "Введите коэффициенты основной матрицы:" << endl;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        cout << " Элемент [" << i + 1 << " , " << j + 1 << "]: ";
                        cin >> matrix[i][j];
                    }
                }
                
                cout << "Введите правую часть системы:" << endl;
                for (int i = 0; i < n; i++) {
                    cout << " Элемент [" << i + 1 << "]: ";
                    cin >> b[i];
                }
        
                cout << "Матрица: " << endl;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++)
                        cout << matrix[i][j] << " ";
                    cout << " | " << b[i] << endl;
                }
                cout << endl;

                if (matrix[0][0] == 0) {
                    throw runtime_error("Исключение: matrix[0][0] == 0, решение невозможно");
                }
                
                if (isSingular(matrix, n)) {
                    throw runtime_error("Исключение: Определитель равен 0, система не имеет единственного решения");
                }
                
                if (isLinearlyDependent(matrix, n)) {
                    throw runtime_error("Исключение: Строки линейно зависимы, система не имеет единственного решения");
                }

                seidelMethod(matrix, b, solution, n);
        
                cout << "Решение системы:" << endl;
                for (int i = 0; i < n; i++) {
                    cout << "x[" << i + 1 << "] = " << solution[i] << endl;
                }
            } catch (const exception& e) {
                cout << e.what() << endl;
                continue;
            }
        }
        if (choice == 2) {
            break;
        }
    }
    return 0;
}