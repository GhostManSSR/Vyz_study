#include <iostream>
#include <iomanip>
#include <cmath>

#define EPSILON 0.001  // Точность
using namespace std;


bool render(float* row, int m) {
    for (int j = 0; j < m; j++) {
        if (row[j] != 0) {
            return false;
        }
    }
    return true;
}

double determinant(double** matrix, int n) {
    double det = 1;
    for (int i = 0; i < n; i++) {
        if (matrix[i][i] == 0) return 0; 
        det *= matrix[i][i];
    }
    return det;
}

bool opred(double** matrix, int n) {
    return determinant(matrix, n) == 0;
}

int first_chlen(double** matrix) {
    return matrix[0][0] != 0;
}

bool isLinearlyDependent(double** matrix, int n) {
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

bool isDiagonallyDominant(double** matrix, int n) {
    for (int i = 0; i < n; i++) {
        double sum = 0;
        for (int j = 0; j < n; j++) {
            if (i != j) sum += fabs(matrix[i][j]);
        }
        if (fabs(matrix[i][i]) < sum) return false;
    }
    return true;
}

int seidelMethod(double** A, double* b, double* x, int n) {
    double* prev_x = new double[n];
    bool stop;
    int iter = 0;

    int maxiter = 1000;

              if (!isDiagonallyDominant(A, n)) {
                cout << "Исключение" << endl;
                cout << "Матрица не является диагонально преобладающей, возможна расходимость метода" << endl;
                return 1;
            }
    
    do {
        stop = true;
        for (int i = 0; i < n; i++) {
            prev_x[i] = x[i];
            cout << prev_x[i] << ":" << x[i] << endl;
            double sum = 0;
            for (int j = 0; j < n; j++) {
                if (i != j)
                    sum += A[i][j] * x[j];
            }
            x[i] = (b[i] - sum) / A[i][i];
        }
        
        for (int i = 0; i < n; i++) {
            if (fabs(x[i] - prev_x[i]) > EPSILON)
                stop = false;
        }
        iter++;

        cout << "Итерация " << iter + 1 << ": ";
        for (int i = 0; i < n; i++) {
            cout << "x[" << i + 1 << "] = " << x[i] << " ";
        }
        cout << endl;
        
        if(iter > maxiter){
            cout << "Решение методом Зейделя не найдено за " << iter << " итераций." << endl;
            break;
        }
    } while (!stop);
    return 0;
    delete[] prev_x;
    cout << "Решение методом Зейделя найдено за " << iter << " итераций." << endl;
}

#include <vector>

double spectralRadius(double** A, int n, double tau) {
    vector<double> x(n, 1.0), y(n);
    double norm = 0, lambda_old = 0, lambda_new = 0;
    
    for (int iter = 0; iter < 100; iter++) { 
        for (int i = 0; i < n; i++) {
            y[i] = (1 - tau * A[i][i]) * x[i];
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    y[i] -= tau * A[i][j] * x[j];
                }
            }
        }

        norm = 0;
        for (int i = 0; i < n; i++) {
            norm += y[i] * y[i];
        }
        norm = sqrt(norm);

        for (int i = 0; i < n; i++) {
            x[i] = y[i] / norm;
        }

        lambda_new = 0;
        for (int i = 0; i < n; i++) {
            lambda_new += x[i] * y[i];
        }

        if (fabs(lambda_new - lambda_old) < 1e-6) break;
        lambda_old = lambda_new;
    }

    return fabs(lambda_new);
}


void mpiMethod(double** A, double* b, double* x, int n) {
    double* prev_x = new double[n];
    bool stop;
    double tau = 0.1;
    int iter = 0;

    int maxiter = 1000;

    if (spectralRadius(A, n, tau) >= 1) {
        cout << "Исключение" << endl;
        cout << "Метод простых итераций может не сходиться (спектральный радиус >= 1)" << endl;
        return;
    }

    
    do {
        stop = true;
        for (int i = 0; i < n; i++) {
            prev_x[i] = x[i];
        }
        
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int j = 0; j < n; j++) {
                sum += A[i][j] * prev_x[j];
            }
            x[i] = prev_x[i] + tau * (b[i] - sum);
        }

        cout << "Итерация " << iter + 1 << ": ";
        for (int i = 0; i < n; i++) {
            cout << "x[" << i + 1 << "] = " << x[i] << " ";
        }
        cout << endl;
        
        for (int i = 0; i < n; i++) {
            if (fabs(x[i] - prev_x[i]) > EPSILON)
                stop = false;
        }
        iter++;
        if(iter > maxiter){
            cout << "Решение методом простых итераций не найдено за " << iter << " итераций." << endl;
            break;
        }
    } while (!stop);
    
    delete[] prev_x;
    cout << "Решение методом простых итераций найдено за " << iter << " итераций." << endl;
}

int main() {
    setlocale(LC_ALL, "ru");
    while(true){
        int chose;
        cout << "1. Ввести пример для решения" << endl;
        cout << "2. Выход" << endl;
        cin >> chose;
        if(chose == 1){
            int i, j, n;
            cout << "Количество уравнений и переменных (N): ";
            cin >> n;

            double** matrix = new double* [n];
            for (i = 0; i < n; i++)
                matrix[i] = new double[n];

            double* b = new double[n];
            double* solution = new double[n];
            
            cout << "Введите коэффициенты основной матрицы:" << endl;
            for (i = 0; i < n; i++) {
                for (j = 0; j < n; j++) {
                    cout << " Элемент [" << i + 1 << " , " << j + 1 << "]: ";
                    cin >> matrix[i][j];
                }
            }
            
            cout << "Введите правую часть системы:" << endl;
            for (i = 0; i < n; i++) {
                cout << " Элемент [" << i + 1 << "]: ";
                cin >> b[i];
            }
    
            int method;
            cout << "Матрица: " << endl;
            for (i = 0; i < n; i++) {
                for (j = 0; j < n; j++)
                    cout << matrix[i][j] << " ";
                cout << " | " << b[i] << endl;
            }
            cout << endl;

            if (!first_chlen(matrix)) {
                cout << "Исключение" << endl;
                cout << "Член матрицы matrix[0][0] == 0, решение невозможно" << endl;
                continue;
            }
            
            if (opred(matrix, n)) {
                cout << "Исключение" << endl;
                cout << "Определитель равен 0, система не имеет единственного решения" << endl;
                continue;
            }
            
            if (isLinearlyDependent(matrix, n)) {
                cout << "Исключение" << endl;
                cout << "Строки линейно зависимы, система не имеет единственного решения" << endl;
                continue;
            }

            // if (!isDiagonallyDominant(matrix, n)) {
            //     cout << "Исключение" << endl;
            //     cout << "Матрица не является диагонально преобладающей, возможна расходимость метода" << endl;
            //     continue;
            // }

            cout << "Выберите метод решения (1 - Зейделя, 2 - МПИ): ";
            cin >> method;
    
            if (method == 1) {
                if(seidelMethod(matrix, b, solution, n)==1){
                    return 0;
                }
            } else {
                mpiMethod(matrix, b, solution, n);
            }
    
            cout << "Решение системы:" << endl;
            for (int i = 0; i < n; i++) {
                cout << "x[" << i + 1 << "] = " << solution[i] << endl;
            }
    
            for (int i = 0; i < n; i++) {
                delete[] matrix[i];
            }
            delete[] matrix;
            delete[] b;
            delete[] solution;
        }
        if(chose == 2){
            break;
        }
    }
    return 0;
}