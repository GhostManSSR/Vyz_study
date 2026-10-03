using System.Numerics;
using MentalPoker.Crypto;
using MentalPoker.Models;

namespace MentalPoker.UI;

public class PokerTablePanel : Panel
{
    private readonly List<PlayerSeatControl> _seats = new();

    private readonly FlowLayoutPanel _communityCards;

    private readonly Label _stageLabel;

    private readonly Label _potLabel;

    private readonly Label _titleLabel;

    private List<Player> _players = new();

    public PokerTablePanel()
    {
        DoubleBuffered = true;

        BackColor =
            Color.FromArgb(11, 18, 15);

        _titleLabel =
            new Label
            {
                Text = "♠ МЕНТАЛЬНЫЙ ПОКЕР",
                ForeColor = Color.White,
                Font = new Font(
                    "Segoe UI",
                    18,
                    FontStyle.Bold),
                AutoSize = true,
                BackColor = Color.Transparent
            };

        Controls.Add(_titleLabel);

        _stageLabel =
            new Label
            {
                Text = "ПОДГОТОВКА",
                ForeColor = Color.FromArgb(230, 199, 106),
                Font = new Font(
                    "Segoe UI",
                    10,
                    FontStyle.Bold),
                AutoSize = true,
                BackColor = Color.Transparent
            };

        Controls.Add(_stageLabel);

        _potLabel =
            new Label
            {
                Text = "БАНК 0",
                ForeColor = Color.FromArgb(230, 199, 106),
                Font = new Font(
                    "Segoe UI",
                    11,
                    FontStyle.Bold),
                AutoSize = true,
                BackColor = Color.Transparent
            };

        Controls.Add(_potLabel);

        _communityCards =
            new FlowLayoutPanel
            {
                Width = 380,
                Height = 100,
                FlowDirection = FlowDirection.LeftToRight,
                WrapContents = false,
                BackColor = Color.Transparent
            };

        Controls.Add(_communityCards);

        Resize += (_, _) =>
        {
            UpdateLayout();
        };
    }

    public void SetPlayers(
        List<Player> players)
    {
        foreach (Control control in _seats)
        {
            Controls.Remove(control);
            control.Dispose();
        }

        _seats.Clear();

        _players = players;

        foreach (Player player in players)
        {
            var seat =
                new PlayerSeatControl();

            seat.SetPlayer(player);

            _seats.Add(seat);

            Controls.Add(seat);
        }

        BringElementsToFront();

        UpdateLayout();
    }

    public void ShowCommunityCards(
        List<BigInteger> cards)
    {
        _communityCards.Controls.Clear();

        foreach (BigInteger value in cards)
        {
            var card =
                new PlayingCardControl
                {
                    CardId = (int)value,
                    IsHidden = false,
                    Width = 58,
                    Height = 82
                };

            _communityCards.Controls.Add(card);
        }

        if (cards.Count == 0)
        {
            for (int i = 0; i < 5; i++)
            {
                var card =
                    new PlayingCardControl
                    {
                        Width = 58,
                        Height = 82,
                        IsHidden = true
                    };

                _communityCards.Controls.Add(card);
            }
        }
    }

    public void SetStage(string text)
    {
        _stageLabel.Text = text;
    }

    public void SetPot(int amount)
    {
        _potLabel.Text =
            $"БАНК {amount:N0}";
    }

    public void RevealAllCards()
    {
        foreach (PlayerSeatControl seat in _seats)
        {
            seat.RevealCards();
        }
    }

    public void HideAllCards()
    {
        foreach (PlayerSeatControl seat in _seats)
        {
            seat.RefreshCards(true);
        }
    }

    public void ActivatePlayer(int playerIndex)
    {
        for (int i = 0; i < _seats.Count; i++)
        {
            _seats[i].SetActive(
                i == playerIndex);
        }
    }

    private void UpdateLayout()
    {
        if (_seats.Count == 0)
            return;

        int centerX =
            ClientSize.Width / 2;

        int centerY =
            ClientSize.Height / 2 + 15;

        _titleLabel.Location =
            new Point(
                20,
                15);

        _stageLabel.Location =
            new Point(
                ClientSize.Width - 180,
                20);

        _potLabel.Location =
            new Point(
                centerX - 50,
                centerY + 115);

        _communityCards.Location =
            new Point(
                centerX - _communityCards.Width / 2,
                centerY - 65);

        int count = _seats.Count;

        /*
         * Раскладываем игроков по окружности.
         */

        double radiusX =
            Math.Max(
                220,
                ClientSize.Width * 0.36);

        double radiusY =
            Math.Max(
                145,
                ClientSize.Height * 0.36);

        for (int i = 0; i < count; i++)
        {
            double angle =
                -Math.PI / 2 +
                i * (2 * Math.PI / count);

            int x =
                centerX +
                (int)(Math.Cos(angle) * radiusX) -
                _seats[i].Width / 2;

            int y =
                centerY +
                (int)(Math.Sin(angle) * radiusY) -
                _seats[i].Height / 2;

            x =
                Math.Max(
                    5,
                    Math.Min(
                        ClientSize.Width -
                        _seats[i].Width -
                        5,
                        x));

            y =
                Math.Max(
                    50,
                    Math.Min(
                        ClientSize.Height -
                        _seats[i].Height -
                        5,
                        y));

            _seats[i].Location =
                new Point(x, y);
        }

        BringElementsToFront();
    }

    private void BringElementsToFront()
    {
        _titleLabel.BringToFront();
        _stageLabel.BringToFront();
        _potLabel.BringToFront();
        _communityCards.BringToFront();

        foreach (PlayerSeatControl seat in _seats)
        {
            seat.BringToFront();
        }
    }

    protected override void OnPaint(PaintEventArgs e)
    {
        base.OnPaint(e);

        Graphics g = e.Graphics;

        g.SmoothingMode =
            System.Drawing.Drawing2D.SmoothingMode.AntiAlias;

        Rectangle table =
            new Rectangle(
                70,
                65,
                Math.Max(
                    200,
                    Width - 140),
                Math.Max(
                    250,
                    Height - 120));

        using Brush shadow =
            new SolidBrush(
                Color.FromArgb(
                    100,
                    0,
                    0,
                    0));

        g.FillEllipse(
            shadow,
            new Rectangle(
                table.X + 8,
                table.Y + 10,
                table.Width,
                table.Height));

        using Brush border =
            new SolidBrush(
                Color.FromArgb(
                    76,
                    52,
                    30));

        g.FillEllipse(
            border,
            table);

        Rectangle felt =
            new Rectangle(
                table.X + 14,
                table.Y + 14,
                table.Width - 28,
                table.Height - 28);

        using Brush green =
            new SolidBrush(
                Color.FromArgb(
                    13,
                    91,
                    57));

        g.FillEllipse(
            green,
            felt);

        using Pen innerBorder =
            new Pen(
                Color.FromArgb(
                    100,
                    148,
                    105,
                    55),
                2);

        g.DrawEllipse(
            innerBorder,
            felt);

        /*
         * Декоративные точки вокруг стола.
         */

        using Brush dot =
            new SolidBrush(
                Color.FromArgb(
                    60,
                    255,
                    255,
                    255));

        for (int i = 0; i < 24; i++)
        {
            double angle =
                i * Math.PI * 2 / 24;

            int x =
                table.X +
                table.Width / 2 +
                (int)(
                    Math.Cos(angle) *
                    (table.Width / 2 - 25));

            int y =
                table.Y +
                table.Height / 2 +
                (int)(
                    Math.Sin(angle) *
                    (table.Height / 2 - 25));

            g.FillEllipse(
                dot,
                x - 2,
                y - 2,
                4,
                4);
        }
    }
}