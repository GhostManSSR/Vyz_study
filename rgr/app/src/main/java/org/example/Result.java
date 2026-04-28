package org.example;

public class Result {

    public enum Status {
        OPTIMAL,
        NO_SOLUTION,
        INFINITE
    }

    public Status status;
    public Fraction[] solution;

    public int[] freeVariables;

    public static Result noSolution() {
        Result r = new Result();
        r.status = Status.NO_SOLUTION;
        return r;
    }

    public static Result optimal(Fraction[] sol) {
        Result r = new Result();
        r.status = Status.OPTIMAL;
        r.solution = sol;
        return r;
    }

    public static Result infinite(Fraction[] sol, int[] freeVars) {
        Result r = new Result();
        r.status = Status.INFINITE;
        r.solution = sol;
        r.freeVariables = freeVars;
        return r;
    }

    public void print() {

        System.out.println("\n=== Результат ===");
        System.out.println("Статус: " + status);

        switch (status) {

            case OPTIMAL -> {
                System.out.println("Единственное оптимальное решение:");
                for (int i = 0; i < solution.length; i++) {
                    System.out.println("x" + (i + 1) + " = " + solution[i]);
                }
            }

            case INFINITE -> {
                System.out.println("Бесконечно много оптимальных решений:");
                for (int i = 0; i < solution.length; i++) {
                    System.out.println("x" + (i + 1) + " = " + solution[i]);
                }

                System.out.print("Свободные переменные: ");
                if (freeVariables != null) {
                    for (int v : freeVariables) {
                        System.out.print("x" + (v + 1) + " ");
                    }
                }
                System.out.println();
            }

            case NO_SOLUTION -> {
                System.out.println("Решений нет");
            }
        }
    }
}
