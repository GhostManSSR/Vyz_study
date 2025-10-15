
class Vehicle{
    private make:string;
    private model:string;
    private year:number;

    constructor(make:string, model:string, year:number){
        this.make = make;
        this.model = model;
        this.year = year;
    }

    getInfo():string{
        return `${this.make} ${this.model} ${String(this.year)}`
    }
}

class Car extends Vehicle{
    private doors: number;

    constructor(make:string, model:string, year:number, doors:number){
        super(make, model, year);
        this.doors = doors;
    }

    openDoor(doorNumber: number): string {
        return `${super.getInfo()} Opening door ${doorNumber}`;
    }

    getInfoDoors(): number{
        return this.doors;
    }

    getInfo(): string {
        return `${super.getInfo()} with ${this.doors} doors`;
    }
}

class Motorcycle extends Vehicle {
    private hasSideCar: boolean;

    constructor(make: string, model: string, year: number, hasSideCar: boolean) {
        super(make, model, year);
        this.hasSideCar = hasSideCar;
    }

    popWheelie(): string {
        return `${super.getInfo()} is popping a wheelie!`;
    }

    getInfoSide():boolean{
        return this.hasSideCar;
    }

    getInfo(): string {
        const sideCarInfo = this.hasSideCar ? 'with sidecar' : 'without sidecar';
        return `${super.getInfo()} ${sideCarInfo}`;
    }
}

abstract class Shape {
    abstract area(): number;
    abstract perimeter(): number;

    getDescription(): string {
        return "This is a shape.";
    }
}

class Rectangle extends Shape {
    constructor(private width: number, private height: number) {
        super();
    }

    area(): number {
        return this.width * this.height;
    }

    perimeter(): number {
        return 2 * (this.width + this.height);
    }
}

class Circle extends Shape {
    constructor(private radius: number) {
        super();
    }

    area(): number {
        return Math.PI * this.radius * this.radius;
    }

    perimeter(): number {
        return 2 * Math.PI * this.radius;
    }
}

let circle1 = new Circle(25)

console.log(circle1.area())

class Repository<T extends { id: number }> {
    private items: T[] = [];

    constructor(private id: number) {}

    getById(id: number): T | undefined {
        return this.items.find(item => item.id === id);
    }

    save(item: T): void {
        const index = this.items.findIndex(i => i.id === item.id);
        if (index === -1) {
            this.items.push(item);
        } else {
            this.items[index] = item;
        }
    }
}

interface User{
    id: number;
    name: string;
    email: string;
    age: number;
}

class UserRepository extends Repository<User> {
    constructor(id: number) {
        super(id);
    }

    getByEmail(email: string): User | undefined {
        return this['items'].find(user => user.email === email);
    }
}

const userRepo = new UserRepository(1);

userRepo.save({ id: 1, name: "Alice", email: "alice@example.com", age: 30 });
userRepo.save({ id: 2, name: "Bob", email: "bob@example.com", age: 25 });

const userById = userRepo.getById(1);
console.log(userById);

const userByEmail = userRepo.getByEmail("bob@example.com");
console.log(userByEmail);

class Employee{
    private id:number;
    private name:string;
    private salary:number;

    constructor(id:number, name:string, salary:number){
        this.id = id;
        this.name = name;
        this.salary = salary;
    }

    getSalary():number{
        return this.salary;
    }

    getBonus():number{
        return this.salary * 0.1;
    }
}

class Manager extends Employee{
    private team:Employee[];

    constructor(id:number, name:string, salary:number, team:Employee[]){
        super(id, name, salary);
        this.team = team;
    }

    getTeam():Employee[]{
        return this.team;
    }

    getBonus(): number {
        const baseBonus = super.getSalary() * 0.15;
        const teamBonus = this.team.length * super.getSalary() * 0.05;
        return baseBonus + teamBonus;
    }
}

class Director extends Manager{
    private departament: string;

    constructor(id:number, name:string, salary:number, team:Employee[], departament:string){
        super(id, name, salary, team);
        this.departament = departament;
    }

    getBonus():number{
        const baseBonus = super.getSalary() * 0.20;
        const teamBonus = super.getTeam().length * super.getSalary() * 0.10;
        return baseBonus + teamBonus;
    }
}

const emp1 = new Employee(1, "John", 50000);
const emp2 = new Employee(2, "Jane", 60000);

const mgr = new Manager(3, "Michael", 80000, [emp1, emp2]);
const dir = new Director(4, "Sarah", 120000, [emp1, emp2, mgr], "Sales");

console.log(emp1.getBonus());
console.log(mgr.getBonus());
console.log(dir.getBonus());


type Constructor<T = {}> = new (...args: any[]) => T;

function Timestamped<TBase extends Constructor>(Base: TBase) {
    return class extends Base {
        created: Date = new Date();

        getTimestamp(): Date {
            return this.created;
        }
    };
}

function Loggable<TBase extends Constructor>(Base: TBase) {
    return class extends Base {
        log(message: string): void {
            console.log(`[LOG - ${new Date().toISOString()}]: ${message}`);
        }
    };
}

class User {
    constructor(public id: number, public name: string) {}
}

const UserWithTimestamp = Timestamped(User);
const UserWithLogging = Loggable(UserWithTimestamp);

const user = new UserWithLogging(1, "Alice");
console.log(user.name);
console.log(user.getTimestamp());
user.log("This is a log message.");

class Animal{
    private name:string;

    constructor(name:string){
        this.name = name;
    }


    speak():string{
        return `${name}`
    }
}

class Dog extends Animal{

    constructor(name:string){
        super(name);
    }

    speak():string{
        return `${name} сказал гав`;
    }
}

class Cat extends Animal{
    constructor(name:string){
        super(name);
    }

    speak():string{
        return `${name} сказала мяу`;
    }
}

function createAnimal<T extends Animal>(animalClass: new (name: string) => T, name:string){
    return new animalClass(name);
}

let bobs = createAnimal(Cat, "Bob");

console.log(bobs.speak())

function isCar(vehicle: Vehicle): vehicle is Car {
    return typeof (vehicle as Car).getInfoDoors === "function";
}

function isMotorcycle(vehicle: Vehicle): vehicle is Motorcycle {
    return typeof (vehicle as Motorcycle).getInfoSide === "function";
}

const vehicles: Vehicle[] = [
    new Car("Toyota", "Camry", 2022, 4),
    new Motorcycle("Harley", "Sportster", 2021, true),
    new Car("Honda", "Accord", 2020, 2),
];

vehicles.forEach(vehicle => {
    console.log(vehicle.getInfo());

    if (isCar(vehicle)) {
        console.log(vehicle.openDoor(1));
    } else if (isMotorcycle(vehicle)) {
        console.log(vehicle.popWheelie());
    }
});