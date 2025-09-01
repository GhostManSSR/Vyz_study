#include <iostream>
#include <vector>

using namespace std;

double lagrangeInterpolation(const vector<double>& x, const vector<double>& y, double point) {
    int n = x.size();
    double result = 0.0;

    for (int i = 0; i < n; ++i) {
        double term = y[i];
        for (int j = 0; j < n; ++j) {
            if (i != j) {
                term *= (point - x[j]) / (x[i] - x[j]);
            }
        }
        result += term;
    }

    return result;
}

int main() {
    vector<double> x = {2.0, 3.0, 4.0, 5.0};
    vector<double> y = {1.4142, 1.7321, 2.0, 2.2361};
    double point = 2.56;

    double interpolatedValue = lagrangeInterpolation(x, y, point);
    cout << "Интерполированное значение в точке " << point << ": " << interpolatedValue << endl;

    return 0;
}