using System.Numerics;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using Lab13_BlindSignature.Models;

namespace Lab13_BlindSignature.Crypto;

public static class BallotHash
{
    public static byte[] Sha3_256(Ballot ballot)
    {
        var json = JsonSerializer.Serialize(ballot);
        return SHA3_256.HashData(Encoding.UTF8.GetBytes(json));
    }

    public static BigInteger ToPositiveInteger(byte[] hash)
    {
        return new BigInteger(hash, isUnsigned: true, isBigEndian: true);
    }

    public static string ToHex(byte[] data) => Convert.ToHexString(data);
}
