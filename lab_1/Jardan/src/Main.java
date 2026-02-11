void main() {
    try {
        Jardan solver = new Jardan("src/input.txt");
        solver.solve();
    } catch (Exception e) {
        System.err.println("Ошибка: " + e.getMessage());
    }
}
