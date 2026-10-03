using System.Numerics;

namespace Lab13_BlindSignature.Crypto;

public sealed record RsaPublicKey(BigInteger N, BigInteger E);

public sealed record RsaPrivateKey(BigInteger N, BigInteger D);

public sealed record RsaKeyPair(
    BigInteger P,
    BigInteger Q,
    BigInteger Phi,
    RsaPublicKey PublicKey,
    RsaPrivateKey PrivateKey);
