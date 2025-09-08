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

// 1 задание

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


// 2 задание


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

// 3 задание

function filteryArray<T>(items: T[], predicate: (item: T) => boolean): T[] {
    return items.filter(item => predicate(item));
}


let arrayNum = [1, 2, 3, 4, 2, 3];

console.log(filteryArray(arrayNum, item => item > 2));

// 4 задание

enum OrderStatus{
    Pending,
    Shipped,
    Delivered
}

function getStatusMessage(status: OrderStatus) : string{
    switch(status){
        case OrderStatus.Pending:
            return  "Your order is pending."
        case OrderStatus.Shipped:
            return  "Your order is on the way."
        case OrderStatus.Delivered:
            return "Your order has been delivered."
    }
}

console.log(getStatusMessage(OrderStatus.Delivered))

// 5 задание

let age: number = 15;

let names: string = "hello";

let isStudent:boolean = true;

let hobbies: string [] = ['reading', 'sports']

let favoriteScores: [number, number] = [10,10]

let contact : object = {phone:"11", email: "abc"}

// 6 задание

type Status = 'active' | 'inactive' | 'new';

function getStatusColor(status: Status): string{
    switch(status){
        case 'active':
            return "green"
        case 'new':
            return 'blue'
        case 'inactive':
            return "red"
    }
}

console.log(getStatusColor('active'))

// 7 задание

type StringFormatter = (input: string, uppercase?: boolean) => string;

const capitalizeFirstLetter: StringFormatter = (input, uppercase = false) => {
    if (!input) return input;
    const firstLetter = input[0].toUpperCase();
    const rest = input.slice(1);
    return uppercase ? (firstLetter + rest).toUpperCase() : firstLetter + rest;
};

const trimAndUppercase: StringFormatter = (input, uppercase = false) => {
    const trimmed = input.trim();
    return uppercase ? trimmed.toUpperCase() : trimmed;
};

console.log(capitalizeFirstLetter("active", true))

console.log(capitalizeFirstLetter("active"))

console.log(trimAndUppercase(" active "))

// 8 задание

enum Direction{
    Up,
    Down,
    Left,
    Right
}

function move(direction: Direction) : void{
    switch(direction){
        case Direction.Up:
            return console.log("Moving Up")
        case Direction.Left:
            return console.log("Moving Left")
        case Direction.Down:
            return console.log("Moving Down")
        case Direction.Right:
            return console.log("Moving Right")
    }
}

move(Direction.Up)

// 9 задание

function getFirstElement<T>(arr: T[]): T | undefined {
    return arr.length > 0 ? arr[0] : undefined;
}

const numbers = [10, 20];
const firstNumber = getFirstElement(numbers);
console.log(firstNumber);

const strings = ["apple", "banana"];
const firstString = getFirstElement(strings);
console.log(firstString);

const emptyArray: number[] = [];
const firstEmpty = getFirstElement(emptyArray);
console.log(firstEmpty);

// 10 задание

interface HasId{
    id: number
}

function findById<T extends HasId>(items: T[], id: number): T | undefined {
  return items.find(item => item.id === id);
}

let arrayObject: object[] = [{id: 14, names: "dsad"}, {names: "Hello"}]

const items = [{ id: 1, name: "A" }, { id: 2, name: "B" }];
const found = findById(items, 2);
console.log(found)


// 11 задание

interface Book {
    title: string;
    author: string;
    year?: number;
    genre: 'fiction' | 'non-fiction';
}

function createBook(book: Book): Book {
    return book;
}

const book1: Book = {
    title: "1984",
    author: "George",
    genre: "fiction",
    year: 1949
};

const book2: Book = {
    title: "Sapiens",
    author: "Harari",
    genre: "non-fiction"
};

console.log(createBook(book1));
console.log(createBook(book2));


// 12 задание

let pageNumber: unknown = '10';

let pageNumberAsNumber: number = parseInt(pageNumber as string);

console.log(pageNumberAsNumber)


// 13 задание

function throwError(message: string): never {
    throw new Error(message);
}

// throwError("error")

// 14 задание

interface Point {
    readonly x: number;
    readonly y: number;
}

const point: Point = { x: 10, y: 20 };

//point.x = 30  //Нельзя присваивать значение свойству "x", так как оно только для чтения


const scores: ReadonlyArray<number> = [1, 2, 3];

// Метод "push" отсутствует так как массив исползуется только для чтения функция добавления отсутсвует"
// scores.push(4);
