using MentalPoker.Models;

namespace MentalPoker.Poker;

public class PokerHandResult
{
    public Player Player { get; }

    public PokerHandCategory Category { get; }

    public List<int> BestCards { get; }

    public string Description { get; }

    public int[] ComparisonValues { get; }

    public PokerHandResult(
        Player player,
        PokerHandCategory category,
        List<int> bestCards,
        string description,
        int[] comparisonValues)
    {
        Player = player;
        Category = category;
        BestCards = bestCards;
        Description = description;
        ComparisonValues = comparisonValues;
    }

    public override string ToString()
    {
        return $"{Player.Name}: {Description}";
    }
}