using System;
using System.Collections.Generic;
using Npgsql;
using System.Data;
using System.Globalization;

namespace PickupPoint.Db;

public class Database : IDisposable
{
    private readonly NpgsqlConnection _conn;

    public Database(string connString)
    {
        _conn = new NpgsqlConnection(connString);
        _conn.Open();
        if (_conn.State != ConnectionState.Open)
            throw new InvalidOperationException(
                "Не удалось подключиться к PostgreSQL");

        EnsureSchema();
    }

    private void EnsureSchema()
    {
        const string sql = """
                           CREATE TABLE IF NOT EXISTS orders (
                               id              SERIAL PRIMARY KEY,
                               order_code      VARCHAR(30) UNIQUE NOT NULL,
                               phone           VARCHAR(20),
                               parcel_article  VARCHAR(50),
                               cell            VARCHAR(30),
                               status          VARCHAR(20) NOT NULL DEFAULT 'ready',
                               created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                               issued_at       TIMESTAMP
                           );
                           """;

        using var cmd = new NpgsqlCommand(sql, _conn);
        cmd.ExecuteNonQuery();
    }

    public void Dispose() => _conn.Dispose();

    private static bool LooksLikePhone(string s)
    {
        if (string.IsNullOrEmpty(s)) return false;
        int digits = 0;
        foreach (char c in s)
        {
            if (char.IsDigit(c)) { digits++; continue; }
            if (c is '+' or '-' or ' ' or '(' or ')') continue;
            return false;
        }
        return digits >= 10;
    }

    public List<Order> ListOrders(string filter = "")
    {
        var result = new List<Order>();
        using var cmd = new NpgsqlCommand { Connection = _conn };

        string sql = """
                     SELECT id,
                            order_code,
                            COALESCE(phone, '')          AS phone,
                            COALESCE(parcel_article, '') AS parcel_article,
                            COALESCE(cell, '')           AS cell,
                            status,
                            TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI') AS created_at
                     FROM orders
                     """;

        if (!string.IsNullOrEmpty(filter))
        {
            sql += """
                    WHERE order_code ILIKE @p
                       OR COALESCE(phone, '')          ILIKE @p
                       OR COALESCE(parcel_article, '') ILIKE @p
                   """;
            cmd.Parameters.AddWithValue("p", "%" + filter + "%");
        }

        sql += " ORDER BY created_at DESC, id DESC";
        cmd.CommandText = sql;

        using var reader = cmd.ExecuteReader();
        while (reader.Read())
        {
            result.Add(new Order
            {
                Id        = reader.GetInt32(0),
                Code      = reader.GetString(1),
                Phone     = reader.GetString(2),
                Article   = reader.GetString(3),
                Cell      = reader.GetString(4),
                Status    = reader.GetString(5),
                CreatedAt = reader.GetString(6)
            });
        }
        return result;
    }

    public List<Order> ListByStatus(string status)
    {
        var result = new List<Order>();
        using var cmd = new NpgsqlCommand { Connection = _conn };

        string sql = """
                     SELECT id,
                            order_code,
                            COALESCE(phone, '')          AS phone,
                            COALESCE(parcel_article, '') AS parcel_article,
                            COALESCE(cell, '')           AS cell,
                            status,
                            TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI') AS created_at
                     FROM orders
                     """;

        if (!string.IsNullOrEmpty(status))
        {
            sql += " WHERE status = @s";
            cmd.Parameters.AddWithValue("s", status);
        }

        sql += " ORDER BY created_at DESC, id DESC";
        cmd.CommandText = sql;

        using var reader = cmd.ExecuteReader();
        while (reader.Read())
        {
            result.Add(new Order
            {
                Id        = reader.GetInt32(0),
                Code      = reader.GetString(1),
                Phone     = reader.GetString(2),
                Article   = reader.GetString(3),
                Cell      = reader.GetString(4),
                Status    = reader.GetString(5),
                CreatedAt = reader.GetString(6)
            });
        }

        return result;
    }

    public string AddOrder(string contact, string cell)
    {
        bool isPhone   = LooksLikePhone(contact);
        string phone   = isPhone ? contact : "";
        string article = isPhone ? "" : contact;

        using var tx = _conn.BeginTransaction();

        string tempCode = "TMP-" + Guid.NewGuid().ToString("N")[..24];

        int newId;
        using (var cmd = new NpgsqlCommand(
            """
            INSERT INTO orders (order_code, phone, parcel_article, cell, status)
            VALUES (@code, @phone, @article, @cell, 'ready')
            RETURNING id
            """, _conn, tx))
        {
            cmd.Parameters.AddWithValue("code",    tempCode);
            cmd.Parameters.AddWithValue("phone",   phone);
            cmd.Parameters.AddWithValue("article", article);
            cmd.Parameters.AddWithValue("cell",    cell);

            newId = Convert.ToInt32(cmd.ExecuteScalar(),
                CultureInfo.InvariantCulture);
        }

        string finalCode = $"PVZ-{newId:D6}";

        using (var cmd = new NpgsqlCommand(
            "UPDATE orders SET order_code = @code WHERE id = @id",
            _conn, tx))
        {
            cmd.Parameters.AddWithValue("code", finalCode);
            cmd.Parameters.AddWithValue("id",   newId);
            cmd.ExecuteNonQuery();
        }

        tx.Commit();
        return finalCode;
    }

    public bool IssueOrder(string code)
    {
        using var tx = _conn.BeginTransaction();

        string? status;
        using (var cmd = new NpgsqlCommand(
            "SELECT status FROM orders WHERE order_code = @c", _conn, tx))
        {
            cmd.Parameters.AddWithValue("c", code);
            status = cmd.ExecuteScalar() as string;
        }

        if (status != "ready")
        {
            tx.Rollback();
            return false;
        }

        using (var cmd = new NpgsqlCommand(
            """
            UPDATE orders
               SET status = 'issued', issued_at = NOW()
             WHERE order_code = @c AND status = 'ready'
            """, _conn, tx))
        {
            cmd.Parameters.AddWithValue("c", code);
            cmd.ExecuteNonQuery();
        }

        tx.Commit();
        return true;
    }
    public bool CancelOrder(string code)
    {
        using var tx = _conn.BeginTransaction();

        string? status;
        using (var cmd = new NpgsqlCommand(
            "SELECT status FROM orders WHERE order_code = @c", _conn, tx))
        {
            cmd.Parameters.AddWithValue("c", code);
            status = cmd.ExecuteScalar() as string;
        }

        if (status != "ready")
        {
            tx.Rollback();
            return false;
        }

        using (var cmd = new NpgsqlCommand(
            "UPDATE orders SET status = 'cancelled' WHERE order_code = @c",
            _conn, tx))
        {
            cmd.Parameters.AddWithValue("c", code);
            cmd.ExecuteNonQuery();
        }

        tx.Commit();
        return true;
    }

    public Report BuildReport()
    {
        var report = new Report();
        using var cmd = new NpgsqlCommand(
            "SELECT status, COUNT(*) FROM orders GROUP BY status",
            _conn);

        using var reader = cmd.ExecuteReader();
        while (reader.Read())
        {
            string s = reader.GetString(0);
            int    n = (int)reader.GetInt64(1);
            switch (s)
            {
                case "ready":     report.ReadyCount     = n; break;
                case "issued":    report.IssuedCount    = n; break;
                case "cancelled": report.CancelledCount = n; break;
            }
        }

        return report;
    }

    public int CountClosedOrders()
    {
        using var tx  = _conn.BeginTransaction();
        using var cmd = new NpgsqlCommand(
            "SELECT COUNT(*) FROM orders WHERE status IN ('issued', 'cancelled')",
            _conn, tx);

        int n = Convert.ToInt32(cmd.ExecuteScalar(), CultureInfo.InvariantCulture);
        tx.Commit();
        return n;
    }

    public int DeleteClosedOrders()
    {
        using var tx  = _conn.BeginTransaction();
        using var cmd = new NpgsqlCommand(
            "DELETE FROM orders WHERE status IN ('issued', 'cancelled')",
            _conn, tx);

        int affected = cmd.ExecuteNonQuery();
        tx.Commit();
        return affected;
    }
}