using MentalPoker.Models;

namespace MentalPoker.UI;

public class PlayerSeatControl : Panel
{
    private readonly Label _nameLabel;
    private readonly FlowLayoutPanel _cardsPanel;

    public Player? Player { get; private set; }

    public bool IsActive { get; private set; }

    public PlayerSeatControl()
    {
        Width = 170;
        Height = 130;

        BackColor =
            Color.FromArgb(225, 20, 31, 27);

        BorderStyle =
            BorderStyle.FixedSingle;

        _nameLabel =
            new Label
            {
                Dock = DockStyle.Top,
                Height = 32,
                TextAlign = ContentAlignment.MiddleCenter,
                ForeColor = Color.White,
                Font = new Font(
                    "Segoe UI",
                    10,
                    FontStyle.Bold)
            };

        _cardsPanel =
            new FlowLayoutPanel
            {
                Dock = DockStyle.Fill,
                FlowDirection = FlowDirection.LeftToRight,
                WrapContents = false,
                Padding = new Padding(14, 4, 14, 4),
                BackColor = Color.Transparent
            };

        Controls.Add(_cardsPanel);
        Controls.Add(_nameLabel);
    }

    public void SetPlayer(Player player)
    {
        Player = player;

        _nameLabel.Text =
            $"👤 {player.Name}";

        RefreshCards();
    }

    public void SetActive(bool active)
    {
        IsActive = active;

        BackColor =
            active
                ? Color.FromArgb(235, 50, 67, 55)
                : Color.FromArgb(225, 20, 31, 27);

        Invalidate();
    }

    public void RefreshCards(
        bool hidden = true)
    {
        _cardsPanel.Controls.Clear();

        if (Player == null)
            return;

        if (Player.Cards.Count == 0)
        {
            for (int i = 0; i < 2; i++)
            {
                var card =
                    new PlayingCardControl
                    {
                        Width = 52,
                        Height = 70,
                        IsHidden = hidden
                    };

                _cardsPanel.Controls.Add(card);
            }

            return;
        }

        foreach (int cardId in Player.Cards)
        {
            var card =
                new PlayingCardControl
                {
                    Width = 52,
                    Height = 70,
                    CardId = cardId,
                    IsHidden = hidden
                };

            _cardsPanel.Controls.Add(card);
        }
    }

    public void RevealCards()
    {
        if (Player == null)
            return;

        _cardsPanel.Controls.Clear();

        foreach (int cardId in Player.Cards)
        {
            var card =
                new PlayingCardControl
                {
                    Width = 52,
                    Height = 70,
                    CardId = cardId,
                    IsHidden = false
                };

            _cardsPanel.Controls.Add(card);
        }
    }
}