const field = "Tom"
const obj = [
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
]

let P = (a) => {
    if(a.name.length > 3){
        return true
    }
    return false
}

let deleteWhere = (objs, p) => {
    // for (const item of objs) {
    //     if (typeof item !== "object" || item === null) {
    //         throw new Error("Все элементы массива должны быть объектами.");
    //     }
    // }
    result = []

    objs.forEach(element => {
        if (typeof element !== "object" || element === null) {
            throw new Error("Все элементы массива должны быть объектами.");
        }
        // if(p(element) === true){
        //     console.log("ss")
        //     result.push(element)
        // }
        p(element) && result.push(element) 
    });
    return result;
}

console.log(deleteWhere(obj,P))


module.exports = deleteWhere;