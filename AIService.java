import java.sql.*;
import java.util.*;
 
/*
 * AIService - Local "smart search" using TF-IDF + Cosine Similarity + Coverage ranking.
 * NO API calls, NO cost, NO internet needed - works 100% offline.
 *
 * KEY FIX: results are now grouped by "how many distinct query words matched".
 * Example: query "java core" (2 words)
 *   - "Core Java" matches BOTH words -> shown
 *   - "Java Basic" matches only "java" -> NOT shown (lower coverage)
 * This is why searching "java core" now returns ONLY Core Java, not every Java book.
 *
 * But query "basic" alone (1 word) still correctly returns ALL "*Basic" books,
 * since they all match that single word equally.
 */
public class AIService {
 
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
 
            documents.add(title + " " + title + " " + author + " " + description);
        }
        rs.close();
        pst.close();
 
        if (bookIds.isEmpty()) return Collections.emptyList();
 
        // Step 2: Tokenize documents + query
        List<List<String>> tokenizedDocs = new ArrayList<>();
        for (String doc : documents) tokenizedDocs.add(tokenize(doc));
 
        List<String> queryTokens = tokenize(userQuery);
        if (queryTokens.isEmpty()) return Collections.emptyList();
 
        // Step 3: Vocabulary + fuzzy-correct typos in the query
        Set<String> vocabulary = new HashSet<>();
        for (List<String> doc : tokenizedDocs) vocabulary.addAll(doc);
 
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
        Set<String> distinctQueryTerms = new HashSet<>(queryTokens);
        vocabulary.addAll(queryTokens);
 
        // Step 4: IDF for each word
        Map<String, Double> idf = new HashMap<>();
        int totalDocs = tokenizedDocs.size();
 
        for (String word : vocabulary) {
            int docCount = 0;
            for (List<String> doc : tokenizedDocs) {
                if (doc.contains(word)) docCount++;
            }
            double idfValue = Math.log((double) (totalDocs + 1) / (docCount + 1)) + 1;
            idf.put(word, idfValue);
        }
 
        // Step 5: Query vector
        Map<String, Double> queryVector = computeTfIdfVector(queryTokens, idf);
 
        // Step 6: For each doc, compute similarity score AND "coverage"
        // (how many distinct query words actually appear in that document)
        List<int[]> coverageAndIndex = new ArrayList<>();   // [docIndex, coverageCount]
        List<Double> similarityScores = new ArrayList<>();
 
        for (int i = 0; i < tokenizedDocs.size(); i++) {
            List<String> doc = tokenizedDocs.get(i);
            Set<String> docWordSet = new HashSet<>(doc);
 
            int coverage = 0;
            for (String term : distinctQueryTerms) {
                if (docWordSet.contains(term)) coverage++;
            }
 
            Map<String, Double> docVector = computeTfIdfVector(doc, idf);
            double similarity = cosineSimilarity(queryVector, docVector);
 
            coverageAndIndex.add(new int[]{i, coverage});
            similarityScores.add(similarity);
        }
 
        // Step 7: Find the best coverage level achieved by any document
        int maxCoverage = 0;
        for (int[] entry : coverageAndIndex) {
            maxCoverage = Math.max(maxCoverage, entry[1]);
        }
 
        if (maxCoverage == 0) return Collections.emptyList(); // nothing matched at all
 
        // Step 8: Keep ONLY documents that matched the maximum coverage level,
        // then rank those by similarity score (highest first)
        List<int[]> topTier = new ArrayList<>();
        for (int[] entry : coverageAndIndex) {
            if (entry[1] == maxCoverage) topTier.add(entry);
        }
 
        topTier.sort((a, b) -> Double.compare(similarityScores.get(b[0]), similarityScores.get(a[0])));
 
        List<Integer> result = new ArrayList<>();
        for (int[] entry : topTier) {
            result.add(bookIds.get(entry[0]));
            if (result.size() >= 5) break;
        }
 
        return result;
    }
 
    // --- Helper methods ---
 
    private static String safe(String s) {
        return s == null ? "" : s;
    }
 
    private static List<String> tokenize(String text) {
        String lower = text.toLowerCase();
 
        lower = lower.replace("c++", "cplusplus")
                     .replace("c#", "csharp")
                     .replace(".net", "dotnet");
 
        String[] words = lower.split("[^a-z0-9]+");
        List<String> tokens = new ArrayList<>();
        for (String w : words) {
            if (w.length() > 1 && !STOP_WORDS.contains(w)) {
                tokens.add(w);
            }
        }
        return tokens;
    }
 
    private static String findClosestWord(String word, Set<String> vocabulary) {
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        int maxAllowedDistance = word.length() <= 4 ? 1 : 2;
 
        for (String vocabWord : vocabulary) {
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
 
    private static Map<String, Double> computeTfIdfVector(List<String> tokens, Map<String, Double> idf) {
        Map<String, Integer> termCount = new HashMap<>();
        for (String token : tokens) termCount.merge(token, 1, Integer::sum);
 
        Map<String, Double> vector = new HashMap<>();
        int totalTerms = tokens.size();
 
        for (Map.Entry<String, Integer> entry : termCount.entrySet()) {
            double tf = (double) entry.getValue() / totalTerms;
            double idfValue = idf.getOrDefault(entry.getKey(), 1.0);
            vector.put(entry.getKey(), tf * idfValue);
        }
        return vector;
    }
 
    private static double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> allWords = new HashSet<>(v1.keySet());
        allWords.addAll(v2.keySet());
 
        double dotProduct = 0.0, normA = 0.0, normB = 0.0;
 
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