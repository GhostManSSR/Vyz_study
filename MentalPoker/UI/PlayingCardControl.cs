using System.ComponentModel;
using System.Drawing.Drawing2D;
using MentalPoker.Crypto;

namespace MentalPoker.UI;

public class PlayingCardControl : Control
{
    private int _cardId;
    private bool _isHidden = true;

    [Browsable(false)]
    [DesignerSerializationVisibility(
        DesignerSerializationVisibility.Hidden)]
    public int CardId
    {
        get => _cardId;
        set
        {
            _cardId = value;
            Invalidate();
        }
    }

    [Browsable(false)]
    [DesignerSerializationVisibility(
        DesignerSerializationVisibility.Hidden)]
    public bool IsHidden
    {
        get => _isHidden;
        set
        {
            _isHidden = value;
            Invalidate();
        }
    }

    public PlayingCardControl()
    {
        SetStyle(
            ControlStyles.UserPaint |
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Width = 64;
        Height = 88;

        BackColor = Color.White;
        Cursor = Cursors.Hand;
    }

    protected override void OnPaint(PaintEventArgs e)
    {
        base.OnPaint(e);

        Graphics g = e.Graphics;

        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.InterpolationMode = InterpolationMode.HighQualityBicubic;
        g.PixelOffsetMode = PixelOffsetMode.HighQuality;

        Rectangle rect = new Rectangle(
            1,
            1,
            Width - 4,
            Height - 5);

        // Тень карты
        using Brush shadow = new SolidBrush(
            Color.FromArgb(70, 0, 0, 0));

        g.FillRoundedRectangle(
            shadow,
            new Rectangle(
                rect.X + 3,
                rect.Y + 4,
                rect.Width,
                rect.Height),
            8);

        // Рубашка
        if (IsHidden)
        {
            DrawBack(g, rect);
            return;
        }

        // Лицевая сторона
        using Brush whiteBrush =
            new SolidBrush(Color.White);

        g.FillRoundedRectangle(
            whiteBrush,
            rect,
            8);

        using Pen border =
            new Pen(
                Color.FromArgb(180, 180, 180),
                1);

        g.DrawRoundedRectangle(
            border,
            rect,
            8);

        string text =
            MentalPokerProtocol.GetCardName(CardId);

        bool red =
            MentalPokerProtocol.IsRedCard(CardId);

        using Brush textBrush =
            new SolidBrush(
                red
                    ? Color.Firebrick
                    : Color.FromArgb(25, 25, 25));

        using Font font =
            new Font(
                "Segoe UI",
                GetFontSize(),
                FontStyle.Bold);

        StringFormat format =
            new StringFormat
            {
                Alignment = StringAlignment.Center,
                LineAlignment = StringAlignment.Center
            };

        g.DrawString(
            text,
            font,
            textBrush,
            rect,
            format);
    }

    private float GetFontSize()
    {
        if (CardId <= 0)
            return 17;

        string text =
            MentalPokerProtocol.GetCardName(CardId);

        return text.Length >= 3
            ? 13
            : 17;
    }

    private static void DrawBack(
        Graphics g,
        Rectangle rect)
    {
        // Основной фон рубашки
        using Brush brush =
            new SolidBrush(
                Color.FromArgb(28, 66, 105));

        g.FillRoundedRectangle(
            brush,
            rect,
            8);

        // Внешняя рамка
        using Pen border =
            new Pen(
                Color.White,
                2);

        g.DrawRoundedRectangle(
            border,
            rect,
            8);

        // Внутренняя область
        Rectangle inner =
            new Rectangle(
                rect.X + 7,
                rect.Y + 7,
                rect.Width - 14,
                rect.Height - 14);

        using Brush pattern =
            new HatchBrush(
                HatchStyle.DiagonalCross,
                Color.FromArgb(
                    90,
                    220,
                    230,
                    240),
                Color.Transparent);

        g.FillRoundedRectangle(
            pattern,
            inner,
            5);

        // Центральный ромб
        Point center =
            new Point(
                rect.X + rect.Width / 2,
                rect.Y + rect.Height / 2);

        int size = 12;

        Point[] diamond =
        {
            new Point(center.X, center.Y - size),
            new Point(center.X + size, center.Y),
            new Point(center.X, center.Y + size),
            new Point(center.X - size, center.Y)
        };

        using Brush diamondBrush =
            new SolidBrush(
                Color.FromArgb(
                    170,
                    235,
                    240,
                    245));

        g.FillPolygon(
            diamondBrush,
            diamond);

        using Pen diamondPen =
            new Pen(
                Color.White,
                1);

        g.DrawPolygon(
            diamondPen,
            diamond);
    }
}

internal static class GraphicsExtensions
{
    public static void FillRoundedRectangle(
        this Graphics graphics,
        Brush brush,
        Rectangle rectangle,
        int radius)
    {
        using GraphicsPath path =
            CreateRoundedPath(
                rectangle,
                radius);

        graphics.FillPath(
            brush,
            path);
    }

    public static void DrawRoundedRectangle(
        this Graphics graphics,
        Pen pen,
        Rectangle rectangle,
        int radius)
    {
        using GraphicsPath path =
            CreateRoundedPath(
                rectangle,
                radius);

        graphics.DrawPath(
            pen,
            path);
    }

    private static GraphicsPath CreateRoundedPath(
        Rectangle rectangle,
        int radius)
    {
        int diameter = radius * 2;

        GraphicsPath path =
            new GraphicsPath();

        path.AddArc(
            rectangle.X,
            rectangle.Y,
            diameter,
            diameter,
            180,
            90);

        path.AddArc(
            rectangle.Right - diameter,
            rectangle.Y,
            diameter,
            diameter,
            270,
            90);

        path.AddArc(
            rectangle.Right - diameter,
            rectangle.Bottom - diameter,
            diameter,
            diameter,
            0,
            90);

        path.AddArc(
            rectangle.X,
            rectangle.Bottom - diameter,
            diameter,
            diameter,
            90,
            90);

        path.CloseFigure();

        return path;
    }
}