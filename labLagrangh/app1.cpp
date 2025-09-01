#include <iostream>
#include <vector>
#include <fstream>
#include <sstream>

using namespace std;

double lagrangeInterpolation(const vector<double>& x, const vector<double>& y, double value) {
    int n = x.size();
    double result = 0.0;
    
    for (int i = 0; i < n; i++) {
        double term = y[i];
        for (int j = 0; j < n; j++) {
            if (i != j) {
                term *= (value - x[j]) / (x[i] - x[j]);
            }
        }
        result += term;
    }
    
    return result;
}

int main() {
    ifstream inputFile("in.txt");
    if (!inputFile) {
        cerr << "Ошибка открытия файла!" << endl;
        return 1;
    }
    
    vector<double> x, y;
    string line;
    
    if (getline(inputFile, line)) {
        stringstream ss(line);
        double value;
        while (ss >> value) {
            x.push_back(value);
        }
    }
    
    if (getline(inputFile, line)) {
        stringstream ss(line);
        double value;
        while (ss >> value) {
            y.push_back(value);
        }
    }
    
    if (x.size() != y.size() || x.empty()) {
        cerr << "Ошибка в формате входных данных!" << endl;
        return 1;
    }
    
    double value;
    cout << "Введите значение, в котором нужно вычислить полином: ";
    cin >> value;
    
    double result = lagrangeInterpolation(x, y, value);
    
    cout << "Значение полинома в точке " << value << " равно " << result << endl;
    
    return 0;
}