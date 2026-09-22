using System.Text;

namespace PlaylistApp;

public class Playlist
{
    private readonly Track[] _tracks; 
    private int _count;             

    public int Capacity { get; }    
    public int Count => _count;      

    public Playlist(int capacity)
    {
        if (capacity <= 0)
            throw new PlaylistException($"недопустимое значение вместимости = {capacity}");

        Capacity = capacity;
        _count = 0;
        _tracks = new Track[capacity];
    }
    
    public Track this[int i]
    {
        get
        {
            CheckIndex(i);
            return _tracks[i];
        }
        set
        {
            CheckIndex(i);
            _tracks[i] = value;
        }
    }

    private void CheckIndex(int i)
    {
        if (i < 0 || i >= _count)
            throw new PlaylistException($"неверное значение индекса i = {i}");
    }
    
    public bool Equals(Playlist? other)
    {
        if (other is null) return false;
        if (ReferenceEquals(this, other)) return true;
        if (_count != other._count) return false;

        for (int i = 0; i < _count; i++)
            if (!_tracks[i].Equals(other._tracks[i]))
                return false;

        return true;
    }

    public override bool Equals(object? obj) => obj is Playlist p && Equals(p);
    public static bool operator ==(Playlist? a, Playlist? b) => Equals(a, b);
    public static bool operator !=(Playlist? a, Playlist? b) => !Equals(a, b);
    
    private bool Contains(Track track)
    {
        for (int i = 0; i < _count; i++)
            if (_tracks[i].Equals(track))
                return true;
        return false;
    }
    
    public void Add(Track track)
    {
        if (_count >= Capacity)
            throw new PlaylistException($"плейлист заполнен, вместимость = {Capacity}");

        if (Contains(track))
            throw new PlaylistException($"трек уже есть в плейлисте: {track.Artist} - {track.Title}");

        _tracks[_count] = track;
        _count++;
    }
    
    public void Merge(Playlist other)
    {
        int toAdd = 0;
        for (int i = 0; i < other._count; i++)
        {
            var t = other._tracks[i];
            if (!Contains(t) && !ContainsInRange(other, i, t))
                toAdd++;
        }

        if (_count + toAdd > Capacity)
            throw new PlaylistException(
                $"недостаточно вместимости: нужно {_count + toAdd}, доступно {Capacity}");

        for (int i = 0; i < other._count; i++)
        {
            var t = other._tracks[i];
            if (!Contains(t))
                Add(t);
        }
    }

    private static bool ContainsInRange(Playlist other, int limit, Track track)
    {
        for (int j = 0; j < limit; j++)
            if (other._tracks[j].Equals(track))
                return true;
        return false;
    }
    
    public void RemoveTracksOf(Playlist other)
    {
        bool anyFound = false;
        for (int i = 0; i < _count; i++)
        {
            if (other.Contains(_tracks[i]))
            {
                anyFound = true;
                break;
            }
        }

        if (!anyFound)
            throw new PlaylistException("ни один трек из other не найден в текущем плейлисте");

        int write = 0;
        for (int read = 0; read < _count; read++)
        {
            if (!other.Contains(_tracks[read]))
            {
                _tracks[write] = _tracks[read];
                write++;
            }
        }
        _count = write;
    }
    
    public int TotalDuration()
    {
        int total = 0;
        for (int i = 0; i < _count; i++)
            total += _tracks[i].DurationSec;
        return total;
    }
    
    public int FindByArtist(string artist)
    {
        for (int i = 0; i < _count; i++)
            if (_tracks[i].Artist == artist)
                return i;

        throw new PlaylistException($"трек исполнителя '{artist}' не найден");
    }
    
    public override string ToString()
    {
        var sb = new StringBuilder();
        for (int i = 0; i < _count; i++)
        {
            var t = _tracks[i];
            int m = t.DurationSec / 60;
            int s = t.DurationSec % 60;
            sb.AppendLine($"{t.Artist} - {t.Title} ({m}:{s:D2})");
        }
        return sb.ToString();
    }
}