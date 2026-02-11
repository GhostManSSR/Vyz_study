import math

class SimpleFraction:
    def __init__(self, num=0, den=1):  # Исправлено: __init__
        if den == 0:
            raise ValueError("Знаменатель не может быть нулем")
        common = math.gcd(int(num), int(den))
        self.num = int(num) // common
        self.den = int(den) // common
        if self.den < 0:
            self.num, self.den = -self.num, -self.den

    def __abs__(self):  # ДОБАВЛЕНО для abs()
        return SimpleFraction(abs(self.num), self.den)

    def __eq__(self, other):  # ДОБАВЛЕНО для ==
        if not isinstance(other, SimpleFraction):
            return False
        return self.num * other.den == other.num * self.den

    def add(self, other):
        return SimpleFraction(self.num * other.den + other.num * self.den, self.den * other.den)

    def sub(self, other):
        return SimpleFraction(self.num * other.den - other.num * self.den, self.den * other.den)

    def mul(self, other):
        return SimpleFraction(self.num * other.num, self.den * other.den)

    def truediv(self, other):
        if other.num == 0: 
            raise ZeroDivisionError("Деление на 0")
        return SimpleFraction(self.num * other.den, self.den * other.num)

    def abs(self):
        return SimpleFraction(abs(self.num), self.den)

    def lt(self, other):
        return self.num * other.den < other.num * self.den

    def eq(self, other):
        return self.num == other.num and self.den == other.den

    def __repr__(self):  # Исправлено: __repr__
        return f"{self.num}/{self.den}" if self.den != 1 else f"{self.num}"

def print_matrix(matrix, title):
    print(f"\n{title}")
    for row in matrix:
        print("\t".join(f"{str(x):>8}" for x in row))

def solve_jordan_gauss(matrix):
    rows = len(matrix)
    cols = len(matrix[0]) - 1 

    pivot_row = 0
    for j in range(cols):
        if pivot_row >= rows: 
            break

        max_idx = pivot_row
        for i in range(pivot_row + 1, rows):
            # ИСПРАВЛЕНО: используем методы класса вместо встроенных функций
            abs_i = abs(matrix[i][j])
            abs_max = abs(matrix[max_idx][j])
            if (abs_i > abs_max or abs_i == abs_max):
                max_idx = i

        matrix[pivot_row], matrix[max_idx] = matrix[max_idx], matrix[pivot_row]

        if matrix[pivot_row][j] == SimpleFraction(0,1):
            continue

        pivot = matrix[pivot_row][j]
        # ИСПРАВЛЕНО: используем truediv
        matrix[pivot_row] = [x.truediv(pivot) for x in matrix[pivot_row]]

        for i in range(rows):
            if i != pivot_row:
                factor = matrix[i][j]
                # ИСПРАВЛЕНО: используем методы класса
                matrix[i] = [matrix[i][k].sub(factor.mul(matrix[pivot_row][k])) 
                           for k in range(cols + 1)]

        pivot_row += 1
        print_matrix(matrix, f"После обработки столбца {j + 1}")

    solution = [None] * cols
    rank = 0
    for i in range(rows):
        is_zero_row = all(matrix[i][k].eq(SimpleFraction(0,1)) for k in range(cols))
        if is_zero_row and not matrix[i][cols].eq(SimpleFraction(0,1)):
            return "СИСТЕМА НЕ ИМЕЕТ РЕШЕНИЯ"
        if not is_zero_row:
            rank += 1
            for k in range(cols):
                if matrix[i][k].eq(SimpleFraction(1,1)):
                    solution[k] = matrix[i][cols]
                    break

    if rank < cols:
        print("СИСТЕМА ИМЕЕТ БЕСКОНЕЧНО МНОГО РЕШЕНИЙ")
        pivot_cols = []
        pivot_row_for_col = {}
        for i in range(rows):
            for k in range(cols):
                if matrix[i][k].eq(SimpleFraction(1,1)):
                    pivot_cols.append(k)
                    pivot_row_for_col[k] = i
                    break

        free_cols = [j for j in range(cols) if j not in pivot_cols]

        solutions = []
        for i in range(cols):
            if i in free_cols:
                solutions.append(f"x{i+1} = x{i+1}")
            else:
                r = pivot_row_for_col[i]
                const = matrix[r][cols]
                parts = []
                if not const.eq(SimpleFraction(0,1)):
                    parts.append(str(const))

                # ИСПРАВЛЕНА ОТСТУПОВКА
                for j in free_cols:
                    coeff = matrix[r][j]
                    if coeff.eq(SimpleFraction(0,1)):
                        continue
                    coeff_abs = abs(coeff)
                    if coeff.num < 0:
                        if coeff_abs.eq(SimpleFraction(1,1)):
                            parts.append(f"- x{j+1}")
                        else:
                            parts.append(f"- {coeff_abs}x{j+1}")
                    else:
                        if coeff_abs.eq(SimpleFraction(1,1)):
                            parts.append(f"+ x{j+1}")
                        else:
                            parts.append(f"+ {coeff_abs}x{j+1}")

                if not parts:
                    expr = "0"
                else:
                    expr = parts[0]
                    for p in parts[1:]:
                        expr += f" {p}"
                    expr = expr.lstrip('+ ').strip()

                solutions.append(f"x{i+1} = {expr}")

        for line in solutions:
            print(line)
        return "МНОЖЕСТВО РЕШЕНИЙ"

    return f"ЕДИНСТВЕННОЕ РЕШЕНИЕ: {solution}"

try:
    with open('input.txt', 'r') as f:
        input_matrix = [[SimpleFraction(val) for val in line.split()] for line in f if line.strip()]

    print_matrix(input_matrix, "ИСХОДНАЯ МАТРИЦА")
    result = solve_jordan_gauss(input_matrix)
    print(f"\nИТОГ: {result}")

except FileNotFoundError:
    print("Ошибка: Файл 'input.txt' не найден.")
except Exception as e:
    print(f"Произошла ошибка: {e}")
