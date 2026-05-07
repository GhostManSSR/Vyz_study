package org.example;

public class Result {

    public enum Status {
        OPTIMAL,
        NO_SOLUTION,
        THROWS,
        INFINITE,
        UNBOUNDED
    }

    public Status status;
    public Fraction[] solution;

    public int[] freeVariables;
    public Fraction zValue;

    public Fraction[] secondSolution;

    public static Result noSolution() {
        Result r = new Result();
        r.status = Status.NO_SOLUTION;
        return r;
    }

    private String vec(Fraction[] v) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < v.length; i++) {
            sb.append(v[i]);
            if (i != v.length - 1) sb.append("; ");
        }
        sb.append(")");
        return sb.toString();
    }

    public static Result throwsSolution() {
        Result r = new Result();
        r.status = Status.THROWS;
        return r;
    }

    public static Result optimal(Fraction[] sol) {
        Result r = new Result();
        r.status = Status.OPTIMAL;
        r.solution = sol;
        return r;
    }

    public static Result infinite(Fraction[] x1, Fraction[] x2, int[] freeVars, Fraction z) {
        Result r = new Result();
        r.status = Status.INFINITE;
        r.solution = x1;
        r.secondSolution = x2;
        r.freeVariables = freeVars;
        r.zValue = z;
        return r;
    }

    public static Result unbounded() {
        Result r = new Result();
        r.status = Status.UNBOUNDED;
        return r;
    }

    private Fraction[] calcLambda(Fraction lambda) {
        Fraction[] res = new Fraction[solution.length];
        for (int i = 0; i < solution.length; i++) {
            res[i] = solution[i].add(lambda.mul(secondSolution[i].sub(solution[i])));
        }
        return res;
    }

    public void print() {

        switch (status) {

            case OPTIMAL -> {

                System.out.println("\n=== РЕЗУЛЬТАТ ===");

                System.out.println("Максимальное значение Z = " + zValue);

                System.out.println("Система имеет единственное решение");
                System.out.print("Zmax(");

                for (int i = 0; i < solution.length; i++) {
                    System.out.print(solution[i]);
                    if (i != solution.length - 1) System.out.print(", ");
                }

                System.out.print(") = " + zValue);
                System.out.println();
            }

            case THROWS -> {
                System.out.println("ЗЛП не имеет допустимых решений");
            }

            case NO_SOLUTION -> {
                System.out.println("Решений нет (несовместность)");
            }

            case UNBOUNDED -> {
                System.out.println("\nФункция не ограничена");
            }

            case INFINITE -> {

                System.out.println("\n=== БЕСКОНЕЧНО МНОГО РЕШЕНИЙ ===\n");

                String x1 = vec(solution);
                String x2 = vec(secondSolution);

                System.out.println("X1 = " + x1);
                System.out.println("X2 = " + x2);

                System.out.println("\nX* = (1 - lambda) * X1 + lambda * X2");

                StringBuilder expanded = new StringBuilder("X* = (");

                for (int i = 0; i < solution.length; i++) {

                    Fraction a = solution[i];
                    Fraction b = secondSolution[i];
                    Fraction d = b.sub(a);

                    if (a.isZero() && d.isZero()) {
                        expanded.append("0");
                    } else if (d.isZero()) {
                        expanded.append(a);
                    } else if (a.isZero()) {
                        expanded.append("lambda*(").append(d).append(")");
                    } else {
                        expanded.append(a)
                                .append(" + lambda*(")
                                .append(d)
                                .append(")");
                    }

                    if (i != solution.length - 1) expanded.append("; ");
                }

                expanded.append(")");

                System.out.println(expanded);

                System.out.println("\nZmax = " + zValue + ", где 0 ≤ lambda ≤ 1");
            }
        }
    }

    public static Result optimal(Fraction[] sol, Fraction z) {
        Result r = new Result();
        r.status = Status.OPTIMAL;
        r.solution = sol;
        r.zValue = z;
        return r;
    }

    private void printVector(Fraction[] v) {
        for (int i = 0; i < v.length; i++) {
            System.out.println("x" + (i + 1) + " = " + v[i]);
        }
    }
}