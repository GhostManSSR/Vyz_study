using MentalPoker.Models;

namespace MentalPoker.Poker;

public class PokerGameResult
{
    public List<PokerHandResult> Results { get; } = new();

    public List<Player> Winners { get; } = new();

    public int Pot { get; }

    public int PrizePerWinner { get; set; }

    public int RemainingPrize { get; set; }

    public PokerGameResult(int pot)
    {
        Pot = pot;
    }
}