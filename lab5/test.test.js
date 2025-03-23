const getDataFromAPI = require("./getDataFromAPI")
const getStatus = require("./getStatus")


describe("Тестирование функции getStatus", () => {

    test("Корректная обработка данных", async () => {
        const mockData = [
            { category: "beauty" },
            { category: "fragrances" },
            { category: "furniture" },
            { category: "groceries" },
        ];

        jest.spyOn(getDataFromAPI, 'getDataFromAPI').mockResolvedValue(mockData);

        const expectedResult = {
            "beauty": 1,
            "fragrances": 1,
            "furniture": 1,
            "groceries": 1,
        };

        await expect(getStatus.getStatus()).resolves.toEqual(expectedResult);
    });

    // test("Выброс ошибки, если нет доступа к url", async () => {
    // //     global.fetch = jest.fn().mockRejectedValue(new Error("Network Error"));

    // //     await expect(getStatus.getStatus()).rejects.toThrow("HTTP error link or network!");
    // // });

    // test("Выброс ошибки, если элементов нет", async () => {
    //     global.fetch = jest.fn().mockResolvedValue({
    //         ok: true,
    //         json: () => ({ data: [] }),
    //     });

    //     await expect(fun.calcStatsFromAPI()).rejects.toThrow("No data");
    // });

});
