package utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BadWordsDetector {
    private static final Set<String> badWords = new HashSet<>(Arrays.asList(
        "spam", "ttt", "ggg", "bbb", "fff"
    ));

    public static boolean containsBadWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();

        String[] words = lowerText.split("\\s+");

        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "");
            if (badWords.contains(word)) {
                return true;
            }
        }
        
        return false;
    }

    public static String censorBadWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String lowerText = text.toLowerCase();
        String result = text;

        for (String badWord : badWords) {
            String regex = "(?i)" + badWord;
            String stars = "*".repeat(badWord.length());
            result = result.replaceAll(regex, stars);
        }

        return result;
    }
} 