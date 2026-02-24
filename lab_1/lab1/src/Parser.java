import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {

    public static SimpleDrobi[][] readMatrixFromFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);

        List<String[]> rowsList = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                String[] values = line.split("\\s+");
                rowsList.add(values);
            }
        }

        scanner.close();

        int rows = rowsList.size();
        if (rows == 0) {
            return new SimpleDrobi[0][0];
        }

        int cols = rowsList.get(0).length;

        for (String[] row : rowsList) {
            if (row.length != cols) {
                throw new IllegalArgumentException("Матрица имеет строки разной длины");
            }
        }

        SimpleDrobi[][] matrix = new SimpleDrobi[rows][cols];

        for (int i = 0; i < rows; i++) {
            String[] rowValues = rowsList.get(i);
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = parseSimpleDrobi(rowValues[j]);
            }
        }

        return matrix;
    }

    private static SimpleDrobi parseSimpleDrobi(String str) {
        if (str.contains("/")) {
            String[] parts = str.split("/");
            int chislitel = Integer.parseInt(parts[0].trim());
            int znamenatel = Integer.parseInt(parts[1].trim());
            return new SimpleDrobi(chislitel, znamenatel);
        } else {
            int value = Integer.parseInt(str.trim());
            return new SimpleDrobi(value);
        }
    }

}