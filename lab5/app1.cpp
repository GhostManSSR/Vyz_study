#include <iostream>
using namespace std;

struct Data { double x, y; };

double interpolate(Data f[], int n, double xi) {
 double result = 0;
 for (int i = 0; i < n; i++) {
  double term = f[i].y;
  for (int j = 0; j < n; j++) {
   if (j != i) term *= (xi - f[j].x) / (f[i].x - f[j].x);
  }
  result += term;
 }
 return result;
}

int main() {
 //Количество исходных точек:
 const int n = 4;
 //Исходные значения (x,y):
 Data f[n] = { 
  { 0, 2 },
  { 1, 3 },
  { 2, 12 },
  { 5, 147 } 
 }; 
 double x1 = 0,
        x2 = 5,
        dx = 0.125;
 cout.width(20); cout << "X"; cout.width(20); cout << "Y" << endl;
 for (double x = x1; x <= x2; x += dx) {
  double y = interpolate(f, n, x);
  cout.width(20); cout.precision(14); cout << x;
  cout.width(20); cout.precision(14); cout << y << endl;
 }

 return 0;
}