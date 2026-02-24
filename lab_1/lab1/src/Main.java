import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        try {
            SimpleDrobi[][] matrix = Parser.readMatrixFromFile("matrix.txt");

            System.out.println("Матрица дробей");
            MatrixUtils.printMatrix(matrix);

            SimpleDrobi[] solution = JordanGauss.solver(matrix);
            JordanGauss.printSolution(solution);

        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }


    }
}