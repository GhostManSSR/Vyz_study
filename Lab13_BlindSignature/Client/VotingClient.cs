using System.Numerics;
using System.Security.Cryptography;
using Lab13_BlindSignature.Crypto;
using Lab13_BlindSignature.Models;
using Lab13_BlindSignature.Server;

namespace Lab13_BlindSignature.Client;

public sealed class VotingClient
{
    public string UserId { get; }

    public Ballot? Ballot { get; private set; }

    // Случайное число r
    public BigInteger RandomR { get; private set; }

    // r^(-1) mod n
    public BigInteger RandomRInverse { get; private set; }

    // SHA3-256 от бюллетеня в виде массива байт
    public byte[]? BallotHashBytes { get; private set; }

    // Хэш бюллетеня как положительное число
    public BigInteger BallotHash { get; private set; }

    // Ослабленный/ослеплённый хэш B'
    public BigInteger BlindedHash { get; private set; }

    // Подпись сервером ослеплённого значения S'
    public BigInteger BlindedSignature { get; private set; }

    // Настоящая подпись бюллетеня S
    public BigInteger Signature { get; private set; }

    public VotingClient(string userId)
    {
        UserId = userId;
    }

    /// <summary>
    /// Создание бюллетеня и вычисление его SHA3-256 хэша.
    /// </summary>
    public void CreateBallot(VoteChoice choice)
    {
        // Случайная служебная информация бюллетеня.
        // Она делает каждый бюллетень уникальным.
        var nonceBytes = RandomNumberGenerator.GetBytes(32);
        var nonce = Convert.ToHexString(nonceBytes);

        Ballot = new Ballot(
            ElectionId: "LAB13-2026",
            Question: "Одобряете ли вы проведение данного голосования?",
            Choice: choice,
            ServerAddress: "local://voting-server",
            RandomNonce: nonce
        );

        // ВАЖНО:
        // Здесь явно указываем класс BallotHash,
        // чтобы он не конфликтовал со свойством BallotHash.
        BallotHashBytes =
            Lab13_BlindSignature.Crypto.BallotHash.Sha3_256(Ballot);

        // Преобразуем SHA3-256 в положительное BigInteger.
        BallotHash =
            Lab13_BlindSignature.Crypto.BallotHash.ToPositiveInteger(
                BallotHashBytes
            );
    }

    /// <summary>
    /// Подготовка бюллетеня к слепому подписыванию.
    /// </summary>
    public void PrepareBlindSignature(RsaPublicKey publicKey)
    {
        if (Ballot is null || BallotHashBytes is null)
            throw new InvalidOperationException(
                "Сначала необходимо создать бюллетень."
            );

        // Генерируем случайное r, взаимно простое с n.
        RandomR =
            RsaBlindSignature.GenerateCoprimeRandom(publicKey.N);

        // Вычисляем r^(-1) mod n.
        RandomRInverse =
            RsaBlindSignature.ModInverse(
                RandomR,
                publicKey.N
            );

        // B' = H * r^e mod n
        BlindedHash =
            RsaBlindSignature.Blind(
                BallotHash,
                RandomR,
                publicKey
            );
    }

    /// <summary>
    /// Получение слепой подписи от сервера
    /// и снятие ослепления.
    /// </summary>
    public bool ReceiveAndUnblind(
        RsaPublicKey publicKey,
        BigInteger blindedSignature)
    {
        // S' — подпись сервера для ослеплённого хэша.
        BlindedSignature = blindedSignature;

        // S = S' * r^(-1) mod n
        Signature =
            RsaBlindSignature.Unblind(
                blindedSignature,
                RandomR,
                publicKey
            );

        // Проверяем полученную подпись ещё на стороне клиента.
        return RsaBlindSignature.Verify(
            BallotHash,
            Signature,
            publicKey
        );
    }

    /// <summary>
    /// Передача готового подписанного бюллетеня
    /// через условно анонимный канал.
    /// </summary>
    public bool SendAnonymously(
        VotingServer server,
        out string message)
    {
        if (Ballot is null)
            throw new InvalidOperationException(
                "Бюллетень не создан."
            );

        return server.AcceptAnonymousBallot(
            Ballot,
            Signature,
            out message
        );
    }
}