#include <iostream>
#include <vector>
#include <cmath>
#include <iomanip>

const double EPSILON = 1e-6;
const double h_start = 0.1;
const double X_START = 0.0;
const double X_END = 1.0;

struct State {
    double y, dy;
};

State f(double x, State s) {
    double ddy = exp(2 * x) + (s.y + s.dy) / 2;
    return {s.dy, ddy};
}

State rk2_step(double x, State s, double h) {
    State k1 = f(x, s);
    State k2 = f(x + h, {s.y + h * k1.y, s.dy + h * k1.dy});
    return {s.y + h * (k1.y + k2.y) / 2,
            s.dy + h * (k1.dy + k2.dy) / 2};
}


State rk4_step(double x, State s, double h) {
    State k1 = f(x, s);
    State k2 = f(x + h / 2, {s.y + h * k1.y / 2, s.dy + h * k1.dy / 2});
    State k3 = f(x + h / 2, {s.y + h * k2.y / 2, s.dy + h * k2.dy / 2});
    State k4 = f(x + h, {s.y + h * k3.y, s.dy + h * k3.dy});
    
    return {s.y + h * (k1.y + 2 * k2.y + 2 * k3.y + k4.y) / 6,
            s.dy + h * (k1.dy + 2 * k2.dy + 2 * k3.dy + k4.dy) / 6};
}

std::vector<std::pair<double, double>> solve_rk2() {
    std::vector<std::pair<double, double>> result;
    double x = X_START;
    State s = {1.0, 0.0}; 
    double h = h_start;
    
    while (x <= X_END) {
        result.push_back({x, s.y});
        s = rk2_step(x, s, h);
        x += h;
    }
    return result;
}

std::vector<std::pair<double, double>> solve_rk4() {
    std::vector<std::pair<double, double>> result;
    double x = X_START;
    State s = {1.0, 0.0}; 
    double h = h_start;
    
    while (x <= X_END) {
        result.push_back({x, s.y});
        s = rk4_step(x, s, h);
        x += h;
    }
    return result;
}

double newton_interpolation(const std::vector<std::pair<double, double>>& points, double x) {
    int n = points.size();
    std::vector<double> coef(n);
    
    for (int i = 0; i < n; i++) {
        coef[i] = points[i].second;
    }
    
    for (int j = 1; j < n; j++) {
        for (int i = n - 1; i >= j; i--) {
            coef[i] = (coef[i] - coef[i - 1]) / (points[i].first - points[i - j].first);
        }
    }
    
    double result = coef[n - 1];
    for (int i = n - 2; i >= 0; i--) {
        result = result * (x - points[i].first) + coef[i];
    }
    return result;
}

int main() {
    std::vector<std::pair<double, double>> solution_rk2 = solve_rk2();
    std::vector<std::pair<double, double>> solution_rk4 = solve_rk4();
    std::vector<std::pair<double, double>> interp_points_rk2 = {{0.0, 0.0}, {0.2, 0.0}, {0.4, 0.0}, {0.6, 0.0}, {0.8, 0.0}, {1.0, 0.0}};
    std::vector<std::pair<double, double>> interp_points_rk4 = interp_points_rk2;
    
    for (auto& p : interp_points_rk2) {
        for (const auto& s : solution_rk2) {
            if (fabs(s.first - p.first) < EPSILON) {
                p.second = s.second;
                break;
            }
        }
    }
    
    for (auto& p : interp_points_rk4) {
        for (const auto& s : solution_rk4) {
            if (fabs(s.first - p.first) < EPSILON) {
                p.second = s.second;
                break;
            }
        }
    }
    std::cout << "y(1) по RK4: " << solution_rk4.back().second << "\n";
    std::cout << std::fixed << std::setprecision(6);
    std::cout << "y(1) по RK2: " << solution_rk2.back().second << "\n";
    std::cout << std::fixed << std::setprecision(6);
    std::cout << "x\t\ty_RK2\t\ty_RK4\n";
    for (double x = 0.0; x <= 1.0; x += 0.1) {
        double y_interp_rk2 = newton_interpolation(interp_points_rk2, x);
        double y_interp_rk4 = newton_interpolation(interp_points_rk4, x);
        std::cout << x << "\t" << y_interp_rk2 << "\t" << y_interp_rk4 << "\n";
    }
    return 0;
}
