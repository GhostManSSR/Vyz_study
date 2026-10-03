namespace Lab13_BlindSignature.Models;

public enum VoteChoice
{
    Yes = 1,
    No = 2,
    Abstained = 3
}

public sealed record Ballot(
    string ElectionId,
    string Question,
    VoteChoice Choice,
    string ServerAddress,
    string RandomNonce);
