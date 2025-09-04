// Welcome to the TypeScript Playground, this is a website
// which gives you a chance to write, share and learn TypeScript.

// You could think of it in three ways:
//
//  - A location to learn TypeScript where nothing can break
//  - A place to experiment with TypeScript syntax, and share the URLs with others
//  - A sandbox to experiment with different compiler features of TypeScript


function printId(num: number | string){
    console.log("Your Id:" + num + "!");
}

// printId(101)

function multiplyAll(
    values: number[] | undefined,
    factor: number
): number[] | undefined {
    if (!values) {
        return values;
    } else {
        return values.map((x) => x * factor);
    }
}

async function getFavoriteNumber(): Promise<number>{
    return 26;
}
console.log(getFavoriteNumber())


const numberList = [1,2,3,4]

multiplyAll(numberList, 5)

function getRandomInt(min: number, max: number): number {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

interface User{
    id: number;
    name: string | "";
    email:string | "";
    isActive: boolean | true;
}


function createUser(opts: User) : {id: number, name: string, email: string, isActive: boolean} {
    let currentIds = opts.id === undefined ? 0 : opts.id;
    let currentName = opts.name;
    let currentEmail = opts.email;
    let currentIsActive = opts.isActive;
    return {id: currentIds, name: currentName, email: currentEmail, isActive: currentIsActive}
}

// console.log(createUser({id: 15, name: "Arti", email: "ess", isActive: true}))


interface Shape{
    shape: string,
    side: number,
}

// function calculateArea(shapeCircle: string) : number {
//     // let result = 0;
//     // if(isShapes.shape === "square"){
//     //     result = isShapes.side * isShapes.side
//     // }
//     // return result;
//     return 5
// }


function calculateArea(shape: "square", value: number): number;
function calculateArea(shape: "circle", value: number): number;
function calculateArea(shape: "square" | "circle", value: number): number {
    if (shape === "square") {
        return value * value;
    } else if (shape === "circle") {
        return Math.PI * value * value;
    }
    return 0;
}


console.log(calculateArea("circle",15))