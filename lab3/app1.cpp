#include <stdio.h>
#include <math.h>

#define EPSILON 1e-6
#define MAX_DEGREE 10
#define SEARCH_RANGE 1000 

float coefficients[MAX_DEGREE + 1];
int degree;

float equation(float x) {
    float result = 0;
    for (int i = 0; i <= degree; i++) {
        result += coefficients[i] * pow(x, degree - i);
    }
    return result;
}

float derivative(float x) {
    float result = 0;
    for (int i = 0; i < degree; i++) {
        result += (degree - i) * coefficients[i] * pow(x, degree - i - 1);
    }
    return result;
}

void find_interval(float *a, float *b) {
    float step = 1.0;
    *a = -SEARCH_RANGE;
    *b = SEARCH_RANGE;
    for (float x = -SEARCH_RANGE; x < SEARCH_RANGE; x += step) {
        if (equation(x) * equation(x + step) < 0) {
            *a = x;
            *b = x + step;
            return;
        }
    }
    printf("Не удалось найти подходящий интервал, используйте другой диапазон!\n");
}

float bisection(float a, float b) {
    if (equation(a) * equation(b) >= 0) {
        printf("Неверный интервал!\n");
        return NULL;
    }
    
    float c;
    while ((b - a) / 2 >= EPSILON) {
        c = (a + b) / 2;
        if (equation(c) == 0.0)
            break;
        else if (equation(a) * equation(c) < 0)
            b = c;
        else
            a = c;
    }
    return c;
}

float newton(float x0) {
    float x = x0;
    while (fabs(equation(x)) > EPSILON) {
        x = x - equation(x) / derivative(x);
    }
    return x;
}

int main() {
    printf("Введите степень уравнения: ");
    scanf("%d", &degree);
    
    printf("Введите коэффициенты уравнения от старшего к младшему: ");
    for (int i = 0; i <= degree; i++) {
        printf("Значение %d = ", i + 1);
        scanf("%f", &coefficients[i]);
        printf("\n");
    }
    
    float a, b, x0;
    find_interval(&a, &b);
    printf("Найденный интервал: [%.2f, %.2f]\n", a, b);
    
    float root_bisec = bisection(a, b);
    printf("Корень методом бисекции: %.6f\n", root_bisec);
    
    printf("Введите начальное приближение для метода Ньютона: ");
    scanf("%f", &x0);
    float root_newton = newton(x0);
    printf("Корень методом Ньютона: %.6f\n", root_newton);
    
    return 0;
}
