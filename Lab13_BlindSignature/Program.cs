using System.Numerics;
using Lab13_BlindSignature.Client;
using Lab13_BlindSignature.Models;
using Lab13_BlindSignature.Server;

Console.OutputEncoding = System.Text.Encoding.UTF8;
Console.WriteLine("============================================================");
Console.WriteLine(" ЛАБОРАТОРНАЯ РАБОТА №13");
Console.WriteLine(" ПРОТОКОЛ «СЛЕПОЙ» ПОДПИСИ — АНОНИМНОЕ ГОЛОСОВАНИЕ");
Console.WriteLine("============================================================\n");

Console.WriteLine("Запуск сервера...");
Console.WriteLine("Генерация двух 1024-битных простых чисел p и q.");
Console.WriteLine("Это может занять несколько секунд.\n");

var server = new VotingServer(1024);
var publicKey = server.Keys.PublicKey;

Print("p (секретное простое число)", server.Keys.P);
Print("q (секретное простое число)", server.Keys.Q);
Print("n = p * q (открытый модуль)", publicKey.N);
Print("phi(n)", server.Keys.Phi);
Print("e (открытая степень)", publicKey.E);
Print("d (секретная степень сервера)", server.Keys.PrivateKey.D);

Console.WriteLine("\nОткрытый ключ сервера: (e, n)");
Console.WriteLine("Секретный ключ сервера: (d, n) — клиент его не получает.\n");

Console.WriteLine("------------------------------------------------------------");
Console.WriteLine("ГОЛОСОВАНИЕ");
Console.WriteLine("------------------------------------------------------------");
Console.WriteLine(server.PublicDatabase.Count == 0
    ? "Открытая база пока пуста."
    : "В открытой базе уже есть бюллетени.");

Console.WriteLine("\nВопрос: Одобряете ли вы проведение данного голосования?");
Console.WriteLine("1 — Да");
Console.WriteLine("2 — Нет");
Console.WriteLine("3 — Воздержался");

var choice = ReadChoice();
var client = new VotingClient("user-001");
client.CreateBallot(choice);

Console.WriteLine("\n================ КЛИЕНТ: ШАГ 1 =================");
Console.WriteLine("Сформирован бюллетень:");
Console.WriteLine($"ElectionId      : {client.Ballot!.ElectionId}");
Console.WriteLine($"Question        : {client.Ballot.Question}");
Console.WriteLine($"Choice          : {ChoiceText(client.Ballot.Choice)} ({(int)client.Ballot.Choice})");
Console.WriteLine($"ServerAddress   : {client.Ballot.ServerAddress}");
Console.WriteLine($"RandomNonce     : {client.Ballot.RandomNonce}");

Console.WriteLine("\n================ КЛИЕНТ: ШАГ 2 =================");
Console.WriteLine("SHA3-256 вычисляется локально на клиенте.");
Print("H = SHA3-256(B) как число", client.BallotHash);
Console.WriteLine($"SHA3-256 (hex)  : {Convert.ToHexString(client.BallotHashBytes!)}");

Console.WriteLine("\n================ КЛИЕНТ: ШАГ 3 =================");
client.PrepareBlindSignature(publicKey);
Console.WriteLine("Выбран случайный r, взаимно простой с n.");
Print("r", client.RandomR);
Print("r^-1 mod n", client.RandomRInverse);
Print("B' = H * r^e mod n (ослеплённый хэш)", client.BlindedHash);

Console.WriteLine("\nКЛИЕНТ → СЕРВЕР: передаётся только B'.");
Console.WriteLine("Сервер НЕ получает исходный бюллетень и его SHA3-хэш.\n");

Console.WriteLine("================ СЕРВЕР: ШАГ 4 =================");
if (!server.TryIssueBlindSignature(client.UserId, client.BlindedHash, out var blindedSignature, out var issueMessage))
{
    Console.WriteLine($"ОШИБКА: {issueMessage}");
    return;
}

Console.WriteLine(issueMessage);
Print("S' = (B')^d mod n (слепая подпись)", blindedSignature);
Console.WriteLine("Сервер пометил пользователя как получившего бюллетень.");
Console.WriteLine("Сервер по-прежнему не знает, за какой вариант будет голосование.\n");

Console.WriteLine("================ КЛИЕНТ: ШАГ 5 =================");
var localSignatureValid = client.ReceiveAndUnblind(publicKey, blindedSignature);
Print("S = S' * r^-1 mod n (обычная подпись бюллетеня)", client.Signature);
Console.WriteLine($"Локальная проверка S^e mod n = H: {(localSignatureValid ? "УСПЕШНО" : "ОШИБКА")}");

Console.WriteLine("\n================ АНОНИМНАЯ ПЕРЕДАЧА =================");
Console.WriteLine("Клиент передаёт серверу B + S по условно анонимному каналу.");
Console.WriteLine("В данной лабораторной сети нет: передача моделируется вызовом метода.");

var accepted = server.AcceptAnonymousBallot(client.Ballot, client.Signature, out var acceptMessage);
Console.WriteLine($"\nСервер: {acceptMessage}");

Console.WriteLine("\n================ ПРОВЕРКА =================");
var serverHash = Lab13_BlindSignature.Crypto.BallotHash.ToPositiveInteger(
    Lab13_BlindSignature.Crypto.BallotHash.Sha3_256(client.Ballot));
var verificationValue = BigInteger.ModPow(client.Signature, publicKey.E, publicKey.N);
Print("H = SHA3-256(B)", serverHash);
Print("S^e mod n", verificationValue);
Console.WriteLine($"S^e mod n == H: {(verificationValue == serverHash ? "TRUE — подпись корректна" : "FALSE — подпись некорректна")}");
Console.WriteLine($"Голос принят: {(accepted ? "ДА" : "НЕТ")}");

Console.WriteLine("\n================ ОТКРЫТАЯ БАЗА =================");
foreach (var record in server.PublicDatabase)
{
    Console.WriteLine($"BallotId : {record.BallotId}");
    Console.WriteLine($"Choice   : {ChoiceText(record.Ballot.Choice)}");
    Console.WriteLine($"Signature: {record.Signature}");
    Console.WriteLine($"Accepted : {record.AcceptedAt:yyyy-MM-dd HH:mm:ss}");
}

Console.WriteLine("\n================ ПРОВЕРКА ПОВТОРНОГО ГОЛОСА =================");
var repeated = server.AcceptAnonymousBallot(client.Ballot, client.Signature, out var repeatMessage);
Console.WriteLine($"Результат повторной отправки: {(repeated ? "ПРИНЯТО" : "ОТКЛОНЕНО")}");
Console.WriteLine($"Причина: {repeatMessage}");

Console.WriteLine("\n============================================================");
Console.WriteLine("Демонстрация протокола завершена.");
Console.WriteLine("============================================================");

static VoteChoice ReadChoice()
{
    while (true)
    {
        Console.Write("Введите номер варианта: ");
        var input = Console.ReadLine();
        if (input == "1") return VoteChoice.Yes;
        if (input == "2") return VoteChoice.No;
        if (input == "3") return VoteChoice.Abstained;
        Console.WriteLine("Ошибка: введите 1, 2 или 3.");
    }
}

static string ChoiceText(VoteChoice choice) => choice switch
{
    VoteChoice.Yes => "Да",
    VoteChoice.No => "Нет",
    VoteChoice.Abstained => "Воздержался",
    _ => "Неизвестно"
};

static void Print(string name, BigInteger value)
{
    Console.WriteLine($"{name}:");
    Console.WriteLine(value);
    Console.WriteLine($"  [бит: {GetBitLength(value)}]\n");
}

static int GetBitLength(BigInteger value)
{
    if (value.IsZero) return 0;
    var bytes = BigInteger.Abs(value).ToByteArray(isUnsigned: true, isBigEndian: true);
    var bits = (bytes.Length - 1) * 8;
    var first = bytes[0];
    while (first > 0)
    {
        bits++;
        first >>= 1;
    }
    return bits;
}
