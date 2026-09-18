namespace Lab1;

public record ElGamalResult(
    long P,
    long G,
    long OriginalMessage,
    long U,
    long V,
    long DecryptedMessage);