void main() {
    try {
//        System.out.println("\n====== ================ ЕДИНСТВЕННОЕ РЕШЕНИЕ ============== ===========\n");
//        Jardan solver = new Jardan("input.txt");
//        solver.solve();
//        System.out.println("\n====== ================ НЕТ РЕШЕНИЯ ============== ===========\n");
//        Jardan solver_2 = new Jardan("input_2.txt");
//        solver_2.solve();
//        System.out.println("\n====== ================ МНОЖЕСТВО РЕШЕНИЙ ============== ===========\n");
//        Jardan solver_3 = new Jardan("input_3.txt");
//        solver_3.solve();
        System.out.println("\n====== ================ 7 Nomer ============== ===========\n");
        Jardan solver_4 = new Jardan("input_1.txt");
        solver_4.solve();
    } catch (Exception e) {
        System.err.println("Ошибка: " + e.getMessage());
    }
}
