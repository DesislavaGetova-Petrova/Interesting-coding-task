import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Coding {

    public static void main(String[] args) {
        String url = "https://raw.githubusercontent.com/nikiiv/JavaCodingTestOne/master/scrabble-words.txt";
        try {
            Map<Integer, Set<String>> wordCollections = readWordsFromFile(url);
            Set<String> previousWords = Set.of("I", "A");
            for (int i = 2; i <= 9; i++) {
                Set<String> currentWords = findWordsWithSubstrings(wordCollections.getOrDefault(i, new HashSet<>()), previousWords);
                wordCollections.put(i, currentWords);
                previousWords = currentWords;
            }
            System.out.printf("Number of words:%d%n", wordCollections.get(9).size());
            for (String word : wordCollections.get(9)) {
                System.out.println(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<Integer, Set<String>> readWordsFromFile(String url) throws IOException {
        Map<Integer, Set<String>> wordCollections = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                int length = word.length();
                Set<String> collection = wordCollections.computeIfAbsent(length, k -> new HashSet<>());
                collection.add(word);
            }
        }
        return wordCollections;
    }

    private static Set<String> findWordsWithSubstrings(Set<String> targetWords, Set<String> sourceWords) {
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        return pool.invoke(new SubstringTask(targetWords.toArray(new String[0]), sourceWords));
    }

    static class SubstringTask extends RecursiveTask<Set<String>> {
        private final String[] targetWords;
        private final Set<String> sourceWords;
        private final Map<Character, Integer> sourceCharFreq;

        SubstringTask(String[] targetWords, Set<String> sourceWords) {
            this.targetWords = targetWords;
            this.sourceWords = sourceWords;
            this.sourceCharFreq = computeCharFrequencies(sourceWords);
        }

        @Override
        protected Set<String> compute() {
            Set<String> result = new HashSet<>();
            for (String targetWord : targetWords) {
                if (containsAllChars(targetWord)) {
                    result.add(targetWord);
                }
            }
            return result;
        }

        private boolean containsAllChars(String targetWord) {
            Map<Character, Integer> targetCharFreq = new HashMap<>();
            for (char c : targetWord.toCharArray()) {
                targetCharFreq.put(c, targetCharFreq.getOrDefault(c, 0) + 1);
            }
            for (Map.Entry<Character, Integer> entry : sourceCharFreq.entrySet()) {
                char c = entry.getKey();
                int sourceFreq = entry.getValue();
                int targetFreq = targetCharFreq.getOrDefault(c, 0);
                if (targetFreq < sourceFreq) {
                    return false;
                }
            }
            return true;
        }

        private Map<Character, Integer> computeCharFrequencies(Set<String> words) {
            Map<Character, Integer> charFreq = new HashMap<>();
            for (String word : words) {
                for (char c : word.toCharArray()) {
                    charFreq.put(c, charFreq.getOrDefault(c, 0) + 1);
                }
            }
            return charFreq;
        }
    }
}
