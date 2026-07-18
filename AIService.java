import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.*;

/*
 * AIService - REAL AI powered book search.
 *
 * Ipo neenga panra "AI" search (SearchBooks by category with LIKE query) - adhu
 * AI illa, adhu just SQL keyword match. Real AI panna rendu approach irukku:
 *
 * APPROACH 1 (RECOMMENDED - easy, cheap, fast): "AI re-ranks the results"
 *   1. User oru natural sentence type pannuvaru: "I want a thrilling mystery novel"
 *   2. Neenga ella books (title+author+category) DB la irundhu edukanum
 *   3. Andha list + user query ah Claude API ku anupanum
 *   4. Claude best-matching books ah pick pannitu, EN yellow explain pannum
 *   5. Adha UI la kaata
 *
 * APPROACH 2 (advanced): Embeddings based semantic search
 *   - Create an embedding vector for each book description and store it in the DB.
 *   - User query kum vector create pannunga
 *   - Cosine similarity vachu close matches edunga
 *   - Idhu perf ah nalla irukum but implement panna konjam complex (needs a vector DB
 *     or manual cosine-similarity code + embedding API)
 *
 * Idhu keela irukra code Approach 1 - "LLM re-ranks results" - use pannitu iruken,
 * karana idhu setup panna easy, and un existing Access DB oda nalla work aagum.
 *
 * SETUP:
 *   1. Anthropic Console la (https://console.anthropic.com) sign up pannitu API key edunga
 *   2. Andha key ah environment variable ah vachukonga: ANTHROPIC_API_KEY
 *      (NEVER hardcode the key in your source code / never commit to GitHub)
 *   3. Below code Java 11+ HttpClient use pannudhu (already built-in, no extra library)
 */
public class AIService {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_KEY = System.getenv("ANTHROPIC_API_KEY");
    private static final String MODEL = "claude-sonnet-4-6"; // change if needed

    /**
     * Takes a natural-language user query, fetches all books from DB,
     * asks the AI to pick and rank the best matches, and returns book_ids in order.
     */
    public static List<Integer> smartSearch(Connection con, String userQuery) throws Exception {

        // Step 1: Pull all books (id + title + author + category/description)
        StringBuilder bookListText = new StringBuilder();
        Map<Integer, String> idToTitle = new LinkedHashMap<>();

        String sql = "SELECT book_id, title, author FROM books";
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("book_id");
            String title = rs.getString("title");
            String author = rs.getString("author");
            idToTitle.put(id, title);
            bookListText.append(id).append(": \"").append(title)
                    .append("\" by ").append(author).append("\n");
        }
        rs.close();
        pst.close();

        if (idToTitle.isEmpty()) return Collections.emptyList();

        // Step 2: Build the prompt - ask Claude to return ONLY a JSON array of book_ids
        String prompt = "You are a library book search assistant. Here is the full book catalog:\n\n"
                + bookListText.toString()
                + "\nUser request: \"" + userQuery + "\"\n\n"
                + "Return ONLY a JSON array of the book_id numbers that best match the user's "
                + "request, ordered from most relevant to least relevant. "
                + "Return at most 10 ids. If nothing matches well, return an empty array []. "
                + "Do not include any explanation, only the JSON array.";

        String jsonBody = "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"max_tokens\":300,"
                + "\"messages\":[{\"role\":\"user\",\"content\":" + escapeJson(prompt) + "}]"
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", API_KEY)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Step 3: Parse the "text" field out of Claude's JSON response, then parse the id array
        String responseBody = response.body();
        String text = extractTextField(responseBody);

        return parseIdArray(text);
    }

    // --- tiny helpers, no external JSON library needed ---

    private static String escapeJson(String s) {
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n") + "\"";
    }

    private static String extractTextField(String responseJson) {
        // crude extraction of the first "text":"..." value
        int idx = responseJson.indexOf("\"text\":\"");
        if (idx == -1) return "[]";
        int start = idx + 8;
        int end = responseJson.indexOf("\"", start);
        while (end > 0 && responseJson.charAt(end - 1) == '\\') {
            end = responseJson.indexOf("\"", end + 1);
        }
        if (end == -1) return "[]";
        return responseJson.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
    }

    private static List<Integer> parseIdArray(String text) {
        List<Integer> ids = new ArrayList<>();
        int start = text.indexOf('[');
        int end = text.indexOf(']');
        if (start == -1 || end == -1) return ids;

        String inner = text.substring(start + 1, end).trim();
        if (inner.isEmpty()) return ids;

        for (String part : inner.split(",")) {
            try {
                ids.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignore) {}
        }
        return ids;
    }
}
