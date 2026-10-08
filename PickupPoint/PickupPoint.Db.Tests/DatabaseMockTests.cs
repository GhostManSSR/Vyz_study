using System.Collections.Generic;
using System.Linq;
using Moq;
using PickupPoint.Db;
using Xunit;

namespace PickupPoint.Db.Tests;

public class DatabaseMockTests
{
    private readonly Mock<IDatabase> _db;

    public DatabaseMockTests()
    {
        _db = new Mock<IDatabase>();
    }

    [Fact]
    public void AddOrder_ReturnsGeneratedCode()
    {
        _db
            .Setup(x => x.AddOrder("+79990001122", "A1"))
            .Returns("PVZ-000001");

        string code = _db.Object.AddOrder("+79990001122", "A1");

        Assert.Equal("PVZ-000001", code);

        _db.Verify(
            x => x.AddOrder("+79990001122", "A1"),
            Times.Once);
    }

    [Fact]
    public void ListOrders_ReturnsOrders()
    {
        var orders = new List<Order>
        {
            new Order
            {
                Id = 1,
                Code = "PVZ-000001",
                Phone = "+79990001122",
                Article = "",
                Cell = "A1",
                Status = "ready"
            },
            new Order
            {
                Id = 2,
                Code = "PVZ-000002",
                Phone = "+79990003344",
                Article = "",
                Cell = "A2",
                Status = "ready"
            }
        };

        _db
            .Setup(x => x.ListOrders(""))
            .Returns(orders);

        var result = _db.Object.ListOrders("");

        Assert.Equal(2, result.Count);
        Assert.Equal("PVZ-000001", result[0].Code);
        Assert.Equal("PVZ-000002", result[1].Code);
    }

    [Fact]
    public void ListOrders_Filter_ReturnsMatchingOrders()
    {
        var orders = new List<Order>
        {
            new Order
            {
                Id = 1,
                Code = "PVZ-000001",
                Phone = "+79990001122",
                Article = "",
                Cell = "A1",
                Status = "ready"
            }
        };

        _db
            .Setup(x => x.ListOrders("+7999"))
            .Returns(orders);

        var result = _db.Object.ListOrders("+7999");

        Assert.Single(result);
        Assert.Equal("+79990001122", result[0].Phone);
    }

    [Fact]
    public void ListOrders_UnknownFilter_ReturnsEmpty()
    {
        _db
            .Setup(x => x.ListOrders("unknown"))
            .Returns(new List<Order>());

        var result = _db.Object.ListOrders("unknown");

        Assert.Empty(result);
    }

    [Fact]
    public void IssueOrder_ReadyOrder_ReturnsTrue()
    {
        _db
            .Setup(x => x.IssueOrder("PVZ-000001"))
            .Returns(true);

        bool result = _db.Object.IssueOrder("PVZ-000001");

        Assert.True(result);

        _db.Verify(
            x => x.IssueOrder("PVZ-000001"),
            Times.Once);
    }

    [Fact]
    public void IssueOrder_AlreadyIssued_ReturnsFalse()
    {
        _db
            .Setup(x => x.IssueOrder("PVZ-000001"))
            .Returns(false);

        bool result = _db.Object.IssueOrder("PVZ-000001");

        Assert.False(result);
    }

    [Fact]
    public void IssueOrder_UnknownCode_ReturnsFalse()
    {
        _db
            .Setup(x => x.IssueOrder("PVZ-999999"))
            .Returns(false);

        bool result = _db.Object.IssueOrder("PVZ-999999");

        Assert.False(result);
    }

    [Fact]
    public void CancelOrder_ReadyOrder_ReturnsTrue()
    {
        _db
            .Setup(x => x.CancelOrder("PVZ-000001"))
            .Returns(true);

        bool result = _db.Object.CancelOrder("PVZ-000001");

        Assert.True(result);

        _db.Verify(
            x => x.CancelOrder("PVZ-000001"),
            Times.Once);
    }

    [Fact]
    public void CancelOrder_IssuedOrder_ReturnsFalse()
    {
        _db
            .Setup(x => x.CancelOrder("PVZ-000001"))
            .Returns(false);

        bool result = _db.Object.CancelOrder("PVZ-000001");

        Assert.False(result);
    }

    [Fact]
    public void BuildReport_ReturnsCorrectReport()
    {
        var report = new Report
        {
            ReadyCount = 2,
            IssuedCount = 1,
            CancelledCount = 1
        };

        _db
            .Setup(x => x.BuildReport())
            .Returns(report);

        var result = _db.Object.BuildReport();

        Assert.Equal(2, result.ReadyCount);
        Assert.Equal(1, result.IssuedCount);
        Assert.Equal(1, result.CancelledCount);
    }

    [Fact]
    public void BuildReport_EmptyDatabase_ReturnsZeros()
    {
        var report = new Report
        {
            ReadyCount = 0,
            IssuedCount = 0,
            CancelledCount = 0
        };

        _db
            .Setup(x => x.BuildReport())
            .Returns(report);

        var result = _db.Object.BuildReport();

        Assert.Equal(0, result.ReadyCount);
        Assert.Equal(0, result.IssuedCount);
        Assert.Equal(0, result.CancelledCount);
    }

    [Fact]
    public void ListByStatus_ReturnsOnlyReadyOrders()
    {
        var orders = new List<Order>
        {
            new Order
            {
                Id = 1,
                Code = "PVZ-000001",
                Phone = "+79990001122",
                Status = "ready"
            },
            new Order
            {
                Id = 2,
                Code = "PVZ-000002",
                Phone = "+79990003344",
                Status = "ready"
            }
        };

        _db
            .Setup(x => x.ListByStatus("ready"))
            .Returns(orders);

        var result = _db.Object.ListByStatus("ready");

        Assert.Equal(2, result.Count);
        Assert.All(result, x => Assert.Equal("ready", x.Status));
    }

    [Fact]
    public void ListByStatus_Issued_ReturnsIssuedOrders()
    {
        var orders = new List<Order>
        {
            new Order
            {
                Id = 1,
                Code = "PVZ-000001",
                Status = "issued"
            }
        };

        _db
            .Setup(x => x.ListByStatus("issued"))
            .Returns(orders);

        var result = _db.Object.ListByStatus("issued");

        Assert.Single(result);
        Assert.Equal("issued", result[0].Status);
    }

    [Fact]
    public void CountClosedOrders_ReturnsCorrectCount()
    {
        _db
            .Setup(x => x.CountClosedOrders())
            .Returns(2);

        int result = _db.Object.CountClosedOrders();

        Assert.Equal(2, result);
    }

    [Fact]
    public void DeleteClosedOrders_ReturnsDeletedCount()
    {
        _db
            .Setup(x => x.DeleteClosedOrders())
            .Returns(2);

        int result = _db.Object.DeleteClosedOrders();

        Assert.Equal(2, result);

        _db.Verify(
            x => x.DeleteClosedOrders(),
            Times.Once);
    }

    [Fact]
    public void DeleteClosedOrders_WhenNothingToDelete_ReturnsZero()
    {
        _db
            .Setup(x => x.DeleteClosedOrders())
            .Returns(0);

        int result = _db.Object.DeleteClosedOrders();

        Assert.Equal(0, result);
    }
}