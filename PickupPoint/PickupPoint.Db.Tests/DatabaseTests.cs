using System;
using System.Linq;
using PickupPoint.Db;
using Xunit;

namespace PickupPoint.Db.Tests;

public class DatabaseTests : IDisposable
{
    private readonly Database _db;

    public DatabaseTests()
    {
        string conn = Environment.GetEnvironmentVariable("PICKUP_DB_TEST")
            ?? throw new InvalidOperationException(
                "Задайте PICKUP_DB_TEST (например, Host=localhost;Database=pickup_test;Username=pickup_user;Password=pickup_pass)");

        _db = new Database(conn);
        _db.DeleteClosedOrders();              
        foreach (var o in _db.ListOrders(""))  
            _db.IssueOrder(o.Code);           
        _db.DeleteClosedOrders();
    }

    public void Dispose() => _db.Dispose();


    [Fact]
    public void AddOrder_ByPhone_SetsPhone_NotArticle()
    {
        string code = _db.AddOrder("+79990001122", "A1");

        Assert.StartsWith("PVZ-", code);
        var o = _db.ListOrders("").Single();
        Assert.Equal("+79990001122", o.Phone);
        Assert.Equal("", o.Article);
        Assert.Equal("A1", o.Cell);
        Assert.Equal("ready", o.Status);
    }

    [Fact]
    public void AddOrder_ByArticle_SetsArticle_NotPhone()
    {
        _db.AddOrder("ART-123456789", "");
        var o = _db.ListOrders("").Single();

        Assert.Equal("", o.Phone);
        Assert.Equal("ART-123456789", o.Article);
        Assert.Equal("", o.Cell);
    }

    [Fact]
    public void AddOrder_GeneratesSequentialCodes()
    {
        string a = _db.AddOrder("+79990001122", "");
        string b = _db.AddOrder("+79990003344", "");

        Assert.Equal("PVZ-000001", a);
        Assert.Equal("PVZ-000002", b);
    }


    [Fact]
    public void ListOrders_FilterByPhone_Finds()
    {
        _db.AddOrder("+79990001122", "");
        _db.AddOrder("+79995556677", "");

        var result = _db.ListOrders("+7999");
        Assert.Equal(2, result.Count);

        result = _db.ListOrders("5556");
        Assert.Single(result);
    }

    [Fact]
    public void ListOrders_FilterByArticle_Finds()
    {
        _db.AddOrder("ART-123", "");
        _db.AddOrder("+79990001122", "");

        var result = _db.ListOrders("ART-");
        Assert.Single(result);
        Assert.Equal("ART-123", result[0].Article);
    }

    [Fact]
    public void ListOrders_FilterIsCaseInsensitive()
    {
        _db.AddOrder("art-abc", "");
        Assert.Single(_db.ListOrders("ART-ABC"));
    }

    [Fact]
    public void ListOrders_Empty_ReturnsAll()
    {
        _db.AddOrder("+79990001122", "");
        _db.AddOrder("+79990003344", "");
        Assert.Equal(2, _db.ListOrders("").Count);
    }


    [Fact]
    public void IssueOrder_Ready_ReturnsTrue_AndChangesStatus()
    {
        string code = _db.AddOrder("+79990001122", "");

        Assert.True(_db.IssueOrder(code));
        Assert.Equal("issued", _db.ListOrders("").Single().Status);
    }

    [Fact]
    public void IssueOrder_AlreadyIssued_ReturnsFalse()
    {
        string code = _db.AddOrder("+79990001122", "");
        _db.IssueOrder(code);

        Assert.False(_db.IssueOrder(code));
    }

    [Fact]
    public void IssueOrder_UnknownCode_ReturnsFalse()
    {
        Assert.False(_db.IssueOrder("PVZ-999999"));
    }


    [Fact]
    public void CancelOrder_Ready_ReturnsTrue()
    {
        string code = _db.AddOrder("+79990001122", "");
        Assert.True(_db.CancelOrder(code));
        Assert.Equal("cancelled", _db.ListOrders("").Single().Status);
    }

    [Fact]
    public void CancelOrder_Issued_ReturnsFalse()
    {
        string code = _db.AddOrder("+79990001122", "");
        _db.IssueOrder(code);

        Assert.False(_db.CancelOrder(code));
    }


    [Fact]
    public void BuildReport_CountsCorrectly()
    {
        string a = _db.AddOrder("+79990001122", "");
        string b = _db.AddOrder("+79990003344", "");
        string c = _db.AddOrder("+79990005566", "");
        string d = _db.AddOrder("+79990007788", "");

        _db.IssueOrder(a);
        _db.CancelOrder(b);

        var r = _db.BuildReport();
        Assert.Equal(2, r.ReadyCount);      // c, d
        Assert.Equal(1, r.IssuedCount);     // a
        Assert.Equal(1, r.CancelledCount);  // b
    }

    [Fact]
    public void BuildReport_EmptyDb_AllZeros()
    {
        var r = _db.BuildReport();
        Assert.Equal(0, r.ReadyCount);
        Assert.Equal(0, r.IssuedCount);
        Assert.Equal(0, r.CancelledCount);
    }


    [Fact]
    public void ListByStatus_ReturnsOnlyMatching()
    {
        string a = _db.AddOrder("+79990001122", "");
        string b = _db.AddOrder("+79990003344", "");
        _db.IssueOrder(a);

        Assert.Single(_db.ListByStatus("ready"));      // b
        Assert.Single(_db.ListByStatus("issued"));     // a
        Assert.Empty(_db.ListByStatus("cancelled"));
        Assert.Equal(2, _db.ListByStatus("").Count);   // все
    }


    [Fact]
    public void CountAndDeleteClosedOrders()
    {
        string a = _db.AddOrder("+79990001122", "");
        string b = _db.AddOrder("+79990003344", "");
        string c = _db.AddOrder("+79990005566", "");
        _db.IssueOrder(a);
        _db.CancelOrder(b);

        Assert.Equal(2, _db.CountClosedOrders());
        Assert.Equal(2, _db.DeleteClosedOrders());

        var rest = _db.ListOrders("");
        Assert.Single(rest);
        Assert.Equal(c, rest[0].Code);
    }


    [Fact]
    public void ListOrders_SqlInjection_ReturnsEmpty()
    {
        _db.AddOrder("+79990001122", "");
        var result = _db.ListOrders("' OR '1'='1");
        Assert.Empty(result);
    }
}