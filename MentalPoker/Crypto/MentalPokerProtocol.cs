
using System.Numerics;
using System.Security.Cryptography;
using MentalPoker.Models;

namespace MentalPoker.Crypto;

public class MentalPokerProtocol
{
    private readonly Random _random = new();

    public BigInteger P { get; private set; }

    public BigInteger Q { get; private set; }

    public int CardsPerPlayer { get; private set; }

    public int CommunityCardsCount { get; private set; }

    public List<Player> Players { get; } = new();

    public List<BigInteger> OriginalDeck { get; private set; } = new();

    public List<BigInteger> EncryptedDeck { get; private set; } = new();

    public List<BigInteger> DecryptedDeck { get; private set; } = new();

    public List<BigInteger> CommunityCards { get; private set; } = new();

    public List<ProtocolLogEntry> Log { get; } = new();

    public bool IsCompleted { get; private set; }

    public bool IsInitialized { get; private set; }


    // ============================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ============================================================

    public void Initialize(
        int playerCount,
        int deckSize,
        int cardsPerPlayer,
        int communityCards)
    {
        if (playerCount < 2)
            throw new ArgumentException(
                "Минимум 2 игрока.");

        if (deckSize < 5)
            throw new ArgumentException(
                "Колода должна содержать минимум 5 карт.");

        if (cardsPerPlayer < 1)
            throw new ArgumentException(
                "Количество карт игроку должно быть больше 0.");

        if (communityCards < 0)
            throw new ArgumentException(
                "Количество общих карт не может быть отрицательным.");

        int requiredCards =
            playerCount * cardsPerPlayer +
            communityCards;

        if (requiredCards > deckSize)
        {
            throw new ArgumentException(
                $"Недостаточно карт.\n\n" +
                $"Необходимо: {requiredCards}\n" +
                $"В колоде: {deckSize}");
        }

        Players.Clear();
        OriginalDeck.Clear();
        EncryptedDeck.Clear();
        DecryptedDeck.Clear();
        CommunityCards.Clear();
        Log.Clear();

        IsCompleted = false;
        IsInitialized = false;

        CardsPerPlayer = cardsPerPlayer;
        CommunityCardsCount = communityCards;


        // ------------------------------------------------------------
        // Генерируем p и q
        // ------------------------------------------------------------

        GeneratePrimeParameters(deckSize);


        AddLog(
            "INIT",
            $"Создание протокола: " +
            $"игроков={playerCount}, " +
            $"колода={deckSize}, " +
            $"карт игроку={cardsPerPlayer}, " +
            $"общих карт={communityCards}");


        // ------------------------------------------------------------
        // Создаём игроков
        // ------------------------------------------------------------

        for (int i = 0; i < playerCount; i++)
        {
            Players.Add(
                new Player(
                    i + 1,
                    $"Игрок {i + 1}")
            );
        }


        // ------------------------------------------------------------
        // Создаём ключи
        // ------------------------------------------------------------

        GenerateKeys();


        // ------------------------------------------------------------
        // Создаём уникальную колоду
        //
        // Карты представлены числами:
        //
        // 1, 2, 3, ..., deckSize
        // ------------------------------------------------------------

        for (int i = 1; i <= deckSize; i++)
        {
            OriginalDeck.Add(i);
        }


        // Дополнительная проверка исходной колоды.

        if (OriginalDeck.Distinct().Count()
            != OriginalDeck.Count)
        {
            throw new InvalidOperationException(
                "Ошибка создания исходной колоды: обнаружены повторы.");
        }


        EncryptedDeck =
            new List<BigInteger>(OriginalDeck);


        AddLog(
            "DECK",
            $"Создана колода из {deckSize} уникальных карт.");

        AddLog(
            "DECK",
            $"Диапазон карт: 1..{deckSize}.");

        IsInitialized = true;
    }


    // ============================================================
    // ГЕНЕРАЦИЯ p И q
    // ============================================================

    private void GeneratePrimeParameters(int deckSize)
    {
        /*
         * Используем:
         *
         * p — простое число
         *
         * q = (p - 1) / 2 — тоже простое число
         *
         * То есть p является безопасным простым числом.
         *
         * Важно:
         *
         * p должно быть больше максимального номера карты.
         */

        BigInteger candidate =
            Math.Max(7, deckSize + 2);


        if (candidate.IsEven)
            candidate++;


        while (true)
        {
            if (IsPrime(candidate))
            {
                BigInteger q =
                    (candidate - 1) / 2;

                if (IsPrime(q))
                {
                    P = candidate;
                    Q = q;

                    AddLog(
                        "PARAMETERS",
                        $"Выбраны параметры p={P}, q={Q}.");

                    return;
                }
            }

            candidate += 2;
        }
    }


    // ============================================================
    // ГЕНЕРАЦИЯ КЛЮЧЕЙ
    // ============================================================

    private void GenerateKeys()
    {
        BigInteger phi =
            P - 1;


        foreach (Player player in Players)
        {
            BigInteger k;

            do
            {
                k = RandomBigInteger(
                    2,
                    phi - 1);
            }
            while (
                BigInteger.GreatestCommonDivisor(
                    k,
                    phi) != 1);


            BigInteger d =
                ModInverse(
                    k,
                    phi);


            player.EncryptionKey = k;

            player.DecryptionKey = d;


            AddLog(
                "KEY",
                $"{player.Name}: " +
                $"k={k}, " +
                $"d={d}, " +
                $"k*d mod (p-1)=" +
                $"{(k * d) % phi}");
        }
    }


    // ============================================================
    // ЗАПУСК ИГРЫ
    // ============================================================

    public void RunGame()
    {
        if (!IsInitialized)
        {
            throw new InvalidOperationException(
                "Сначала необходимо вызвать Initialize().");
        }


        AddLog(
            "GAME",
            "Начало протокола ментального покера.");


        // --------------------------------------------------------
        // 1. Все игроки шифруют и перемешивают колоду
        // --------------------------------------------------------

        EncryptAndShuffleDeck();


        // --------------------------------------------------------
        // 2. Проверяем зашифрованную колоду
        // --------------------------------------------------------

        VerifyEncryptedDeck();


        // --------------------------------------------------------
        // 3. Раздаём карты
        // --------------------------------------------------------

        DealCards();


        // --------------------------------------------------------
        // 4. Раскрываем карты игроков
        // --------------------------------------------------------

        RevealPlayerCards();


        // --------------------------------------------------------
        // 5. Раскрываем общие карты
        // --------------------------------------------------------

        RevealCommunityCards();


        // --------------------------------------------------------
        // 6. Восстанавливаем полностью расшифрованную колоду
        // --------------------------------------------------------

        DecryptedDeck =
            DecryptEntireDeck();


        // --------------------------------------------------------
        // 7. Проверяем результат
        // --------------------------------------------------------

        bool verified =
            VerifyGame();


        IsCompleted =
            verified;


        AddLog(
            "RESULT",
            verified
                ? "Протокол успешно завершён и проверен."
                : "Проверка протокола завершилась ошибкой.");
    }


    // ============================================================
    // ШИФРОВАНИЕ И ПЕРЕМЕШИВАНИЕ
    // ============================================================

    private void EncryptAndShuffleDeck()
    {
        EncryptedDeck =
            new List<BigInteger>(
                OriginalDeck);


        foreach (Player player in Players)
        {
            AddLog(
                "ENCRYPT",
                $"{player.Name}: " +
                $"начинает шифрование колоды.");


            // ----------------------------------------------------
            // Каждый игрок шифрует ВСЮ колоду.
            //
            // E(x) = x^k mod p
            // ----------------------------------------------------

            for (int i = 0;
                 i < EncryptedDeck.Count;
                 i++)
            {
                EncryptedDeck[i] =
                    Encrypt(
                        EncryptedDeck[i],
                        player.EncryptionKey);
            }


            // ----------------------------------------------------
            // После шифрования игрок перемешивает колоду.
            // ----------------------------------------------------

            Shuffle(
                EncryptedDeck);


            AddLog(
                "ENCRYPT",
                $"{player.Name}: " +
                $"E(k={player.EncryptionKey}) + " +
                "перемешивание колоды.");
        }
    }


    // ============================================================
    // ПРОВЕРКА ЗАШИФРОВАННОЙ КОЛОДЫ
    // ============================================================

    private void VerifyEncryptedDeck()
    {
        if (EncryptedDeck.Count
            != OriginalDeck.Count)
        {
            throw new InvalidOperationException(
                "Количество карт после шифрования изменилось.");
        }


        /*
         * Очень важная проверка.
         *
         * Шифрование x^k mod p должно быть взаимно однозначным
         * для элементов ненулевого поля по модулю p.
         *
         * Поэтому две разные карты не должны превращаться
         * в одно и то же число.
         */

        if (EncryptedDeck.Distinct().Count()
            != EncryptedDeck.Count)
        {
            throw new InvalidOperationException(
                "После шифрования обнаружены одинаковые карты.");
        }


        AddLog(
            "VERIFY",
            "Зашифрованная колода содержит " +
            $"{EncryptedDeck.Count} уникальных карт.");
    }


    // ============================================================
    // РАЗДАЧА
    // ============================================================

    private void DealCards()
    {
        foreach (Player player in Players)
        {
            player.EncryptedCards.Clear();

            player.Cards.Clear();
        }


        CommunityCards.Clear();


        int position = 0;


        /*
         * Раздаём по одной карте каждому игроку.
         *
         * Например:
         *
         * Игрок 1
         * Игрок 2
         * Игрок 3
         * Игрок 4
         *
         * затем второй круг.
         */

        for (int round = 0;
             round < CardsPerPlayer;
             round++)
        {
            foreach (Player player in Players)
            {
                if (position >= EncryptedDeck.Count)
                {
                    throw new InvalidOperationException(
                        "Попытка раздать больше карт, чем есть в колоде.");
                }


                BigInteger card =
                    EncryptedDeck[position++];


                player.EncryptedCards.Add(
                    card);
            }
        }


        // --------------------------------------------------------
        // Общие карты
        // --------------------------------------------------------

        for (int i = 0;
             i < CommunityCardsCount;
             i++)
        {
            if (position >= EncryptedDeck.Count)
            {
                throw new InvalidOperationException(
                    "Недостаточно карт для общего стола.");
            }


            CommunityCards.Add(
                EncryptedDeck[position++]);
        }


        AddLog(
            "DEAL",
            $"Раздано игрокам: " +
            $"{Players.Count * CardsPerPlayer} карт.");


        AddLog(
            "DEAL",
            $"На стол помещено общих карт: " +
            $"{CommunityCardsCount}.");
    }


    // ============================================================
    // РАСКРЫТИЕ КАРТ ИГРОКОВ
    // ============================================================

    private void RevealPlayerCards()
    {
        foreach (Player player in Players)
        {
            foreach (BigInteger encryptedCard
                     in player.EncryptedCards)
            {
                BigInteger card =
                    RevealCard(
                        encryptedCard,
                        player);


                player.Cards.Add(
                    (int)card);
            }


            AddLog(
                "REVEAL",
                $"{player.Name}: карты расшифрованы.");
        }
    }


    // ============================================================
    // РАСКРЫТИЕ ОДНОЙ КАРТЫ
    // ============================================================

    private BigInteger RevealCard(
        BigInteger encryptedCard,
        Player owner)
    {
        BigInteger result =
            encryptedCard;


        /*
         * На карте находятся слои:
         *
         * E1(E2(E3(...E(x))))
         *
         * Каждый игрок снимает свой слой.
         *
         * Поскольку:
         *
         * (x^a)^b = x^(a*b)
         *
         * порядок расшифрования здесь математически
         * совместим с протоколом.
         */


        foreach (Player player in Players)
        {
            if (player.Id == owner.Id)
                continue;


            result =
                Decrypt(
                    result,
                    player.DecryptionKey);
        }


        // Собственный ключ владельца применяется последним.

        result =
            Decrypt(
                result,
                owner.DecryptionKey);


        return result;
    }


    // ============================================================
    // РАСКРЫТИЕ ОБЩИХ КАРТ
    // ============================================================

    private void RevealCommunityCards()
    {
        List<BigInteger> result =
            new();


        foreach (BigInteger encryptedCard
                 in CommunityCards)
        {
            BigInteger card =
                encryptedCard;


            foreach (Player player in Players)
            {
                card =
                    Decrypt(
                        card,
                        player.DecryptionKey);
            }


            result.Add(
                card);
        }


        CommunityCards =
            result;


        AddLog(
            "REVEAL",
            "Общие карты расшифрованы.");
    }


    // ============================================================
    // ПОЛНАЯ РАСШИФРОВКА КОЛОДЫ
    // ============================================================

    private List<BigInteger> DecryptEntireDeck()
    {
        /*
         * Для проверки протокола восстанавливаем
         * каждую карту полной зашифрованной колоды.
         *
         * Важно:
         *
         * порядок карт после перемешиваний отличается
         * от OriginalDeck.
         *
         * Поэтому сравнивать списки напрямую нельзя.
         *
         * Сравниваем множества карт.
         */

        List<BigInteger> result =
            new();


        foreach (BigInteger encryptedCard
                 in EncryptedDeck)
        {
            BigInteger card =
                encryptedCard;


            foreach (Player player in Players)
            {
                card =
                    Decrypt(
                        card,
                        player.DecryptionKey);
            }


            result.Add(
                card);
        }


        return result;
    }


    // ============================================================
    // ПРОВЕРКА ВСЕГО ПРОТОКОЛА
    // ============================================================

    public bool VerifyGame()
    {
        try
        {
            // ----------------------------------------------------
            // Проверяем игроков
            // ----------------------------------------------------

            if (Players.Count < 2)
            {
                AddLog(
                    "VERIFY",
                    "Ошибка: недостаточно игроков.");

                return false;
            }


            // ----------------------------------------------------
            // Проверяем исходную колоду
            // ----------------------------------------------------

            if (OriginalDeck.Count == 0)
            {
                AddLog(
                    "VERIFY",
                    "Ошибка: исходная колода пуста.");

                return false;
            }


            if (OriginalDeck.Distinct().Count()
                != OriginalDeck.Count)
            {
                AddLog(
                    "VERIFY",
                    "Ошибка: исходная колода содержит повторы.");

                return false;
            }


            // ----------------------------------------------------
            // Проверяем ключи
            // ----------------------------------------------------

            BigInteger phi =
                P - 1;


            foreach (Player player in Players)
            {
                BigInteger check =
                    (player.EncryptionKey *
                     player.DecryptionKey)
                    % phi;


                if (check != 1)
                {
                    AddLog(
                        "VERIFY",
                        $"{player.Name}: " +
                        "ключи не являются взаимно обратными.");

                    return false;
                }
            }


            // ----------------------------------------------------
            // Проверяем полную расшифрованную колоду
            // ----------------------------------------------------

            if (DecryptedDeck.Count
                != OriginalDeck.Count)
            {
                AddLog(
                    "VERIFY",
                    "Ошибка: количество карт " +
                    "после полной расшифровки не совпадает.");

                return false;
            }


            if (DecryptedDeck.Distinct().Count()
                != DecryptedDeck.Count)
            {
                AddLog(
                    "VERIFY",
                    "Ошибка: после полной расшифровки " +
                    "обнаружены повторы.");

                return false;
            }


            // ----------------------------------------------------
            // Проверяем диапазон
            // ----------------------------------------------------

            if (DecryptedDeck.Any(
                    x =>
                        x < 1 ||
                        x > OriginalDeck.Count))
            {
                AddLog(
                    "VERIFY",
                    "Ошибка: после расшифровки " +
                    "найдена карта вне диапазона.");

                return false;
            }


            // ----------------------------------------------------
            // Проверяем, что все исходные карты восстановились
            // ----------------------------------------------------

            HashSet<BigInteger> originalSet =
                OriginalDeck.ToHashSet();


            HashSet<BigInteger> decryptedSet =
                DecryptedDeck.ToHashSet();


            if (!originalSet.SetEquals(
                    decryptedSet))
            {
                AddLog(
                    "VERIFY",
                    "Ошибка: после расшифровки " +
                    "получен другой набор карт.");

                return false;
            }


            // ----------------------------------------------------
            // Проверяем карты игроков + стол
            // ----------------------------------------------------

            List<int> allCards =
                new();


            foreach (Player player in Players)
            {
                allCards.AddRange(
                    player.Cards);
            }


            allCards.AddRange(
                CommunityCards.Select(
                    x => (int)x));


            int expected =
                Players.Count *
                CardsPerPlayer +
                CommunityCardsCount;


            if (allCards.Count != expected)
            {
                AddLog(
                    "VERIFY",
                    $"Ошибка: ожидалось {expected} карт, " +
                    $"получено {allCards.Count}.");

                return false;
            }


            // ----------------------------------------------------
            // Проверяем диапазон выданных карт
            // ----------------------------------------------------

            if (allCards.Any(
                    x =>
                        x < 1 ||
                        x > OriginalDeck.Count))
            {
                AddLog(
                    "VERIFY",
                    "Ошибка: среди выданных карт " +
                    "есть карта вне диапазона.");

                return false;
            }


            // ----------------------------------------------------
            // Проверяем отсутствие дублей среди выданных карт
            // ----------------------------------------------------

            if (allCards.Distinct().Count()
                != allCards.Count)
            {
                AddLog(
                    "VERIFY",
                    "Ошибка: среди выданных карт " +
                    "обнаружены повторы.");

                return false;
            }


            // ----------------------------------------------------
            // Всё успешно
            // ----------------------------------------------------

            AddLog(
                "VERIFY",
                $"OK: {allCards.Count} выданных карт, " +
                "все карты уникальны.");


            AddLog(
                "VERIFY",
                "OK: ключи игроков корректны.");


            AddLog(
                "VERIFY",
                "OK: полная колода успешно восстановлена.");


            AddLog(
                "VERIFY",
                "OK: протокол ментального покера подтверждён.");


            return true;
        }
        catch (Exception ex)
        {
            AddLog(
                "VERIFY",
                $"Исключение: {ex.Message}");

            return false;
        }
    }


    // ============================================================
    // ШИФРОВАНИЕ
    // ============================================================

    private BigInteger Encrypt(
        BigInteger card,
        BigInteger key)
    {
        /*
         * Главное исправление.
         *
         * Было:
         *
         * ModPow(card, key, 1)
         *
         * Из-за этого ВСЕ карты становились 0.
         *
         * Правильно:
         *
         * E(x) = x^k mod p
         */

        return BigInteger.ModPow(
            card,
            key,
            P);
    }


    // ============================================================
    // РАСШИФРОВАНИЕ
    // ============================================================

    private BigInteger Decrypt(
        BigInteger card,
        BigInteger key)
    {
        /*
         * D(x) = x^d mod p
         *
         * где:
         *
         * k * d ≡ 1 (mod p - 1)
         */

        return BigInteger.ModPow(
            card,
            key,
            P);
    }


    // ============================================================
    // ПЕРЕМЕШИВАНИЕ
    // ============================================================

    private void Shuffle(
        List<BigInteger> deck)
    {
        for (int i = deck.Count - 1;
             i > 0;
             i--)
        {
            int j =
                _random.Next(i + 1);


            (deck[i], deck[j]) =
                (deck[j], deck[i]);
        }
    }


    // ============================================================
    // ПРОВЕРКА ПРОСТОГО ЧИСЛА
    // ============================================================

    private static bool IsPrime(
        BigInteger number)
    {
        if (number < 2)
            return false;


        if (number == 2)
            return true;


        if (number.IsEven)
            return false;


        for (BigInteger i = 3;
             i * i <= number;
             i += 2)
        {
            if (number % i == 0)
                return false;
        }


        return true;
    }


    // ============================================================
    // ОБРАТНЫЙ ЭЛЕМЕНТ
    // ============================================================

    private static BigInteger ModInverse(
        BigInteger a,
        BigInteger modulus)
    {
        BigInteger oldR = a;

        BigInteger r = modulus;

        BigInteger oldS = 1;

        BigInteger s = 0;


        while (r != 0)
        {
            BigInteger quotient =
                oldR / r;


            (oldR, r) =
                (r,
                 oldR - quotient * r);


            (oldS, s) =
                (s,
                 oldS - quotient * s);
        }


        if (oldR != 1)
        {
            throw new InvalidOperationException(
                "Обратный элемент не существует.");
        }


        BigInteger result =
            oldS % modulus;


        if (result < 0)
            result += modulus;


        return result;
    }


    // ============================================================
    // СЛУЧАЙНОЕ BIGINTEGER
    // ============================================================

    private static BigInteger RandomBigInteger(
        BigInteger min,
        BigInteger max)
    {
        if (min > max)
            throw new ArgumentException();


        BigInteger range =
            max - min + 1;


        byte[] bytes =
            range.ToByteArray();


        BigInteger value;


        do
        {
            RandomNumberGenerator.Fill(
                bytes);


            bytes[^1] &= 0x7F;


            value =
                new BigInteger(bytes);
        }
        while (value >= range);


        return min + value;
    }


    // ============================================================
    // ЛОГ
    // ============================================================

    private void AddLog(
        string operation,
        string details)
    {
        Log.Add(
            new ProtocolLogEntry
            {
                Time = DateTime.Now,
                Operation = operation,
                Details = details
            });
    }


    // ============================================================
    // НАЗВАНИЕ КАРТЫ
    // ============================================================

    public static string GetCardName(
        int number)
    {
        if (number < 1)
            return "?";


        string[] ranks =
        {
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "J",
            "Q",
            "K",
            "A"
        };


        string[] suits =
        {
            "♠",
            "♥",
            "♦",
            "♣"
        };


        int index =
            number - 1;


        /*
         * Обычная колода содержит 52 карты.
         *
         * Если пользователь указал больше 52,
         * продолжаем показывать:
         *
         * Карта 53
         * Карта 54
         * ...
         */

        if (index >= 52)
            return $"Карта {number}";


        string rank =
            ranks[index % 13];


        string suit =
            suits[index / 13];


        return rank + suit;
    }


    // ============================================================
    // ЦВЕТ КАРТЫ
    // ============================================================

    public static bool IsRedCard(
        int number)
    {
        if (number < 1 ||
            number > 52)
        {
            return false;
        }


        int suit =
            (number - 1) / 13;


        return suit == 1 ||
               suit == 2;
    }
}