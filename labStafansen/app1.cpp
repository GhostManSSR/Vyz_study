#include <iostream>
#include <cmath>

using namespace std;

double f(double x) {
    return 3 - sqrt(x) - 0.5 * log(x);
}

double steffensen(double a, double b, double tol, int max_iter = 100) {
    double x = (a + b) / 2.0;
    int iter = 0;
    
    while (iter < max_iter) {
        double fx = f(x);
        if (fabs(fx) < tol) return x; 

        double gx = (f(x + fx) - fx) / fx; 
        
        if (fabs(gx) < 1e-12) {
            cout << "Метод не сходится." << endl;
            return x;
        }

        double x1 = x - (fx * fx) / (f(x + fx) - fx);
        
        if (fabs(x1 - x) < tol) return x1; 
        
        x = x1;
        iter++;
    }
    
    cout << "Метод не сошелся за " << max_iter << " итераций." << endl;
    return x;
}

int main() {
    double a, b, tol;
    
    cout << "Введите границы отрезка [a, b]: ";
    cin >> a >> b;
    cout << "Введите точность вычисления: ";
    cin >> tol;
    
    if (f(a) * f(b) > 0) {
        cout << "На данном отрезке нет гарантированного корня (или их несколько)." << endl;
        return 1;
    }

    double root = steffensen(a, b, tol);
    
    cout << "Приближенное значение корня: " << root << endl;
    
    return 0;
}
