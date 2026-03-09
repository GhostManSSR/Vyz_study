package mta.spam;

import java.util.List;

public class SpamDetector {

    private static final List<String> spamWords =
            List.of("viagra", "casino", "bitcoin", "free money");

    public static boolean isSpam(String text) {

        String lower = text.toLowerCase();

        for (String word : spamWords) {

            if (lower.contains(word)) {
                return true;
            }
        }

        return false;
    }
}
