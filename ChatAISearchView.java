import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;

/*
 * ChatAISearchView - Chat-style interface for AI book search.
 * User types a message (like chatting), AI replies with matched books
 * (showing Book ID, Title, Author) in a chat bubble. Conversation keeps
 * scrolling like a normal chat app. Search box stays at the bottom,
 * Google-style, so user can keep asking follow-up questions.
 */
public class ChatAISearchView extends JFrame {

    private Connection con;
    private String userid;
    private JPanel chatPanel;
    private JScrollPane chatScroll;
    private JTextField txtInput;

    public ChatAISearchView(Connection con, String userid) {
        this(con, userid, null);
    }

    public ChatAISearchView(Connection con, String userid, String initialQuery) {

        this.con = con;
        this.userid = userid;

        setTitle("AI Library Assistant");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        setContentPane(main);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(92, 64, 51));
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel title = new JLabel("AI Library Assistant");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        topBar.add(title, BorderLayout.WEST);

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.addActionListener(e -> {
            dispose();
            new UserDashboard(con, userid).setVisible(true);
        });
        topBar.add(backBtn, BorderLayout.EAST);

        main.add(topBar, BorderLayout.NORTH);

        // Chat area (scrollable, messages stack vertically)
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(245, 238, 220));
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chatScroll = new JScrollPane(chatPanel);
        chatScroll.setBorder(null);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        main.add(chatScroll, BorderLayout.CENTER);

        // Bottom input bar - Google-style search box
        JPanel inputBar = new JPanel(new BorderLayout(8, 0));
        inputBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputBar.setBackground(Color.WHITE);

        txtInput = new JTextField();
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JButton btnSend = new JButton("Send");
        btnSend.setBackground(new Color(150, 50, 200));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setFocusPainted(false);

        inputBar.add(txtInput, BorderLayout.CENTER);
        inputBar.add(btnSend, BorderLayout.EAST);
        main.add(inputBar, BorderLayout.SOUTH);

        // Actions
        btnSend.addActionListener(e -> sendMessage());
        txtInput.addActionListener(e -> sendMessage()); // Enter key also sends

        // Welcome message
        addAiTextBubble("Hi! Tell me what kind of book you're looking for " +
                "(e.g. \"a book to learn programming basics\" or \"space adventure story\").");

        setVisible(true);

        // If dashboard passed in an initial query, send it right away
        if (initialQuery != null && !initialQuery.trim().isEmpty()) {
            txtInput.setText(initialQuery.trim());
            sendMessage();
        }
    }

    private void sendMessage() {

        String query = txtInput.getText().trim();
        if (query.isEmpty()) return;

        addUserBubble(query);
        txtInput.setText("");
        txtInput.setEnabled(false);

        JPanel typingBubble = addAiTypingBubble();

        new Thread(() -> {
            try {
                List<Integer> matchedIds = AIService.smartSearch(con, query);

                SwingUtilities.invokeLater(() -> {
                    chatPanel.remove(typingBubble);
                    addAiResultBubble(query, matchedIds);
                    txtInput.setEnabled(true);
                    txtInput.requestFocus();
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.remove(typingBubble);
                    addAiTextBubble("Sorry, something went wrong while searching: " + ex.getMessage());
                    txtInput.setEnabled(true);
                });
            }
        }).start();
    }

    // ---------- Chat bubble builders ----------

    private void addUserBubble(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bubble = new JLabel("<html><div style='width:350px;'>" + escapeHtml(text) + "</div></html>");
        bubble.setOpaque(true);
        bubble.setBackground(new Color(0, 120, 215));
        bubble.setForeground(Color.WHITE);
        bubble.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        row.add(bubble);
        chatPanel.add(row);
        chatPanel.add(Box.createVerticalStrut(10));
        refreshChat();
    }

    private void addAiTextBubble(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bubble = new JLabel("<html><div style='width:400px;'>" + escapeHtml(text) + "</div></html>");
        bubble.setOpaque(true);
        bubble.setBackground(Color.WHITE);
        bubble.setForeground(Color.BLACK);
        bubble.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        row.add(bubble);
        chatPanel.add(row);
        chatPanel.add(Box.createVerticalStrut(10));
        refreshChat();
    }

    private JPanel addAiTypingBubble() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bubble = new JLabel("Searching...");
        bubble.setOpaque(true);
        bubble.setBackground(Color.WHITE);
        bubble.setForeground(Color.GRAY);
        bubble.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        row.add(bubble);
        chatPanel.add(row);
        chatPanel.add(Box.createVerticalStrut(10));
        refreshChat();
        return row;
    }

    private void addAiResultBubble(String query, List<Integer> matchedIds) {

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(true);
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(500, 1000));

        if (matchedIds.isEmpty()) {
            JLabel noResult = new JLabel("I couldn't find a good match for \"" + escapeHtml(query) +
                    "\". Try describing it differently.");
            noResult.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            wrapper.add(noResult);
        } else {
            JLabel header = new JLabel("I found " + matchedIds.size() + " book(s) for \"" + escapeHtml(query) + "\":");
            header.setFont(new Font("Segoe UI", Font.BOLD, 14));
            header.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(header);
            wrapper.add(Box.createVerticalStrut(8));

            try {
                for (int id : matchedIds) {
                    PreparedStatement pst = con.prepareStatement("SELECT * FROM books WHERE book_id=?");
                    pst.setInt(1, id);
                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        String bookTitle = rs.getString("title");
                        String author = rs.getString("author");

                        JPanel bookRow = new JPanel(new BorderLayout(10, 0));
                        bookRow.setOpaque(false);
                        bookRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                        bookRow.setMaximumSize(new Dimension(470, 35));

                        JLabel bookLabel = new JLabel("[ID " + id + "] " + bookTitle + " — " + author);
                        bookLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                        JButton viewBtn = new JButton("View");
                        viewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                        viewBtn.addActionListener(e -> new BookDetailView(con, id).setVisible(true));

                        bookRow.add(bookLabel, BorderLayout.CENTER);
                        bookRow.add(viewBtn, BorderLayout.EAST);

                        wrapper.add(bookRow);
                        wrapper.add(Box.createVerticalStrut(4));
                    }
                    rs.close();
                    pst.close();
                }
            } catch (Exception ex) {
                JLabel errLabel = new JLabel("Error loading book details.");
                wrapper.add(errLabel);
            }
        }

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(wrapper);

        chatPanel.add(row);
        chatPanel.add(Box.createVerticalStrut(10));
        refreshChat();
    }

    private void refreshChat() {
        chatPanel.revalidate();
        chatPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScroll.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
