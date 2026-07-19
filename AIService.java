import java.sql.*;
import java.util.*;
import java.util.regex.*;

/*
 * AIService - Local "smart search" using TF-IDF + Cosine Similarity.
 * NO API calls, NO cost, NO internet needed - works 100% offline.
 *
 * This is a real information-retrieval algorithm (same family of techniques
 * used by search engines) implemented from scratch in Java.
 *
 * How it works:
 *   1. Each book (title + author + description) becomes a "document"
 *   2. TF-IDF (Term Frequency - Inverse Document Frequency) scores how
 *      important each word is - common words score low, rare/distinctive
 *      words score high
 *   3. The user's query is converted into the same kind of vector
 *   4. Cosine Similarity measures how close the query vector is to each
 *      book's vector
 *   5. Books are ranked by similarity score, best matches returned first
 *
 * Method signature is unchanged (smartSearch(Connection, String)) so no
 * other file needs to change - UserDashboard.java and AISearchResultsView.java
 * call this exactly the same way as before.
 */
public class AIService {

    // Common English words that don't help distinguish topics
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "was", "were", "i", "want", "to", "for",
            "of", "on", "in", "and", "or", "book", "books", "about", "with", "me",
            "give", "find", "show", "some", "any", "please", "need", "looking",
            "that", "this", "it", "at", "by", "as", "be", "can", "will"
    ));

    public static List<Integer> smartSearch(Connection con, String userQuery) throws Exception {

        // Step 1: Load all books
        List<Integer> bookIds = new ArrayList<>();
        List<String> documents = new ArrayList<>();

        String sql = "SELECT book_id, title, author, description FROM books";
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            bookIds.add(rs.getInt("book_id"));

            String title = safe(rs.getString("title"));
            String author = safe(rs.getString("author"));
            String description = "";
            try { description = safe(rs.getString("description")); } catch (Exception ignore) {}

            // Weight title words more by repeating them (simple boosting trick)
            documents.add(title + " " + title + " " + author + " " + description);
        }
        rs.close();
        pst.close();

        if (bookIds.isEmpty()) return Collections.emptyList();

        // Step 2: Tokenize all documents + the query
        List<List<String>> tokenizedDocs = new ArrayList<>();
        for (String doc : documents) {
            tokenizedDocs.add(tokenize(doc));
        }
        List<String> queryTokens = tokenize(userQuery);

        if (queryTokens.isEmpty()) return Collections.emptyList();

        // Step 3: Build vocabulary (all unique words across documents)
        Set<String> vocabulary = new HashSet<>();
        for (List<String> doc : tokenizedDocs) vocabulary.addAll(doc);

        // Step 3b: Fuzzy-correct query words that don't exist in the vocabulary
        // (handles typos like "programing" -> "programming")
        List<String> correctedQueryTokens = new ArrayList<>();
        for (String qWord : queryTokens) {
            if (vocabulary.contains(qWord)) {
                correctedQueryTokens.add(qWord);
            } else {
                String closest = findClosestWord(qWord, vocabulary);
                correctedQueryTokens.add(closest != null ? closest : qWord);
            }
        }
        queryTokens = correctedQueryTokens;
        vocabulary.addAll(queryTokens);

        // Step 4: Compute IDF (Inverse Document Frequency) for each word
        Map<String, Double> idf = new HashMap<>();
        int totalDocs = tokenizedDocs.size();

        for (String word : vocabulary) {
            int docCount = 0;
            for (List<String> doc : tokenizedDocs) {
                if (doc.contains(word)) docCount++;
            }
            // +1 smoothing to avoid division by zero
            double idfValue = Math.log((double) (totalDocs + 1) / (docCount + 1)) + 1;
            idf.put(word, idfValue);
        }

        // Step 5: Compute TF-IDF vector for the query
        Map<String, Double> queryVector = computeTfIdfVector(queryTokens, idf);

        // Step 6: Compute TF-IDF vector for each document, then cosine similarity vs query
        List<double[]> scores = new ArrayList<>(); // [bookIndex, similarityScore]

        for (int i = 0; i < tokenizedDocs.size(); i++) {
            Map<String, Double> docVector = computeTfIdfVector(tokenizedDocs.get(i), idf);
            double similarity = cosineSimilarity(queryVector, docVector);
            scores.add(new double[]{i, similarity});
        }

        // Step 7: Sort by similarity descending
        scores.sort((a, b) -> Double.compare(b[1], a[1]));

        // Step 8: Return top matches with a meaningful similarity (> 0), max 10
        List<Integer> result = new ArrayList<>();
        for (double[] entry : scores) {
            if (entry[1] <= 0.0001) break; // no more relevant matches
            result.add(bookIds.get((int) entry[0]));
            if (result.size() >= 10) break;
        }

        return result;
    }

    // --- Helper methods ---

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static List<String> tokenize(String text) {
        String lower = text.toLowerCase();
        String[] words = lower.split("[^a-z0-9]+");
        List<String> tokens = new ArrayList<>();
        for (String w : words) {
            if (w.length() > 1 && !STOP_WORDS.contains(w)) {
                tokens.add(w);
            }
        }
        return tokens;
    }

    private static Map<String, Double> computeTfIdfVector(List<String> tokens, Map<String, Double> idf) {
        Map<String, Integer> termCount = new HashMap<>();
        for (String token : tokens) {
            termCount.merge(token, 1, Integer::sum);
        }

        Map<String, Double> vector = new HashMap<>();
        int totalTerms = tokens.size();

        for (Map.Entry<String, Integer> entry : termCount.entrySet()) {
            double tf = (double) entry.getValue() / totalTerms;
            double idfValue = idf.getOrDefault(entry.getKey(), 1.0);
            vector.put(entry.getKey(), tf * idfValue);
        }

        return vector;
    }

    // Finds the closest word in the vocabulary to handle typos.
    // Only accepts matches within a small edit distance so it doesn't
    // wrongly "correct" a genuinely different word.
    private static String findClosestWord(String word, Set<String> vocabulary) {
        String best = null;
        int bestDistance = Integer.MAX_VALUE;

        // Allow up to 2 character edits for longer words, 1 for short words
        int maxAllowedDistance = word.length() <= 4 ? 1 : 2;

        for (String vocabWord : vocabulary) {
            // Quick length filter before doing the expensive distance calculation
            if (Math.abs(vocabWord.length() - word.length()) > maxAllowedDistance) continue;

            int distance = levenshteinDistance(word, vocabWord);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = vocabWord;
            }
        }

        return (bestDistance <= maxAllowedDistance) ? best : null;
    }

    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    private static double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> allWords = new HashSet<>(v1.keySet());
        allWords.addAll(v2.keySet());

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String word : allWords) {
            double a = v1.getOrDefault(word, 0.0);
            double b = v2.getOrDefault(word, 0.0);
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA == 0 || normB == 0) return 0.0;

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}