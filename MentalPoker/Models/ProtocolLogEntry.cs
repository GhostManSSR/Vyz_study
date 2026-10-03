namespace MentalPoker.Models;

public class ProtocolLogEntry
{
    public DateTime Time { get; set; }

    public string Operation { get; set; } = string.Empty;

    public string Details { get; set; } = string.Empty;

    public override string ToString()
    {
        return $"[{Time:HH:mm:ss.fff}] {Operation}: {Details}";
    }
}