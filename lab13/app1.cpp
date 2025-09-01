#include <iostream>
#include <iomanip>
#include <cmath>

using namespace std;

//13 лаба ДУ методом Эйлера

double f(double x, double y) {
    return x + y;
}

void eulerMethod(double x0, double y0, double h, double x_end) {
    double x = x0;
    double y = y0;

    cout << "Результаты вычислений:\n";
    cout << "x\t\ty\n";
    cout << fixed << setprecision(5);

    while (x < x_end) {
        cout << x << "\t" << y << "\n";
        y += h * f(x, y); 
        x += h;          
    }
}

bool isValidInput(double value) {
    return !cin.fail(); 
}

int main() {
    double x0, y0, h, x_end;

    cout << "Введите начальное значение x (x0): ";
    cin >> x0;
    if (!isValidInput(x0)) {
        cout << "Ошибка ввода! Попробуйте еще раз.\n";
        return 1;
    }

    cout << "Введите начальное значение y (y0): ";
    cin >> y0;
    if (!isValidInput(y0)) {
        cout << "Ошибка ввода! Попробуйте еще раз.\n";
        return 1;
    }

    cout << "Введите шаг h: ";
    cin >> h;
    if (!isValidInput(h) && h <= 0) {
        cout << "Ошибка ввода! Шаг должен быть положительным.\n";
        return 1;
    }

    cout << "Введите конечное значение x: ";
    cin >> x_end;
    if (!isValidInput(x_end) &&  x_end <= x0) {
        cout << "Ошибка ввода! Конечное значение должно быть больше начального.\n";
        return 1;
    }

    eulerMethod(x0, y0, h, x_end);

    return 0;
}