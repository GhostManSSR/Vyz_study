namespace PlaylistApp;

public struct Track : IEquatable<Track>
{
    public string Title { get; set; }
    public string Artist { get; set; }
    public int DurationSec { get; set; }

    public Track(string title, string artist, int durationSec)
    {
        Title = title;
        Artist = artist;
        DurationSec = durationSec;
    }

    public bool Equals(Track other) =>
        Title == other.Title && Artist == other.Artist;

    public override bool Equals(object? obj) =>
        obj is Track t && Equals(t);

    public override int GetHashCode() =>
        HashCode.Combine(Title, Artist);

    public static bool operator ==(Track a, Track b) => a.Equals(b);
    public static bool operator !=(Track a, Track b) => !a.Equals(b);
}