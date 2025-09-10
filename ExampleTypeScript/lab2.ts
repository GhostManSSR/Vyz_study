//задание 1

class Stack<T>{
    private stack: T [];

    constructor(initialData: T []){
        this.stack = initialData
    }

    push(item: T): void{
        this.stack.unshift(item)
    }

    pop(): T | undefined{
        let tmpEl = this.stack[this.stack.length - 1]
        this.stack.shift()
        return tmpEl;
    }

    peek(): T | undefined{
        return this.stack[0]
    }

    getItems():void{
        console.log(this.stack)
    }

}

let jk  = [2,3,4]

let newMassiv = new Stack(jk)

console.log(newMassiv.push(4))

newMassiv.getItems()

//задание 2

interface User2{
    id: number;
    name: string;
    email: string;
    age: number
}

type UserKeys = keyof User2; // "id" | "name" | "email" | "age"

function getUserProperty(user: User2, key: UserKeys): User2[UserKeys]{
    return user[key]
}

const userAlice: User2 = { id: 1, name: "Alice", email: "abs", age: 25 };

console.log(getUserProperty(userAlice, "name"))

//задание 3

interface Cars{
    breand: string;
    year: number;
}

const carMersedes:Cars = {
    breand: "Mersedes",
    year: 2005
}



type CarType = typeof carMersedes

// type IsArrayType<T> = (item: T []) => boolean

// const isArrayItem: IsArrayType (item) => {
//     return Array.isArray(item)
// }

//задание 5

type IsArrayType<T> = T extends Array<any> ? true : false;

type Test1 = IsArrayType<number[]>;

type Test2 = IsArrayType<string>;

function isArrayType<T>(arg: T): boolean {
    return Array.isArray(arg);
}

// console.log(isArrayType([1, 2, 3])); // true

type ExtractNumber<T> = T extends Array<number> ? number : never;

function assertNumberArray<T>() {
    // если T не массив чисел, будет ошибка
    type Check = ExtractNumber<T>;
    //const testCheck: Check = 123; // number или never
}

function isNumberArray(arg: any): arg is number[] {
    return Array.isArray(arg) && arg.every(item => typeof item === "number");
}

console.log(isNumberArray([1, 2, 3])); // true
console.log(isNumberArray(["1", "2"])); // false
console.log(isNumberArray(123)); // false

// Вызов (редактор и компилятор покажут ошибки или нет)
assertNumberArray<number[]>();
// assertNumberArray<string[]>(); // ошибка

//задание 6

type ReadonlyOptional<T> = {
    readonly [K in keyof T]?: T[K];
}

interface UserOnly{
    id: number,
    name: string
}

type ReadonlyOptionalUser = ReadonlyOptional<UserOnly>;

// Теперь свойства необязательны и readonly
const user1: ReadonlyOptionalUser = {};
const user2: ReadonlyOptionalUser = { id: 1 };
const user3: ReadonlyOptionalUser = { id: 1, name: "Alex" };

console.log(user3)

//задание 7

type Getters<T> = {
    [K in keyof T as `get${Capitalize<string & K>}`]: () => T[K];
}

type User = {
    name: string;
    age: number;
};

type UserGetters = Getters<User>;

const userGetters: UserGetters = {
    getName: () => "Alice",
    getAge: () => 30,
};

console.log(userGetters.getAge())

//задание 8

type EventHandler<Events extends string> = {
    [E in Events as `on${Capitalize<E>}`]: () => void;
};

type Handlers = EventHandler<'click' | 'hover' | 'submit'>;

const setEvent: Handlers = {
    onClick: () => {},
    onHover: () => {},
    onSubmit: () => {}
};

//задание 9

type CSSUnits = 'px' | 'em' | 'rem' | '%'

type CSSValue = `${number}${CSSUnits}`;

function setStyle(value: CSSValue) {
    console.log(`Установлен стиль со значением: ${value}`);
}

setStyle("100px");
setStyle("2.5em");
setStyle("50%");
setStyle("1rem");

//задание 10

class Pair<T, U>{
    private first:T;
    private second:U;

    constructor(first:T, second:U){
        this.first = first;
        this.second = second;
    }

    getSecond():U{
        return this.second;
    }

    getFirst():T{
        return this.first
    }
}

let testPair = new Pair("45",55)
console.log(testPair.getFirst())
console.log(testPair.getSecond())

//задание 11

class AppConfig {
    private static instance: AppConfig | null = null;
    private config: Record<string, any>;

    private constructor() {
        this.config = this.loadConfig();
    }

    private loadConfig(): Record<string, any> {
        return {
            apiUrl: "https://api.example.com",
            retryCount: 3,
            timeout: 5000,
        };
    }

    public static getInstance(): AppConfig {
        if (this.instance === null) {
            this.instance = new AppConfig();
        }
        return this.instance;
    }

    public getConfig(): Record<string, any> {
        return this.config;
    }
}

const appConfig1 = AppConfig.getInstance();
console.log(appConfig1.getConfig());

const appConfig2 = AppConfig.getInstance();
console.log(appConfig1 === appConfig2); // true, один и тот же экземпляр

//задание 12

type MethodKeys<T> = {
    [K in keyof T]: T[K] extends (...args: any[]) => any ? K : never
}[keyof T];

type Methods<T> = Pick<T, MethodKeys<T>>;

function withLogging<T extends object>(obj: T): T {
    return new Proxy(obj, {
        get(target, prop: string | symbol, receiver) {
            const origValue = Reflect.get(target, prop, receiver);

            if (typeof origValue === "function") {
                return function (this: any, ...args: any[]) {
                    console.log(`Вызов метода: ${String(prop)}, аргументы:`, args);
                    const result = origValue.apply(this, args);
                    console.log(`Результат метода ${String(prop)}:`, result);
                    return result;
                };
            }

            return origValue;
        },
    });
}

const example = {
    greet(name: string) {
        return `Hello, ${name}!`;
    },
    sum(a: number, b: number) {
        return a + b;
    },
    value: 42
};

const loggedExample = withLogging(example);

console.log(loggedExample.greet("Alice"));

console.log(loggedExample.sum(3, 4));

console.log(loggedExample.value);

//задание 13

type ApiRoutes<Paths extends string> = {
    [P in Paths as `get${Capitalize<RemoveSlash<P>>}`]: () => void;
};

// Вспомогательный тип для удаления начального слэша из пути
type RemoveSlash<S extends string> = S extends `/${infer R}` ? R : S;

type Routes = '/user' | '/post';

type Api = ApiRoutes<Routes>;

const api: Api = {
    getUser() {
        console.log('Get user');
    },
    getPost() {
        console.log('Get post');
    }
};
