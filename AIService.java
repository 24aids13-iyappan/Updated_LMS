import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.*;
 
/*
 * AIService - REAL AI powered book search.
 * (Approach 1: LLM re-ranks results from the full book catalog)
 *
 * SETUP:
 *   1. Get API key from https://console.anthropic.com
 *   2. Set env variable: ANTHROPIC_API_KEY
 *   3. Uses Java 11+ built-in HttpClient (no extra library needed)
 */
public class AIService {
 
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_KEY = System.getenv("ANTHROPIC_API_KEY");
    private static final String MODEL = "claude-sonnet-5";   // FIXED - was "claude-sonnet-4-6" (invalid)
 
    public static List<Integer> smartSearch(Connection con, String userQuery) throws Exception {
 
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new Exception("ANTHROPIC_API_KEY environment variable is not set!");
        }
 
        // Step 1: Pull all books
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
 
        // Step 2: Build the prompt
        String prompt = "You are a library book search assistant. Here is the full book catalog:\n\n"
                + bookListText.toString()
                + "\nUser request: \"" + userQuery + "\"\n\n"
                + "Return ONLY a JSON array of the book_id numbers that best match the user's "
                + "request, ordered from most relevant to least relevant. "
                + "Return at most 10 ids. If nothing matches well, return an empty array []. "
                + "Do not include any explanation, only the JSON array.";
 
        String jsonBody = "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"max_tokens\":500,"
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
        String responseBody = response.body();
 
        // CRITICAL FIX - check status code BEFORE parsing.
        // Without this, API errors (bad model name, bad key, etc.) were silently
        // treated as "no matches found" instead of showing the real problem.
        if (response.statusCode() != 200) {
            throw new Exception("API Error (status " + response.statusCode() + "): " + responseBody);
        }
 
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