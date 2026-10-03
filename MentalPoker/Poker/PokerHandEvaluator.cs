using MentalPoker.Models;

namespace MentalPoker.Poker;

public static class PokerHandEvaluator
{
    // ============================================================
    // ОСНОВНОЙ МЕТОД
    // ============================================================

    public static PokerHandResult Evaluate(
        Player player,
        IEnumerable<int> cards)
    {
        List<int> allCards =
            cards
                .Distinct()
                .ToList();

        if (allCards.Count != 7)
        {
            throw new ArgumentException(
                "Для Texas Hold'em необходимо ровно 7 карт: " +
                "2 карты игрока + 5 общих.");
        }

        if (allCards.Any(c => c < 1 || c > 52))
        {
            throw new ArgumentException(
                "Покерный оценщик поддерживает стандартную " +
                "колоду из 52 карт.");
        }

        PokerHandResult? best = null;

        // --------------------------------------------------------
        // Из 7 карт выбираем все комбинации по 5 карт.
        // Всего C(7,5) = 21 комбинация.
        // --------------------------------------------------------

        for (int a = 0; a < allCards.Count - 4; a++)
        {
            for (int b = a + 1; b < allCards.Count - 3; b++)
            {
                for (int c = b + 1; c < allCards.Count - 2; c++)
                {
                    for (int d = c + 1; d < allCards.Count - 1; d++)
                    {
                        for (int e = d + 1; e < allCards.Count; e++)
                        {
                            List<int> combination =
                            [
                                allCards[a],
                                allCards[b],
                                allCards[c],
                                allCards[d],
                                allCards[e]
                            ];

                            PokerHandResult current =
                                EvaluateFiveCards(
                                    player,
                                    combination);

                            if (best == null ||
                                Compare(
                                    current,
                                    best) > 0)
                            {
                                best = current;
                            }
                        }
                    }
                }
            }
        }

        return best!;
    }


    // ============================================================
    // ОЦЕНКА ПЯТИ КАРТ
    // ============================================================

    private static PokerHandResult EvaluateFiveCards(
        Player player,
        List<int> cards)
    {
        List<int> ranks =
            cards
                .Select(GetRank)
                .OrderByDescending(x => x)
                .ToList();

        List<int> suits =
            cards
                .Select(GetSuit)
                .ToList();

        bool flush =
            suits.Distinct().Count() == 1;

        int straightHigh =
            GetStraightHigh(ranks);

        bool straight =
            straightHigh > 0;

        Dictionary<int, int> groups =
            ranks
                .GroupBy(x => x)
                .ToDictionary(
                    g => g.Key,
                    g => g.Count());

        List<int> four =
            groups
                .Where(x => x.Value == 4)
                .Select(x => x.Key)
                .OrderByDescending(x => x)
                .ToList();

        List<int> triples =
            groups
                .Where(x => x.Value == 3)
                .Select(x => x.Key)
                .OrderByDescending(x => x)
                .ToList();

        List<int> pairs =
            groups
                .Where(x => x.Value == 2)
                .Select(x => x.Key)
                .OrderByDescending(x => x)
                .ToList();


        // ========================================================
        // ROYAL FLUSH
        // ========================================================

        if (flush &&
            straight &&
            straightHigh == 14)
        {
            return new PokerHandResult(
                player,
                PokerHandCategory.RoyalFlush,
                cards,
                "Роял-флеш",
                [10, 14]);
        }


        // ========================================================
        // STRAIGHT FLUSH
        // ========================================================

        if (flush && straight)
        {
            return new PokerHandResult(
                player,
                PokerHandCategory.StraightFlush,
                cards,
                $"Стрит-флеш, старшая карта {RankName(straightHigh)}",
                [9, straightHigh]);
        }


        // ========================================================
        // FOUR OF A KIND
        // ========================================================

        if (four.Count > 0)
        {
            int fourRank = four[0];

            int kicker =
                ranks
                    .Where(x => x != fourRank)
                    .First();

            return new PokerHandResult(
                player,
                PokerHandCategory.FourOfAKind,
                cards,
                $"Каре {RankName(fourRank)}",
                [8, fourRank, kicker]);
        }


        // ========================================================
        // FULL HOUSE
        // ========================================================

        if (triples.Count > 0 &&
            (pairs.Count > 0 ||
             triples.Count >= 2))
        {
            int tripleRank =
                triples[0];

            int pairRank;

            if (triples.Count >= 2)
            {
                pairRank =
                    Math.Max(
                        triples[1],
                        pairs.Count > 0
                            ? pairs[0]
                            : 0);
            }
            else
            {
                pairRank = pairs[0];
            }

            return new PokerHandResult(
                player,
                PokerHandCategory.FullHouse,
                cards,
                $"Фулл-хаус: {RankName(tripleRank)} + {RankName(pairRank)}",
                [7, tripleRank, pairRank]);
        }


        // ========================================================
        // FLUSH
        // ========================================================

        if (flush)
        {
            return new PokerHandResult(
                player,
                PokerHandCategory.Flush,
                cards,
                $"Флеш, старшая карта {RankName(ranks[0])}",
                new[] { 6 }
                    .Concat(ranks)
                    .ToArray());
        }


        // ========================================================
        // STRAIGHT
        // ========================================================

        if (straight)
        {
            return new PokerHandResult(
                player,
                PokerHandCategory.Straight,
                cards,
                $"Стрит, старшая карта {RankName(straightHigh)}",
                [5, straightHigh]);
        }


        // ========================================================
        // THREE OF A KIND
        // ========================================================

        if (triples.Count > 0)
        {
            int tripleRank =
                triples[0];

            int[] kickers =
                ranks
                    .Where(x => x != tripleRank)
                    .Take(2)
                    .ToArray();

            return new PokerHandResult(
                player,
                PokerHandCategory.ThreeOfAKind,
                cards,
                $"Сет / тройка {RankName(tripleRank)}",
                [4, tripleRank, kickers[0], kickers[1]]);
        }


        // ========================================================
        // TWO PAIR
        // ========================================================

        if (pairs.Count >= 2)
        {
            int highPair = pairs[0];
            int lowPair = pairs[1];

            int kicker =
                ranks
                    .Where(x =>
                        x != highPair &&
                        x != lowPair)
                    .First();

            return new PokerHandResult(
                player,
                PokerHandCategory.TwoPair,
                cards,
                $"Две пары: {RankName(highPair)} и {RankName(lowPair)}",
                [3, highPair, lowPair, kicker]);
        }


        // ========================================================
        // ONE PAIR
        // ========================================================

        if (pairs.Count == 1)
        {
            int pairRank =
                pairs[0];

            int[] kickers =
                ranks
                    .Where(x => x != pairRank)
                    .Take(3)
                    .ToArray();

            return new PokerHandResult(
                player,
                PokerHandCategory.OnePair,
                cards,
                $"Пара {RankName(pairRank)}",
                [
                    2,
                    pairRank,
                    kickers[0],
                    kickers[1],
                    kickers[2]
                ]);
        }


        // ========================================================
        // HIGH CARD
        // ========================================================

        return new PokerHandResult(
            player,
            PokerHandCategory.HighCard,
            cards,
            $"Старшая карта {RankName(ranks[0])}",
            new[] { 1 }
                .Concat(ranks)
                .ToArray());
    }


    // ============================================================
    // СРАВНЕНИЕ РУК
    // ============================================================

    private static int Compare(
        PokerHandResult a,
        PokerHandResult b)
    {
        int length =
            Math.Min(
                a.ComparisonValues.Length,
                b.ComparisonValues.Length);

        for (int i = 0; i < length; i++)
        {
            if (a.ComparisonValues[i] >
                b.ComparisonValues[i])
            {
                return 1;
            }

            if (a.ComparisonValues[i] <
                b.ComparisonValues[i])
            {
                return -1;
            }
        }

        return
            a.ComparisonValues.Length.CompareTo(
                b.ComparisonValues.Length);
    }


    // ============================================================
    // ОПРЕДЕЛЕНИЕ ПОБЕДИТЕЛЕЙ
    // ============================================================

    public static PokerGameResult DetermineWinners(
        List<Player> players,
        List<int> communityCards,
        int pot)
    {
        PokerGameResult gameResult =
            new PokerGameResult(pot);

        foreach (Player player in players)
        {
            List<int> sevenCards =
                player.Cards
                    .Concat(communityCards)
                    .ToList();

            PokerHandResult result =
                Evaluate(
                    player,
                    sevenCards);

            gameResult.Results.Add(result);
        }


        if (gameResult.Results.Count == 0)
        {
            return gameResult;
        }


        PokerHandResult best =
            gameResult.Results[0];

        foreach (PokerHandResult result
                 in gameResult.Results.Skip(1))
        {
            if (Compare(result, best) > 0)
            {
                best = result;
            }
        }


        foreach (PokerHandResult result
                 in gameResult.Results)
        {
            if (Compare(result, best) == 0)
            {
                gameResult.Winners.Add(
                    result.Player);
            }
        }


        if (gameResult.Winners.Count > 0 &&
            pot > 0)
        {
            gameResult.PrizePerWinner =
                pot / gameResult.Winners.Count;

            gameResult.RemainingPrize =
                pot % gameResult.Winners.Count;
        }


        return gameResult;
    }


    // ============================================================
    // СТАРШАЯ КАРТА СТРИТА
    // ============================================================

    private static int GetStraightHigh(
        List<int> ranks)
    {
        List<int> unique =
            ranks
                .Distinct()
                .OrderByDescending(x => x)
                .ToList();

        if (unique.Contains(14))
        {
            unique.Add(1);
        }

        for (int i = 0;
             i <= unique.Count - 5;
             i++)
        {
            bool straight = true;

            for (int j = 0; j < 4; j++)
            {
                if (unique[i + j] -
                    unique[i + j + 1] != 1)
                {
                    straight = false;
                    break;
                }
            }

            if (straight)
            {
                int high = unique[i];

                if (high == 1)
                    return 5;

                return high;
            }
        }

        return 0;
    }


    // ============================================================
    // КАРТА -> RANK
    // ============================================================

    public static int GetRank(int cardId)
    {
        return ((cardId - 1) % 13) + 2;
    }


    // ============================================================
    // КАРТА -> MAST
    // ============================================================

    public static int GetSuit(int cardId)
    {
        return (cardId - 1) / 13;
    }


    // ============================================================
    // НАЗВАНИЕ RANK
    // ============================================================

    public static string RankName(int rank)
    {
        return rank switch
        {
            14 => "Туз",
            13 => "Король",
            12 => "Дама",
            11 => "Валет",
            10 => "10",
            9 => "9",
            8 => "8",
            7 => "7",
            6 => "6",
            5 => "5",
            4 => "4",
            3 => "3",
            2 => "2",
            _ => rank.ToString()
        };
    }
}