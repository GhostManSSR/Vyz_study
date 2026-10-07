using System;
using System.Collections.Generic;
using PickupPoint.Db;

Console.OutputEncoding = System.Text.Encoding.UTF8;

string? conn = Environment.GetEnvironmentVariable("PICKUP_DB");
if (string.IsNullOrWhiteSpace(conn))
{
    Console.Error.WriteLine(
        "Ошибка: переменная окружения PICKUP_DB не задана.\n\n" +
        "Запустите так:\n" +
        "  PICKUP_DB=\"Host=localhost;Database=pickup;Username=pickup_user;Password=pickup_pass\" dotnet run\n");
    return 1;
}

using var db = new Database(conn);
Console.WriteLine("Подключено к БД. Готово.\n");

while (true)
{
    Console.WriteLine("""
        1 — список заказов (поиск)
        2 — принять заказ
        3 — выдать заказ
        4 — отменить заказ
        5 — отчёт
        6 — очистить историю
        0 — выход
        """);
    Console.Write("> ");
    string cmd = Console.ReadLine() ?? "";

    try
    {
        switch (cmd)
        {
            case "1": ListOrders(db);  break;
            case "2": AddOrder(db);    break;
            case "3": Issue(db);       break;
            case "4": Cancel(db);      break;
            case "5": Report(db);      break;
            case "6": Cleanup(db);     break;
            case "0": return 0;
        }
    }
    catch (Exception e)
    {
        Console.WriteLine($"Ошибка: {e.Message}\n");
    }
}

// ─────────────────────────────────────────────────────────

static void ListOrders(Database db)
{
    Console.Write("Поиск (пусто = все): ");
    string filter = Console.ReadLine() ?? "";
    var orders = db.ListOrders(filter);
    Print(orders);
}

static void AddOrder(Database db)
{
    Console.Write("Телефон или артикул: ");
    string contact = Console.ReadLine() ?? "";
    if (contact.Length < 5) { Console.WriteLine("Слишком коротко.\n"); return; }

    Console.Write("Ячейка (можно пусто): ");
    string cell = Console.ReadLine() ?? "";

    string code = db.AddOrder(contact, cell);
    Console.WriteLine($"Принят: {code}\n");
}

static void Issue(Database db)
{
    Console.Write("Код заказа: ");
    string code = Console.ReadLine() ?? "";
    Console.WriteLine(db.IssueOrder(code) ? "Выдан.\n" : "Нельзя выдать.\n");
}

static void Cancel(Database db)
{
    Console.Write("Код заказа: ");
    string code = Console.ReadLine() ?? "";
    Console.WriteLine(db.CancelOrder(code) ? "Отменён.\n" : "Нельзя отменить.\n");
}

static void Report(Database db)
{
    var r = db.BuildReport();
    Console.WriteLine($"Принято: {r.ReadyCount} • Выдано: {r.IssuedCount} • Отменено: {r.CancelledCount}\n");
}

static void Cleanup(Database db)
{
    int n = db.CountClosedOrders();
    if (n == 0) { Console.WriteLine("Нечего удалять.\n"); return; }

    Console.Write($"Удалить {n} заказ(ов)? (y/n): ");
    if ((Console.ReadLine() ?? "").Trim().ToLower() != "y") return;

    int deleted = db.DeleteClosedOrders();
    Console.WriteLine($"Удалено: {deleted}\n");
}

static void Print(List<Order> orders)
{
    if (orders.Count == 0) { Console.WriteLine("Пусто.\n"); return; }
    Console.WriteLine($"{"Код",-12} {"Телефон",-16} {"Артикул",-16} {"Ячейка",-8} {"Статус",-10} {"Поступил"}");
    Console.WriteLine(new string('-', 90));
    foreach (var o in orders)
    {
        Console.WriteLine($"{o.Code,-12} " +
                          $"{(string.IsNullOrEmpty(o.Phone) ? "—" : o.Phone),-16} " +
                          $"{(string.IsNullOrEmpty(o.Article) ? "—" : o.Article),-16} " +
                          $"{(string.IsNullOrEmpty(o.Cell) ? "—" : o.Cell),-8} " +
                          $"{o.Status,-10} {o.CreatedAt}");
    }
    Console.WriteLine();
}