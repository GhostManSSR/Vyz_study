#include <iostream>
#include <math.h>
#include <locale.h>
#include <stdlib.h>
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

void backSubstitution(double** matrix, double* b, double* solution, int n) {
    for (int i = n - 1; i >= 0; i--) {
        solution[i] = b[i]; // проходимся по всем решением с конца с уже известного b[i]
        for (int j = i + 1; j < n; j++) {
            solution[i] -= matrix[i][j] * solution[j]; // Вычитаем уже найденные переменные
        }
        solution[i] /= matrix[i][i]; // Делим на коэффициент перед x_i
    }
}

bool gaussianElimination(double** matrix, double* b, double* solution, int n) {
    for (int i = 0; i < n; i++) {
        if (matrix[i][i] == 0) {
            cout << "Нулевой элемент на диагонали, невозможно продолжить вычисления, решений бесконечно много" << endl;
            return false;
        }
        double pivot = matrix[i][i];
        for (int j = i; j < n; j++)
            matrix[i][j] /= pivot; 
        b[i] /= pivot;
        for (int j = i + 1; j < n; j++) {
            double factor = matrix[j][i]; 
            for (int k = i; k < n; k++)
                matrix[j][k] -= factor * matrix[i][k]; 
            b[j] -= factor * b[i];
        }
    }
    backSubstitution(matrix, b, solution, n);
    return true;
}

bool advancedGaussianElimination(double** matrix, double* b, double* solution, int n) {
    for (int i = 0; i < n; i++) {
        int maxRow = i;
        for (int k = i + 1; k < n; k++) {
            if (fabs(matrix[k][i]) > fabs(matrix[maxRow][i])) {
                maxRow = k;
            }
        }
        swap(matrix[i], matrix[maxRow]);
        swap(b[i], b[maxRow]);

        if (matrix[i][i] == 0) {
            cout << "Нулевой элемент на диагонали, невозможно продолжить вычисления решений бесконечно много" << endl;
            return false;
        }
        
        double pivot = matrix[i][i];
        for (int j = i; j < n; j++)
            matrix[i][j] /= pivot;
        b[i] /= pivot;
        for (int j = i + 1; j < n; j++) {
            double factor = matrix[j][i];
            for (int k = i; k < n; k++)
                matrix[j][k] -= factor * matrix[i][k];
            b[j] -= factor * b[i];
        }
    }
    backSubstitution(matrix, b, solution, n);
    return true;
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
            
            cout << "Применение стандартного метода Гаусса" << endl;
            if(gaussianElimination(matrix, b, solution, n)){
                for (i = 0; i < n; i++)
                cout << "x" << i + 1 << " = " << solution[i] << " ";
                cout << endl;
            }
            else{
                continue;
            }
            
            fill(solution, solution + n, 0); 

            cout << "Применение продвинутого метода Гаусса" << endl;
            if(advancedGaussianElimination(matrix, b, solution, n)){
                for (i = 0; i < n; i++)
                cout << "x" << i + 1 << " = " << solution[i] << " ";
                cout << endl;
            }
            else{
                continue;
            }
            
            for (i = 0; i < n; i++)
                delete[] matrix[i];
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
