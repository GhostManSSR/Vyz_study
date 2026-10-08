namespace PickupPoint.Db;

public interface IDatabase : IDisposable
{
    List<Order> ListOrders(string filter = "");

    List<Order> ListByStatus(string status);

    string AddOrder(string contact, string cell);

    bool IssueOrder(string code);

    bool CancelOrder(string code);

    Report BuildReport();

    int CountClosedOrders();

    int DeleteClosedOrders();
}