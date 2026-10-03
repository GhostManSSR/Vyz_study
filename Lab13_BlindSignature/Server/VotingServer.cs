using System.Numerics;
using Lab13_BlindSignature.Crypto;
using Lab13_BlindSignature.Models;

namespace Lab13_BlindSignature.Server;

public sealed record IssuedToken(string UserId, DateTime IssuedAt);

public sealed record PublicBallotRecord(
    string BallotId,
    Ballot Ballot,
    BigInteger Signature,
    DateTime AcceptedAt);

public sealed class VotingServer
{
    private readonly HashSet<string> _issuedUsers = new();
    private readonly HashSet<string> _usedBallots = new();
    private readonly List<PublicBallotRecord> _publicDatabase = new();

    public RsaKeyPair Keys { get; }
    public IReadOnlyList<PublicBallotRecord> PublicDatabase => _publicDatabase;

    public VotingServer(int primeBits = 1024)
    {
        Keys = RsaBlindSignature.GenerateKeyPair(primeBits);
    }

    public bool TryIssueBlindSignature(string userId, BigInteger blindedHash, out BigInteger blindedSignature, out string message)
    {
        blindedSignature = BigInteger.Zero;

        if (_issuedUsers.Contains(userId))
        {
            message = "Пользователь уже получил бюллетень. Повторная выдача запрещена.";
            return false;
        }

        if (blindedHash <= 0 || blindedHash >= Keys.PublicKey.N)
        {
            message = "Некорректное ослеплённое значение.";
            return false;
        }

        _issuedUsers.Add(userId);
        blindedSignature = RsaBlindSignature.SignBlinded(blindedHash, Keys.PrivateKey);
        message = "Слепая подпись выдана. Сервер не видел исходный бюллетень и его SHA3-хэш.";
        return true;
    }

    public bool AcceptAnonymousBallot(
        Ballot ballot,
        BigInteger signature,
        out string message)
    {
        var ballotId = ComputeBallotId(ballot);

        if (_usedBallots.Contains(ballotId))
        {
            message = "Бюллетень уже использован. Повторное голосование запрещено.";
            return false;
        }

        var hashBytes = BallotHash.Sha3_256(ballot);
        var hash = BallotHash.ToPositiveInteger(hashBytes);

        if (hash >= Keys.PublicKey.N)
        {
            message = "Хэш бюллетеня не помещается в модуль RSA.";
            return false;
        }

        var valid = RsaBlindSignature.Verify(hash, signature, Keys.PublicKey);
        if (!valid)
        {
            message = "Подпись недействительна. Бюллетень отклонён.";
            return false;
        }

        _usedBallots.Add(ballotId);
        _publicDatabase.Add(new PublicBallotRecord(
            ballotId,
            ballot,
            signature,
            DateTime.Now));

        message = "Подпись корректна. Голос принят и добавлен в открытую базу.";
        return true;
    }

    public static string ComputeBallotId(Ballot ballot)
    {
        return BallotHash.ToHex(BallotHash.Sha3_256(ballot));
    }
}
