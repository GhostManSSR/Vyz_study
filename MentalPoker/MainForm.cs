using System.Numerics;
using MentalPoker.Crypto;
using MentalPoker.Models;
using MentalPoker.Poker;
using MentalPoker.UI;

namespace MentalPoker;

public partial class MainForm : Form
{
    private MentalPokerProtocol? _protocol;

    private PokerTablePanel _table = null!;

    private Panel _topPanel = null!;

    private Panel _bottomPanel = null!;

    private Button _newGameButton = null!;

    private Button _dealButton = null!;

    private Button _revealButton = null!;

    private Button _nextRoundButton = null!;

    private Button _potButton = null!;

    private Button _showdownButton = null!;

    private Button _verifyButton = null!;

    private Button _protocolButton = null!;

    private Button _keysButton = null!;

    private NumericUpDown _playersInput = null!;

    private NumericUpDown _deckInput = null!;

    private NumericUpDown _cardsInput = null!;

    private NumericUpDown _communityInput = null!;

    private Label _statusLabel = null!;

    private Label _roundLabel = null!;

    private Label _potLabel = null!;

    private int _roundNumber;

    private int _pot;

    private bool _cardsDealt;

    private bool _cardsRevealed;

    private bool _showdownCompleted;

    private PokerGameResult? _lastGameResult;


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public MainForm()
    {
        InitializeComponent();

        BuildInterface();

        CreateNewGame();
    }


    // ============================================================
    // ИНТЕРФЕЙС
    // ============================================================

    private void BuildInterface()
    {
        BackColor =
            Color.FromArgb(
                12,
                18,
                16);

        ForeColor =
            Color.White;

        StartPosition =
            FormStartPosition.CenterScreen;

        WindowState =
            FormWindowState.Maximized;

        MinimumSize =
            new Size(
                1200,
                750);

        Text =
            "♠ Ментальный покер";


        // ========================================================
        // TOP
        // ========================================================

        _topPanel =
            new Panel
            {
                Dock = DockStyle.Top,
                Height = 90,
                BackColor =
                    Color.FromArgb(
                        20,
                        29,
                        25)
            };

        Controls.Add(_topPanel);


        Label title =
            new Label
            {
                Text =
                    "♠ МЕНТАЛЬНЫЙ ПОКЕР",
                Font =
                    new Font(
                        "Segoe UI",
                        19,
                        FontStyle.Bold),
                ForeColor =
                    Color.White,
                AutoSize = true,
                Location =
                    new Point(
                        25,
                        12)
            };

        _topPanel.Controls.Add(title);


        Label subtitle =
            new Label
            {
                Text =
                    "Криптографическая карточная игра • Texas Hold'em",
                Font =
                    new Font(
                        "Segoe UI",
                        9),
                ForeColor =
                    Color.FromArgb(
                        170,
                        185,
                        177),
                AutoSize = true,
                Location =
                    new Point(
                        27,
                        44)
            };

        _topPanel.Controls.Add(subtitle);


        // ========================================================
        // НАСТРОЙКИ
        // ========================================================

        AddTopLabel(
            _topPanel,
            "Игроки",
            330);

        _playersInput =
            CreateNumberInput(
                330,
                35,
                2,
                10,
                4);

        _topPanel.Controls.Add(
            _playersInput);


        AddTopLabel(
            _topPanel,
            "Колода",
            415);

        _deckInput =
            CreateNumberInput(
                415,
                35,
                52,
                200,
                52);

        _topPanel.Controls.Add(
            _deckInput);


        AddTopLabel(
            _topPanel,
            "Карт игроку",
            500);

        _cardsInput =
            CreateNumberInput(
                500,
                35,
                2,
                2,
                2);

        _topPanel.Controls.Add(
            _cardsInput);


        AddTopLabel(
            _topPanel,
            "На столе",
            610);

        _communityInput =
            CreateNumberInput(
                610,
                35,
                5,
                5,
                5);

        _topPanel.Controls.Add(
            _communityInput);


        // ========================================================
        // ROUND
        // ========================================================

        _roundLabel =
            new Label
            {
                Text =
                    "РАУНД №1",
                AutoSize = true,
                Font =
                    new Font(
                        "Segoe UI",
                        10,
                        FontStyle.Bold),
                ForeColor =
                    Color.FromArgb(
                        230,
                        199,
                        106),
                Location =
                    new Point(
                        720,
                        15)
            };

        _topPanel.Controls.Add(
            _roundLabel);


        // ========================================================
        // POT
        // ========================================================

        _potLabel =
            new Label
            {
                Text =
                    "🟡 БАНК: 0",
                AutoSize = true,
                Font =
                    new Font(
                        "Segoe UI",
                        11,
                        FontStyle.Bold),
                ForeColor =
                    Color.FromArgb(
                        255,
                        214,
                        90),
                Location =
                    new Point(
                        720,
                        43)
            };

        _topPanel.Controls.Add(
            _potLabel);


        // ========================================================
        // STATUS
        // ========================================================

        _statusLabel =
            new Label
            {
                Text =
                    "● ГОТОВ",
                AutoSize = true,
                Font =
                    new Font(
                        "Segoe UI",
                        10,
                        FontStyle.Bold),
                ForeColor =
                    Color.FromArgb(
                        230,
                        199,
                        106),
                Location =
                    new Point(
                        870,
                        30)
            };

        _topPanel.Controls.Add(
            _statusLabel);


        // ========================================================
        // TABLE
        // ========================================================

        _table =
            new PokerTablePanel
            {
                Dock = DockStyle.Fill
            };

        Controls.Add(_table);


        // ========================================================
        // BOTTOM
        // ========================================================

        _bottomPanel =
            new Panel
            {
                Dock = DockStyle.Bottom,
                Height = 82,
                BackColor =
                    Color.FromArgb(
                        20,
                        29,
                        25)
            };

        Controls.Add(_bottomPanel);


        CreateButtons();
    }


    // ============================================================
    // BUTTONS
    // ============================================================

    private void CreateButtons()
    {
        _newGameButton =
            CreateButton(
                "↻ Новая партия",
                15,
                135);

        _newGameButton.Click +=
            (_, _) => CreateNewGame();

        _bottomPanel.Controls.Add(
            _newGameButton);


        _dealButton =
            CreateButton(
                "▶ Раздать",
                160,
                120);

        _dealButton.BackColor =
            Color.FromArgb(
                11,
                107,
                67);

        _dealButton.Click +=
            (_, _) => StartGame();

        _bottomPanel.Controls.Add(
            _dealButton);


        _revealButton =
            CreateButton(
                "♣ Открыть",
                290,
                120);

        _revealButton.Click +=
            (_, _) => RevealCards();

        _bottomPanel.Controls.Add(
            _revealButton);


        _showdownButton =
            CreateButton(
                "🏆 Showdown",
                420,
                130);

        _showdownButton.BackColor =
            Color.FromArgb(
                105,
                78,
                20);

        _showdownButton.Click +=
            (_, _) => Showdown();

        _bottomPanel.Controls.Add(
            _showdownButton);


        _nextRoundButton =
            CreateButton(
                "↻ Следующий",
                560,
                125);

        _nextRoundButton.Click +=
            (_, _) => StartNextRound();

        _bottomPanel.Controls.Add(
            _nextRoundButton);


        _potButton =
            CreateButton(
                "🟡 +500",
                695,
                100);

        _potButton.Click +=
            (_, _) => AddToPot();

        _bottomPanel.Controls.Add(
            _potButton);


        _verifyButton =
            CreateButton(
                "✓ Проверить",
                805,
                120);

        _verifyButton.Click +=
            (_, _) => VerifyProtocol();

        _bottomPanel.Controls.Add(
            _verifyButton);


        _protocolButton =
            CreateButton(
                "☰ Протокол",
                935,
                120);

        _protocolButton.Click +=
            (_, _) => ShowProtocol();

        _bottomPanel.Controls.Add(
            _protocolButton);


        _keysButton =
            CreateButton(
                "🔑 Ключи",
                1065,
                110);

        _keysButton.Click +=
            (_, _) => ShowKeys();

        _bottomPanel.Controls.Add(
            _keysButton);
    }


    private Button CreateButton(
        string text,
        int left,
        int width)
    {
        Button button =
            new Button
            {
                Text = text,
                Location =
                    new Point(
                        left,
                        20),
                Width = width,
                Height = 42,
                FlatStyle =
                    FlatStyle.Flat,
                BackColor =
                    Color.FromArgb(
                        31,
                        45,
                        39),
                ForeColor =
                    Color.White,
                Font =
                    new Font(
                        "Segoe UI",
                        9,
                        FontStyle.Bold),
                Cursor =
                    Cursors.Hand
            };

        button.FlatAppearance.BorderColor =
            Color.FromArgb(
                65,
                88,
                77);

        button.FlatAppearance.BorderSize =
            1;

        return button;
    }


    // ============================================================
    // NUMERIC INPUT
    // ============================================================

    private NumericUpDown CreateNumberInput(
        int left,
        int top,
        int min,
        int max,
        int value)
    {
        return new NumericUpDown
        {
            Location =
                new Point(
                    left,
                    top),
            Width = 65,
            Minimum = min,
            Maximum = max,
            Value = value,
            BackColor =
                Color.FromArgb(
                    35,
                    45,
                    40),
            ForeColor =
                Color.White,
            BorderStyle =
                BorderStyle.FixedSingle
        };
    }


    // ============================================================
    // LABEL
    // ============================================================

    private void AddTopLabel(
        Control parent,
        string text,
        int left)
    {
        parent.Controls.Add(
            new Label
            {
                Text = text,
                AutoSize = true,
                ForeColor =
                    Color.FromArgb(
                        170,
                        185,
                        177),
                Location =
                    new Point(
                        left,
                        13),
                Font =
                    new Font(
                        "Segoe UI",
                        8)
            });
    }


    // ============================================================
    // НОВАЯ ПАРТИЯ
    // ============================================================

    private void CreateNewGame()
    {
        try
        {
            int players =
                (int)_playersInput.Value;

            int deck =
                (int)_deckInput.Value;

            int cards =
                (int)_cardsInput.Value;

            int community =
                (int)_communityInput.Value;


            int required =
                players * cards +
                community;


            if (required > deck)
            {
                MessageBox.Show(
                    $"Недостаточно карт.\n\n" +
                    $"Нужно: {required}\n" +
                    $"В колоде: {deck}",
                    "Ошибка",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);

                return;
            }


            // Для Texas Hold'em нужна стандартная колода.
            if (deck != 52 ||
                cards != 2 ||
                community != 5)
            {
                MessageBox.Show(
                    "Для определения покерного победителя " +
                    "используется Texas Hold'em:\n\n" +
                    "• колода — 52 карты\n" +
                    "• 2 карты игроку\n" +
                    "• 5 общих карт",
                    "Настройка покера",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Information);

                _deckInput.Value = 52;
                _cardsInput.Value = 2;
                _communityInput.Value = 5;

                deck = 52;
                cards = 2;
                community = 5;
            }


            _roundNumber = 1;

            _pot = 0;

            _cardsDealt = false;

            _cardsRevealed = false;

            _showdownCompleted = false;

            _lastGameResult = null;


            _protocol =
                new MentalPokerProtocol();


            _protocol.Initialize(
                players,
                deck,
                cards,
                community);


            // ----------------------------------------------------
            // Начальный баланс каждого игрока.
            // ----------------------------------------------------

            foreach (Player player
                     in _protocol.Players)
            {
                player.Chips = 5000;

                player.CurrentBet = 0;
            }


            _table.SetPlayers(
                _protocol.Players);

            _table.HideAllCards();

            _table.ShowCommunityCards(
                new List<BigInteger>());

            _table.SetStage(
                "ПОДГОТОВКА КОЛОДЫ");

            _table.SetPot(0);


            UpdateHeader();


            _statusLabel.Text =
                "● ГОТОВ К ИГРЕ";

            _statusLabel.ForeColor =
                Color.FromArgb(
                    230,
                    199,
                    106);


            UpdateButtons();
        }
        catch (Exception ex)
        {
            MessageBox.Show(
                ex.ToString(),
                "Ошибка создания игры",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
        }
    }


    // ============================================================
    // РАЗДАЧА
    // ============================================================

    private async void StartGame()
    {
        if (_protocol == null)
            return;


        try
        {
            UpdateButtons(false);


            _statusLabel.Text =
                "● ШИФРОВАНИЕ КОЛОДЫ";


            _table.SetStage(
                "ШИФРОВАНИЕ КОЛОДЫ");


            await Task.Delay(400);


            _protocol.RunGame();


            _cardsDealt = true;

            _cardsRevealed = false;

            _showdownCompleted = false;

            _lastGameResult = null;


            _table.HideAllCards();

            _table.SetPlayers(
                _protocol.Players);

            _table.ShowCommunityCards(
                new List<BigInteger>());


            for (int i = 0;
                 i < _protocol.Players.Count;
                 i++)
            {
                _table.ActivatePlayer(i);

                await Task.Delay(250);
            }


            _table.ActivatePlayer(-1);


            _statusLabel.Text =
                "● КАРТЫ РАЗДАНЫ";


            _table.SetStage(
                "КАРТЫ РАЗДАНЫ");


            UpdateButtons();
        }
        catch (Exception ex)
        {
            MessageBox.Show(
                ex.ToString(),
                "Ошибка раздачи",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);

            _cardsDealt = false;

            UpdateButtons();
        }
    }


    // ============================================================
    // ВСКРЫТИЕ
    // ============================================================

    private async void RevealCards()
    {
        if (_protocol == null ||
            !_cardsDealt)
        {
            return;
        }


        try
        {
            UpdateButtons(false);


            _statusLabel.Text =
                "● РАСШИФРОВАНИЕ";


            _table.SetStage(
                "РАСШИФРОВАНИЕ КАРТ");


            for (int i = 0;
                 i < _protocol.Players.Count;
                 i++)
            {
                _table.ActivatePlayer(i);

                await Task.Delay(300);
            }


            _table.ActivatePlayer(-1);


            _table.RevealAllCards();

            await Task.Delay(500);


            _table.ShowCommunityCards(
                _protocol.CommunityCards);

            await Task.Delay(300);


            _cardsRevealed = true;


            _statusLabel.Text =
                "● КАРТЫ ОТКРЫТЫ";


            _statusLabel.ForeColor =
                Color.FromArgb(
                    100,
                    220,
                    145);


            _table.SetStage(
                "КАРТЫ ОТКРЫТЫ — ГОТОВ SHOWDOWN");


            UpdateButtons();
        }
        catch (Exception ex)
        {
            MessageBox.Show(
                ex.ToString(),
                "Ошибка вскрытия",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);

            UpdateButtons();
        }
    }


    // ============================================================
    // SHOWDOWN
    // ============================================================

    private void Showdown()
    {
        if (_protocol == null ||
            !_cardsRevealed)
        {
            MessageBox.Show(
                "Сначала необходимо открыть карты.",
                "Showdown",
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);

            return;
        }


        if (_showdownCompleted &&
            _lastGameResult != null)
        {
            ShowShowdownResult(
                _lastGameResult);

            return;
        }


        try
        {
            List<int> communityCards =
                _protocol.CommunityCards
                    .Select(x => (int)x)
                    .ToList();


            // ----------------------------------------------------
            // Определяем победителей.
            // ----------------------------------------------------

            PokerGameResult result =
                PokerHandEvaluator.DetermineWinners(
                    _protocol.Players,
                    communityCards,
                    _pot);


            _lastGameResult = result;


            if (result.Winners.Count == 0)
            {
                MessageBox.Show(
                    "Не удалось определить победителя.",
                    "Showdown",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);

                return;
            }


            // ----------------------------------------------------
            // Распределяем банк.
            // ----------------------------------------------------

            int prize =
                result.PrizePerWinner;


            int remainder =
                result.RemainingPrize;


            foreach (Player winner
                     in result.Winners)
            {
                winner.Chips += prize;
            }


            // ----------------------------------------------------
            // Если банк не делится поровну,
            // остаток получает первый победитель.
            // ----------------------------------------------------

            if (remainder > 0)
            {
                result.Winners[0].Chips +=
                    remainder;
            }


            _pot = 0;


            foreach (Player player
                     in _protocol.Players)
            {
                player.CurrentBet = 0;
            }


            _table.SetPot(0);

            _table.SetPlayers(
                _protocol.Players);


            _showdownCompleted = true;


            _statusLabel.Text =
                result.Winners.Count == 1
                    ? $"🏆 ПОБЕДИТЕЛЬ: {result.Winners[0].Name}"
                    : "🏆 НИЧЬЯ";


            _statusLabel.ForeColor =
                Color.FromArgb(
                    255,
                    214,
                    90);


            _table.SetStage(
                "🏆 SHOWDOWN");


            UpdateHeader();

            UpdateButtons();


            ShowShowdownResult(
                result);
        }
        catch (Exception ex)
        {
            MessageBox.Show(
                ex.ToString(),
                "Ошибка Showdown",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
        }
    }


    // ============================================================
    // ОКНО SHOWDOWN
    // ============================================================

    private void ShowShowdownResult(
        PokerGameResult result)
    {
        using Form form =
            new Form
            {
                Text =
                    "🏆 SHOWDOWN — Результат партии",
                StartPosition =
                    FormStartPosition.CenterParent,
                Width = 850,
                Height = 700,
                BackColor =
                    Color.FromArgb(
                        14,
                        20,
                        18),
                ForeColor =
                    Color.White
            };


        Panel header =
            new Panel
            {
                Dock = DockStyle.Top,
                Height = 125,
                BackColor =
                    Color.FromArgb(
                        23,
                        34,
                        29)
            };


        form.Controls.Add(header);


        string winnerText;

        if (result.Winners.Count == 1)
        {
            winnerText =
                $"🏆 {result.Winners[0].Name}";
        }
        else
        {
            winnerText =
                "🤝 НИЧЬЯ — " +
                string.Join(
                    ", ",
                    result.Winners.Select(
                        x => x.Name));
        }


        Label title =
            new Label
            {
                Text =
                    "SHOWDOWN",
                AutoSize = true,
                Font =
                    new Font(
                        "Segoe UI",
                        20,
                        FontStyle.Bold),
                ForeColor =
                    Color.FromArgb(
                        255,
                        214,
                        90),
                Location =
                    new Point(
                        25,
                        15)
            };

        header.Controls.Add(title);


        Label winner =
            new Label
            {
                Text =
                    winnerText,
                AutoSize = true,
                Font =
                    new Font(
                        "Segoe UI",
                        15,
                        FontStyle.Bold),
                ForeColor =
                    Color.White,
                Location =
                    new Point(
                        27,
                        55)
            };

        header.Controls.Add(winner);


        Label prize =
            new Label
            {
                Text =
                    $"Банк партии: {result.Pot:N0}   " +
                    $"Выплата: {result.PrizePerWinner:N0}",
                AutoSize = true,
                Font =
                    new Font(
                        "Segoe UI",
                        10,
                        FontStyle.Bold),
                ForeColor =
                    Color.FromArgb(
                        170,
                        185,
                        177),
                Location =
                    new Point(
                        28,
                        90)
            };

        header.Controls.Add(prize);


        TextBox text =
            new TextBox
            {
                Multiline = true,
                ReadOnly = true,
                ScrollBars =
                    ScrollBars.Vertical,
                Dock = DockStyle.Fill,
                BackColor =
                    Color.FromArgb(
                        11,
                        17,
                        15),
                ForeColor =
                    Color.FromArgb(
                        220,
                        230,
                        225),
                Font =
                    new Font(
                        "Consolas",
                        11),
                BorderStyle =
                    BorderStyle.None
            };

        form.Controls.Add(text);


        text.AppendText(
            "════════════════════════════════════════════"
            + Environment.NewLine);

        text.AppendText(
            "РЕЗУЛЬТАТЫ ВСЕХ ИГРОКОВ"
            + Environment.NewLine);

        text.AppendText(
            "════════════════════════════════════════════"
            + Environment.NewLine
            + Environment.NewLine);


        foreach (PokerHandResult hand
                 in result.Results
                     .OrderByDescending(
                         x => x.ComparisonValues,
                         Comparer<int[]>.Create(
                             CompareArrays)))
        {
            bool isWinner =
                result.Winners.Contains(
                    hand.Player);


            string marker =
                isWinner
                    ? "🏆"
                    : "  ";


            text.AppendText(
                $"{marker} {hand.Player.Name}"
                + Environment.NewLine);


            text.AppendText(
                $"   Комбинация: {hand.Description}"
                + Environment.NewLine);


            text.AppendText(
                $"   Категория: {hand.Category}"
                + Environment.NewLine);


            text.AppendText(
                $"   Баланс: {hand.Player.Chips:N0}"
                + Environment.NewLine);


            text.AppendText(
                Environment.NewLine);
        }


        text.AppendText(
            "════════════════════════════════════════════"
            + Environment.NewLine);

        text.AppendText(
            "РАСПРЕДЕЛЕНИЕ БАНКА"
            + Environment.NewLine);

        text.AppendText(
            "════════════════════════════════════════════"
            + Environment.NewLine
            + Environment.NewLine);


        if (result.Winners.Count == 1)
        {
            text.AppendText(
                $"🏆 {result.Winners[0].Name}"
                + Environment.NewLine);

            text.AppendText(
                $"Получено: {result.PrizePerWinner:N0}"
                + Environment.NewLine);
        }
        else
        {
            foreach (Player currentWinner 
                     in result.Winners)
            {
                text.AppendText(
                    $"🏆 {currentWinner.Name}: " +
                    $"{result.PrizePerWinner:N0}"
                    + Environment.NewLine);
            }
        }


        if (result.RemainingPrize > 0)
        {
            text.AppendText(
                Environment.NewLine +
                $"Остаток банка: " +
                $"{result.RemainingPrize:N0}"
                + Environment.NewLine);
        }


        form.ShowDialog(this);
    }


    // ============================================================
    // СРАВНЕНИЕ МАССИВОВ
    // ============================================================

    private static int CompareArrays(
        int[]? a,
        int[]? b)
    {
        if (a == null && b == null)
            return 0;

        if (a == null)
            return -1;

        if (b == null)
            return 1;

        int length =
            Math.Min(
                a.Length,
                b.Length);

        for (int i = 0;
             i < length;
             i++)
        {
            int result =
                a[i].CompareTo(b[i]);

            if (result != 0)
                return result;
        }

        return a.Length.CompareTo(
            b.Length);
    }


    // ============================================================
    // БАНК
    // ============================================================

    private void AddToPot()
    {
        if (_protocol == null)
            return;


        const int amountPerPlayer = 500;


        if (_protocol.Players.Any(
                p => p.Chips < amountPerPlayer))
        {
            MessageBox.Show(
                "У одного или нескольких игроков " +
                "недостаточно фишек для ставки 500.",
                "Недостаточно средств",
                MessageBoxButtons.OK,
                MessageBoxIcon.Warning);

            return;
        }


        // --------------------------------------------------------
        // Каждый игрок вносит 500.
        // --------------------------------------------------------

        foreach (Player player
                 in _protocol.Players)
        {
            player.Chips -=
                amountPerPlayer;

            player.CurrentBet +=
                amountPerPlayer;

            _pot +=
                amountPerPlayer;
        }


        _table.SetPot(_pot);

        _table.SetPlayers(
            _protocol.Players);

        UpdateHeader();


        _statusLabel.Text =
            $"● ВСЕ ИГРОКИ: -{amountPerPlayer}";

        _statusLabel.ForeColor =
            Color.FromArgb(
                255,
                214,
                90);
    }


    // ============================================================
    // СЛЕДУЮЩИЙ РАУНД
    // ============================================================

    private void StartNextRound()
    {
        if (!_showdownCompleted)
        {
            MessageBox.Show(
                "Сначала необходимо определить победителя " +
                "через Showdown.",
                "Раунд не завершён",
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);

            return;
        }


        if (_protocol == null)
            return;


        try
        {
            int players =
                (int)_playersInput.Value;


            int deck =
                (int)_deckInput.Value;


            int cards =
                (int)_cardsInput.Value;


            int community =
                (int)_communityInput.Value;


            int required =
                players * cards +
                community;


            if (required > deck)
            {
                MessageBox.Show(
                    "Недостаточно карт.",
                    "Ошибка",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);

                return;
            }


            // ----------------------------------------------------
            // Сохраняем балансы.
            // ----------------------------------------------------

            Dictionary<int, int> balances =
                _protocol.Players.ToDictionary(
                    p => p.Id,
                    p => p.Chips);


            _roundNumber++;


            _protocol =
                new MentalPokerProtocol();


            _protocol.Initialize(
                players,
                deck,
                cards,
                community);


            // ----------------------------------------------------
            // Переносим деньги в новый раунд.
            // ----------------------------------------------------

            foreach (Player player
                     in _protocol.Players)
            {
                if (balances.TryGetValue(
                        player.Id,
                        out int balance))
                {
                    player.Chips = balance;
                }
                else
                {
                    player.Chips = 5000;
                }

                player.CurrentBet = 0;
            }


            _pot = 0;

            _cardsDealt = false;

            _cardsRevealed = false;

            _showdownCompleted = false;

            _lastGameResult = null;


            _table.SetPlayers(
                _protocol.Players);

            _table.HideAllCards();

            _table.ShowCommunityCards(
                new List<BigInteger>());

            _table.SetPot(0);

            _table.SetStage(
                $"ПОДГОТОВКА РАУНДА №{_roundNumber}");


            UpdateHeader();


            _statusLabel.Text =
                $"● ГОТОВ К РАУНДУ №{_roundNumber}";

            _statusLabel.ForeColor =
                Color.FromArgb(
                    230,
                    199,
                    106);


            UpdateButtons();
        }
        catch (Exception ex)
        {
            MessageBox.Show(
                ex.ToString(),
                "Ошибка создания раунда",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
        }
    }


    // ============================================================
    // ПРОВЕРКА ПРОТОКОЛА
    // ============================================================

    private void VerifyProtocol()
    {
        if (_protocol == null)
            return;


        bool result =
            _protocol.VerifyGame();


        if (result)
        {
            _statusLabel.Text =
                "✓ ПРОТОКОЛ ПРОВЕРЕН";


            _statusLabel.ForeColor =
                Color.FromArgb(
                    100,
                    220,
                    145);


            _table.SetStage(
                "✓ ПРОТОКОЛ ПРОВЕРЕН");


            MessageBox.Show(
                "ПРОВЕРКА ПРОЙДЕНА\n\n" +

                "✓ Количество карт корректно\n" +

                "✓ Все карты уникальны\n" +

                "✓ Карты находятся в диапазоне\n" +

                "✓ Ключи игроков взаимно обратны\n" +

                "✓ Полная колода восстановлена\n" +

                "✓ Расшифрование выполнено\n\n" +

                $"Раунд: {_roundNumber}\n" +

                $"Банк: {_pot:N0}",
                "Протокол проверен",
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);
        }
        else
        {
            _statusLabel.Text =
                "✗ ОШИБКА ПРОТОКОЛА";

            _statusLabel.ForeColor =
                Color.Firebrick;

            _table.SetStage(
                "✗ ОШИБКА ПРОТОКОЛА");


            MessageBox.Show(
                "ПРОВЕРКА НЕ ПРОЙДЕНА.\n\n" +
                "Подробности находятся в журнале.",
                "Ошибка",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
        }


        UpdateButtons();
    }


    // ============================================================
    // ПРОТОКОЛ
    // ============================================================

    private void ShowProtocol()
    {
        if (_protocol == null)
            return;


        using Form form =
            new Form
            {
                Text =
                    "Протокол ментального покера",
                StartPosition =
                    FormStartPosition.CenterParent,
                Width = 950,
                Height = 720,
                BackColor =
                    Color.FromArgb(
                        14,
                        20,
                        18),
                ForeColor =
                    Color.White
            };


        Panel header =
            new Panel
            {
                Dock = DockStyle.Top,
                Height = 95,
                BackColor =
                    Color.FromArgb(
                        23,
                        34,
                        29)
            };


        form.Controls.Add(header);


        Label title =
            new Label
            {
                Text =
                    "♠ ПРОТОКОЛ МЕНТАЛЬНОГО ПОКЕРА",
                AutoSize = true,
                Font =
                    new Font(
                        "Segoe UI",
                        17,
                        FontStyle.Bold),
                ForeColor =
                    Color.White,
                Location =
                    new Point(
                        20,
                        12)
            };


        header.Controls.Add(title);


        Label parameters =
            new Label
            {
                Text =
                    $"РАУНД: {_roundNumber}     " +
                    $"БАНК: {_pot:N0}     " +
                    $"p = {_protocol.P}     " +
                    $"q = {_protocol.Q}",
                AutoSize = true,
                Font =
                    new Font(
                        "Consolas",
                        10,
                        FontStyle.Bold),
                ForeColor =
                    Color.FromArgb(
                        230,
                        199,
                        106),
                Location =
                    new Point(
                        22,
                        48)
            };


        header.Controls.Add(parameters);


        TextBox log =
            new TextBox
            {
                Multiline = true,
                ReadOnly = true,
                ScrollBars =
                    ScrollBars.Vertical,
                Dock = DockStyle.Fill,
                BackColor =
                    Color.FromArgb(
                        11,
                        17,
                        15),
                ForeColor =
                    Color.FromArgb(
                        210,
                        220,
                        215),
                Font =
                    new Font(
                        "Consolas",
                        10),
                BorderStyle =
                    BorderStyle.None
            };


        form.Controls.Add(log);


        foreach (ProtocolLogEntry entry
                 in _protocol.Log)
        {
            log.AppendText(
                entry +
                Environment.NewLine);
        }


        log.AppendText(
            Environment.NewLine +
            "════════════════════════════════════════════" +
            Environment.NewLine +
            "ИТОГОВАЯ ИНФОРМАЦИЯ" +
            Environment.NewLine +
            "════════════════════════════════════════════" +
            Environment.NewLine);


        log.AppendText(
            $"Раунд: {_roundNumber}" +
            Environment.NewLine);


        log.AppendText(
            $"Банк: {_pot:N0}" +
            Environment.NewLine);


        log.AppendText(
            $"Игроков: {_protocol.Players.Count}" +
            Environment.NewLine);


        log.AppendText(
            $"Карт в колоде: {_protocol.OriginalDeck.Count}" +
            Environment.NewLine);


        log.AppendText(
            $"Карт игроку: {_protocol.CardsPerPlayer}" +
            Environment.NewLine);


        log.AppendText(
            $"Общих карт: {_protocol.CommunityCardsCount}" +
            Environment.NewLine);


        if (_lastGameResult != null)
        {
            log.AppendText(
                Environment.NewLine +
                "SHOWDOWN:" +
                Environment.NewLine);


            foreach (PokerHandResult hand
                     in _lastGameResult.Results)
            {
                log.AppendText(
                    $"{hand.Player.Name}: " +
                    $"{hand.Description}" +
                    Environment.NewLine);
            }
        }


        form.ShowDialog(this);
    }


    // ============================================================
    // КЛЮЧИ
    // ============================================================

    private void ShowKeys()
    {
        if (_protocol == null)
            return;


        using Form form =
            new Form
            {
                Text =
                    "Открытые ключи протокола",
                StartPosition =
                    FormStartPosition.CenterParent,
                Width = 800,
                Height = 650,
                BackColor =
                    Color.FromArgb(
                        14,
                        20,
                        18),
                ForeColor =
                    Color.White
            };


        Panel header =
            new Panel
            {
                Dock = DockStyle.Top,
                Height = 105,
                BackColor =
                    Color.FromArgb(
                        23,
                        34,
                        29)
            };


        form.Controls.Add(header);


        Label title =
            new Label
            {
                Text =
                    "🔑 ОТКРЫТЫЕ КЛЮЧИ",
                AutoSize = true,
                Font =
                    new Font(
                        "Segoe UI",
                        18,
                        FontStyle.Bold),
                ForeColor =
                    Color.White,
                Location =
                    new Point(
                        20,
                        12)
            };


        header.Controls.Add(title);


        Label parameters =
            new Label
            {
                Text =
                    $"p = {_protocol.P}       " +
                    $"q = {_protocol.Q}       " +
                    $"p - 1 = {_protocol.P - 1}",
                AutoSize = true,
                Font =
                    new Font(
                        "Consolas",
                        11,
                        FontStyle.Bold),
                ForeColor =
                    Color.FromArgb(
                        230,
                        199,
                        106),
                Location =
                    new Point(
                        22,
                        53)
            };


        header.Controls.Add(parameters);


        TextBox text =
            new TextBox
            {
                Multiline = true,
                ReadOnly = true,
                ScrollBars =
                    ScrollBars.Vertical,
                Dock = DockStyle.Fill,
                BackColor =
                    Color.FromArgb(
                        11,
                        17,
                        15),
                ForeColor =
                    Color.FromArgb(
                        220,
                        230,
                        225),
                Font =
                    new Font(
                        "Consolas",
                        11),
                BorderStyle =
                    BorderStyle.None
            };


        form.Controls.Add(text);


        text.AppendText(
            "ПАРАМЕТРЫ ПРОТОКОЛА"
            + Environment.NewLine
            + Environment.NewLine);


        text.AppendText(
            $"p = {_protocol.P}"
            + Environment.NewLine);

        text.AppendText(
            $"q = {_protocol.Q}"
            + Environment.NewLine);

        text.AppendText(
            $"p - 1 = {_protocol.P - 1}"
            + Environment.NewLine);


        text.AppendText(
            Environment.NewLine +
            "════════════════════════════════════════" +
            Environment.NewLine +
            "КЛЮЧИ ИГРОКОВ" +
            Environment.NewLine +
            "════════════════════════════════════════" +
            Environment.NewLine +
            Environment.NewLine);


        foreach (Player player
                 in _protocol.Players)
        {
            BigInteger check =
                (
                    player.EncryptionKey *
                    player.DecryptionKey
                ) % (_protocol.P - 1);


            text.AppendText(
                player.Name +
                Environment.NewLine);


            text.AppendText(
                $"  k = {player.EncryptionKey}" +
                Environment.NewLine);


            text.AppendText(
                $"  d = {player.DecryptionKey}" +
                Environment.NewLine);


            text.AppendText(
                $"  k × d mod (p - 1) = {check}" +
                Environment.NewLine);


            text.AppendText(
                check == 1
                    ? "  ✓ КЛЮЧИ КОРРЕКТНЫ"
                    : "  ✗ ОШИБКА КЛЮЧЕЙ");

            text.AppendText(
                Environment.NewLine +
                Environment.NewLine);
        }


        form.ShowDialog(this);
    }


    // ============================================================
    // HEADER
    // ============================================================

    private void UpdateHeader()
    {
        _roundLabel.Text =
            $"РАУНД №{_roundNumber}";


        _potLabel.Text =
            $"🟡 БАНК: {_pot:N0}";
    }


    // ============================================================
    // BUTTON STATE
    // ============================================================

    private void UpdateButtons(
        bool? forceEnabled = null)
    {
        if (forceEnabled.HasValue)
        {
            bool enabled =
                forceEnabled.Value;


            _newGameButton.Enabled = enabled;

            _dealButton.Enabled = enabled;

            _revealButton.Enabled = enabled;

            _showdownButton.Enabled = enabled;

            _nextRoundButton.Enabled = enabled;

            _potButton.Enabled = enabled;

            _verifyButton.Enabled = enabled;

            _protocolButton.Enabled = enabled;

            _keysButton.Enabled = enabled;

            return;
        }


        _newGameButton.Enabled =
            true;


        _dealButton.Enabled =
            !_cardsDealt;


        _revealButton.Enabled =
            _cardsDealt &&
            !_cardsRevealed;


        _showdownButton.Enabled =
            _cardsRevealed;


        _nextRoundButton.Enabled =
            _showdownCompleted;


        _potButton.Enabled =
            !_cardsRevealed &&
            !_showdownCompleted;


        _verifyButton.Enabled =
            _cardsDealt;


        _protocolButton.Enabled =
            _protocol != null;


        _keysButton.Enabled =
            _protocol != null;
    }
}