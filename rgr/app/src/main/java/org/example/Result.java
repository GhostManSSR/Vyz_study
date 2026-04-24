package org.example;

public class Result {

    public enum Status {
        OPTIMAL,
        NO_SOLUTION,
        INFINITE
    }

    public Status status;
    public Fraction[] solution;

    public static Result noSolution() {
        Result r = new Result();
        r.status = Status.NO_SOLUTION;
        return r;
    }

    public void print() {
        System.out.println("\n=== RESULT ===");
        System.out.println("Status: " + status);

        if (status == Status.OPTIMAL) {
            System.out.println("Optimal solution:");
            for (int i = 0; i < solution.length; i++) {
                System.out.println("x" + (i + 1) + " = " + solution[i]);
            }
        } else if (status == Status.NO_SOLUTION) {
            System.out.println("No feasible solution.");
        } else {
            System.out.println("Infinite solutions.");
        }
    }
}
