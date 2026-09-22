using PlaylistApp;
using Xunit;

public class PlaylistTests
{
    private static Track track(string title, string artist, int dur = 100)
        => new Track(title, artist, dur);

    [Theory]
    [InlineData(0)]
    [InlineData(-1)]
    [InlineData(-100)]
    public void Ctor_InvalidCapacity_Throws(int cap)
        => Assert.Throws<PlaylistException>(() => new Playlist(cap));

    [Fact]
    public void Ctor_Valid_SetsProperties()
    {
        var p = new Playlist(5);
        Assert.Equal(5, p.Capacity);
        Assert.Equal(0, p.Count);
    }

    [Fact]
    public void Indexer_Get_ReturnsTrack()
    {
        var p = new Playlist(2);
        p.Add(track("A", "X"));
        Assert.Equal("A", p[0].Title);
    }

    [Fact]
    public void Indexer_Set_ReplacesTrack()
    {
        var p = new Playlist(2);
        p.Add(track("A", "X"));
        p[0] = track("B", "Y");
        Assert.Equal("B", p[0].Title);
    }

    [Theory]
    [InlineData(-1)]
    [InlineData(0)]
    [InlineData(5)]
    public void Indexer_OutOfRange_Throws(int i)
    {
        var p = new Playlist(2);
        if (i != 0) p.Add(track("A", "X"));
        Assert.Throws<PlaylistException>(() => p[i]);
        Assert.Throws<PlaylistException>(() => p[i] = track("B", "Y"));
    }

    [Fact]
    public void Add_IncrementsCount()
    {
        var p = new Playlist(2);
        p.Add(track("A", "X"));
        Assert.Equal(1, p.Count);
    }

    [Fact]
    public void Add_Overflow_Throws()
    {
        var p = new Playlist(1);
        p.Add(track("A", "X"));
        Assert.Throws<PlaylistException>(() => p.Add(track("B", "Y")));
    }

    [Fact]
    public void Add_Duplicate_Throws()
    {
        var p = new Playlist(2);
        p.Add(track("A", "X", 100));
        Assert.Throws<PlaylistException>(() => p.Add(track("A", "X", 999)));
    }

    [Fact]
    public void Eq_SameTracks_True()
    {
        var a = new Playlist(2); a.Add(track("A", "X"));
        var b = new Playlist(2); b.Add(track("A", "X"));
        Assert.True(a == b);
    }

    [Fact]
    public void Eq_DifferentCount_False()
    {
        var a = new Playlist(2); a.Add(track("A", "X"));
        var b = new Playlist(2);
        Assert.False(a == b);
    }

    [Fact]
    public void Eq_DifferentOrder_False()
    {
        var a = new Playlist(2); a.Add(track("A", "X")); a.Add(track("B", "Y"));
        var b = new Playlist(2); b.Add(track("B", "Y")); b.Add(track("A", "X"));
        Assert.False(a == b);
    }

    [Fact]
    public void Merge_AddsUnique()
    {
        var a = new Playlist(5); a.Add(track("A", "X"));
        var b = new Playlist(5); b.Add(track("B", "Y"));
        a.Merge(b);
        Assert.Equal(2, a.Count);
    }

    [Fact]
    public void Merge_SkipsDuplicates()
    {
        var a = new Playlist(5); a.Add(track("A", "X"));
        var b = new Playlist(5); b.Add(track("A", "X")); b.Add(track("B", "Y"));
        a.Merge(b);
        Assert.Equal(2, a.Count);
    }

    [Fact]
    public void Merge_InsufficientCapacity_Throws()
    {
        var a = new Playlist(2); a.Add(track("A", "X"));
        var b = new Playlist(5); b.Add(track("B", "Y")); b.Add(track("C", "Z"));
        Assert.Throws<PlaylistException>(() => a.Merge(b));
    }

    [Fact]
    public void RemoveTracksOf_RemovesMatching()
    {
        var a = new Playlist(5);
        a.Add(track("A", "X")); a.Add(track("B", "Y")); a.Add(track("C", "Z"));
        var b = new Playlist(5); b.Add(track("B", "Y"));
        a.RemoveTracksOf(b);
        Assert.Equal(2, a.Count);
        Assert.Equal("A", a[0].Title);
        Assert.Equal("C", a[1].Title);
    }

    [Fact]
    public void RemoveTracksOf_NothingMatched_Throws()
    {
        var a = new Playlist(5); a.Add(track("A", "X"));
        var b = new Playlist(5); b.Add(track("B", "Y"));
        Assert.Throws<PlaylistException>(() => a.RemoveTracksOf(b));
    }

    [Fact]
    public void RemoveTracksOf_RemovesAll()
    {
        var a = new Playlist(5); a.Add(track("A", "X")); a.Add(track("B", "Y"));
        var b = new Playlist(5); b.Add(track("A", "X")); b.Add(track("B", "Y"));
        a.RemoveTracksOf(b);
        Assert.Equal(0, a.Count);
    }

    [Fact]
    public void TotalDuration_Empty_Zero()
        => Assert.Equal(0, new Playlist(3).TotalDuration());

    [Fact]
    public void TotalDuration_SumOfAll()
    {
        var p = new Playlist(3);
        p.Add(track("A", "X", 200));
        p.Add(track("B", "Y", 100));
        Assert.Equal(300, p.TotalDuration());
    }

    [Fact]
    public void FindByArtist_ReturnsFirstIndex()
    {
        var p = new Playlist(3);
        p.Add(track("A", "X"));
        p.Add(track("B", "Y"));
        Assert.Equal(1, p.FindByArtist("Y"));
    }

    [Fact]
    public void FindByArtist_NotFound_Throws()
    {
        var p = new Playlist(3);
        p.Add(track("A", "X"));
        Assert.Throws<PlaylistException>(() => p.FindByArtist("Z"));
    }

    [Fact]
    public void ToString_FormatsMmSs()
    {
        var p = new Playlist(3);
        p.Add(track("Song", "Artist", 125)); // 2:05
        Assert.Contains("Artist - Song (2:05)", p.ToString());
    }

    [Fact]
    public void ToString_Empty_ReturnsEmpty()
        => Assert.Equal("", new Playlist(3).ToString());
}