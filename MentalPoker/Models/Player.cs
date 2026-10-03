using System.Numerics;

namespace MentalPoker.Models;

public class Player
{
    public int Id { get; }

    public string Name { get; }

    public BigInteger EncryptionKey { get; set; }

    public BigInteger DecryptionKey { get; set; }

    public List<BigInteger> EncryptedCards { get; } = new();

    public List<int> Cards { get; } = new();

    /// <summary>
    /// Игровой баланс игрока.
    /// </summary>
    public int Chips { get; set; } = 5000;

    /// <summary>
    /// Сколько игрок внёс в текущий банк.
    /// </summary>
    public int CurrentBet { get; set; }

    public Player(int id, string name)
    {
        Id = id;
        Name = name;
    }

    public override string ToString()
        => $"{Name} — {Chips:N0}";
}