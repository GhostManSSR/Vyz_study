const masObject = [
    {
        name: "Dev",
        age: 30,
    },
    {
        name: "Arti",
        age: 25,
    },
    {
        name: "Bob",
        age: 20,
    },
]

const masName = ["name", "age"]

console.log(masObject.map(i => typeof(i) ))

console.log(masObject.map(i => Object.keys(i)));


console.log(masName.length)

let orderBy = (masObject, masName) => {
    if (!Array.isArray(masName)) {
        throw new Error("Второй аргумент не является масивом");
    }

    for (const item of masObject) {
        if (typeof item !== "object" || item === null) {
            throw new Error("Все элементы массива должны быть объектами.");
        }
    }
    return [...masObject].sort((a, b) => {
        for (const prop of masName) {
            if (!(prop in a) || !(prop in b)) {
                throw new Error(`Отсутствует свойство ${prop} в одном из объектов.`);
            }

            if (a[prop] > b[prop]) return 1;
            if (a[prop] < b[prop]) return -1;
        }
        return 0;
    });
}

// console.log(masObject)


// const result = orderBy(masObject, masName);

// console.log(result)

module.exports = orderBy;
