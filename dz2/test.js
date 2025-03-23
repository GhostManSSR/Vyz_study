const orderBy = require("./script");

describe("Тестирование функции orderBy", () => {
    test("Корректная сортировка по одному свойству", () => {
        const input = [{ name: "B" }, { name: "A" }, { name: "C" }];
        const result = orderBy(input, ["name"]);
        expect(result).toEqual([{ name: "A" }, { name: "B" }, { name: "C" }]);
    });

    test("Корректная сортировка по нескольким свойствам", () => {
        const input = [
            { name: "Dev", age: 30 },
            { name: "Arti", age: 25 },
            { name: "Bob", age: 20 },
        ];
        const result = orderBy(input, ["name", "age"]);
        expect(result).toEqual([
            { name: "Arti", age: 25 },
            { name: "Bob", age: 20 },
            { name: "Dev", age: 30 },
        ]);
    });

    test("Выброс ошибки, если передан не массив", () => {
        expect(() => orderBy({ name: "Alice" }, "name")).toThrow("Второй аргумент не является масивом");
    });

    test("Выброс ошибки, если элементы массива не объекты", () => {
        expect(() => orderBy([1, 2, 3], ["name"])).toThrow("Все элементы массива должны быть объектами.");
    });

    test("Выброс ошибки, если в объекте отсутствует свойство для сортировки", () => {
        const input = [{ name: "Alice" }, { age: 30 }];
        expect(() => orderBy(input, ["name"])).toThrow("Отсутствует свойство name в одном из объектов.");
    });
});
