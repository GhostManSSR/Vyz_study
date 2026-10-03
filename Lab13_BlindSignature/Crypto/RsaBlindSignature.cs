using System.Numerics;
using System.Security.Cryptography;

namespace Lab13_BlindSignature.Crypto;

public static class RsaBlindSignature
{
    public static RsaKeyPair GenerateKeyPair(int primeBits = 1024)
    {
        const int e = 65537;

        while (true)
        {
            var p = GeneratePrime(primeBits);
            var q = GeneratePrime(primeBits);
            if (p == q)
                continue;

            var n = p * q;
            // Для учебной реализации стараемся получить полный 2048-битный модуль.
            if (GetBitLength(n) != primeBits * 2)
                continue;

            var phi = (p - 1) * (q - 1);
            var eBig = new BigInteger(e);

            if (BigInteger.GreatestCommonDivisor(eBig, phi) != 1)
                continue;

            var d = ModInverse(eBig, phi);
            return new RsaKeyPair(
                p,
                q,
                phi,
                new RsaPublicKey(n, eBig),
                new RsaPrivateKey(n, d));
        }
    }

    public static BigInteger Blind(BigInteger hash, BigInteger r, RsaPublicKey key)
    {
        return (hash * BigInteger.ModPow(r, key.E, key.N)) % key.N;
    }

    public static BigInteger SignBlinded(BigInteger blindedHash, RsaPrivateKey key)
    {
        return BigInteger.ModPow(blindedHash, key.D, key.N);
    }

    public static BigInteger Unblind(BigInteger blindedSignature, BigInteger r, RsaPublicKey key)
    {
        var rInverse = ModInverse(r, key.N);
        return (blindedSignature * rInverse) % key.N;
    }

    public static bool Verify(BigInteger hash, BigInteger signature, RsaPublicKey key)
    {
        return BigInteger.ModPow(signature, key.E, key.N) == hash;
    }

    public static BigInteger GenerateCoprimeRandom(BigInteger n)
    {
        var bytes = new byte[n.ToByteArray(isUnsigned: true, isBigEndian: true).Length];

        while (true)
        {
            RandomNumberGenerator.Fill(bytes);
            var r = new BigInteger(bytes, isUnsigned: true, isBigEndian: true);

            if (r > 1 && r < n && BigInteger.GreatestCommonDivisor(r, n) == 1)
                return r;
        }
    }

    public static BigInteger ModInverse(BigInteger a, BigInteger m)
    {
        a %= m;
        if (a < 0)
            a += m;

        var oldR = a;
        var r = m;
        var oldS = BigInteger.One;
        var s = BigInteger.Zero;

        while (r != 0)
        {
            var quotient = oldR / r;
            (oldR, r) = (r, oldR - quotient * r);
            (oldS, s) = (s, oldS - quotient * s);
        }

        if (oldR != 1)
            throw new InvalidOperationException("Обратного элемента не существует.");

        var result = oldS % m;
        return result < 0 ? result + m : result;
    }

    private static BigInteger GeneratePrime(int bits)
    {
        if (bits < 16)
            throw new ArgumentOutOfRangeException(nameof(bits));

        var byteCount = (bits + 7) / 8;
        var bytes = new byte[byteCount];

        while (true)
        {
            RandomNumberGenerator.Fill(bytes);
            bytes[0] |= 0x80; // гарантируем нужную длину
            bytes[^1] |= 0x01; // нечётное число

            var candidate = new BigInteger(bytes, isUnsigned: true, isBigEndian: true);
            if (IsProbablePrime(candidate, 40))
                return candidate;
        }
    }

    private static int GetBitLength(BigInteger value)
    {
        if (value.IsZero)
            return 0;

        var bytes = BigInteger.Abs(value).ToByteArray(isUnsigned: true, isBigEndian: true);
        var bits = (bytes.Length - 1) * 8;
        var first = bytes[0];

        while (first > 0)
        {
            bits++;
            first >>= 1;
        }

        return bits;
    }

    private static bool IsProbablePrime(BigInteger n, int rounds)
    {
        if (n < 2)
            return false;

        int[] smallPrimes =
        {
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37
        };

        foreach (var p in smallPrimes)
        {
            if (n == p)
                return true;
            if (n % p == 0)
                return false;
        }

        var d = n - 1;
        var s = 0;
        while (d % 2 == 0)
        {
            d /= 2;
            s++;
        }

        var byteCount = n.ToByteArray(isUnsigned: true, isBigEndian: true).Length;
        var bytes = new byte[byteCount];

        for (var i = 0; i < rounds; i++)
        {
            BigInteger a;
            do
            {
                RandomNumberGenerator.Fill(bytes);
                a = new BigInteger(bytes, isUnsigned: true, isBigEndian: true);
            }
            while (a < 2 || a >= n - 2);

            var x = BigInteger.ModPow(a, d, n);
            if (x == 1 || x == n - 1)
                continue;

            var composite = true;
            for (var r = 1; r < s; r++)
            {
                x = BigInteger.ModPow(x, 2, n);
                if (x == n - 1)
                {
                    composite = false;
                    break;
                }
            }

            if (composite)
                return false;
        }

        return true;
    }
}
