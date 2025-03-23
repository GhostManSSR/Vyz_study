const deleteWhere = require("./script");

describe("Тестирование функции deleteWhere", () => {
    test("Проверка", () => {
        const input = [
            {
                name: "Tom",
                age: 14
            },
            {
                name: "Arti",
                age: 16
            },
            {
                name: "Left",
                age: 74
            }
        ];

        let P = (a) => {
            if(a.name.length > 3){
                return true
            }
            return false
        }
        const result = deleteWhere(input, P);
        expect(result).toEqual([
            {
                name: "Arti",
                age: 16
            },
            {
                name: "Left",
                age: 74
            }
        ]);
    });

    test("Выброс ошибки, если элементы массива не объекты", () => {
        let P = (a) => {
            if(a.name.length > 3){
                return true
            }
            return false
        }
        expect(() => deleteWhere([1, 2, 3], P)).toThrow("Все элементы массива должны быть объектами.");
    });
});