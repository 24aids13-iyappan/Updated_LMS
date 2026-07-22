import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class UserDashboard extends JFrame {

    private String userid;
    private Connection con;
    private JPanel chatPanel;
    private JScrollPane chatScroll;
    private JTextField txtInput;
    private JButton toggleChatBtn;

    public UserDashboard(Connection con, String userid) {
        this.con = con;
        this.userid = userid;

        setTitle("User Dashboard");
        setSize(750, 650);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        // Fetch the student's name to display instead of the raw user ID
        String displayName = userid;
        try {
            PreparedStatement namePst = con.prepareStatement("SELECT name FROM users WHERE user_id = ?");
            namePst.setString(1, userid);
            ResultSet nameRs = namePst.executeQuery();
            if (nameRs.next()) {
                displayName = nameRs.getString("name");
            }
            nameRs.close();
            namePst.close();
        } catch (Exception ignore) {
            // fall back to userid if lookup fails
        }

        // TOP - Welcome banner
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setOpaque(true);
        topBanner.setBackground(new Color(0, 0, 0, 140));
        topBanner.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Welcome, " + displayName + "!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        topBanner.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Ask me for any book below \u2193");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(220, 220, 220));
        topBanner.add(subtitle, BorderLayout.SOUTH);

        toggleChatBtn = new JButton("\u25B2"); // up arrow = "show chat"
        toggleChatBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toggleChatBtn.setFocusPainted(false);
        toggleChatBtn.setToolTipText("Show/hide chat");
        toggleChatBtn.setVisible(false); // only shows once there's something to collapse
        topBanner.add(toggleChatBtn, BorderLayout.EAST);

        bgPanel.add(topBanner, BorderLayout.NORTH);

        // CENTER - Embedded chat area (starts collapsed - only the search bar shows at first)
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setOpaque(true);
        chatPanel.setBackground(new Color(245, 238, 220, 235));
        chatPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        chatScroll = new JScrollPane(chatPanel);
        chatScroll.setBorder(null);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        chatScroll.setOpaque(false);
        chatScroll.getViewport().setOpaque(false);
        chatScroll.setVisible(false); // hidden until the user searches or clicks the arrow
        bgPanel.add(chatScroll, BorderLayout.CENTER);

        toggleChatBtn.addActionListener(e -> {
            boolean nowVisible = !chatScroll.isVisible();
            chatScroll.setVisible(nowVisible);
            toggleChatBtn.setText(nowVisible ? "\u25BC" : "\u25B2"); // down arrow when open, up when closed
            bgPanel.revalidate();
            bgPanel.repaint();
        });

        // BOTTOM - Search input bar
        JPanel inputBar = new JPanel(new BorderLayout(8, 0));
        inputBar.setBorder(BorderFactory.createEmptyBorder(12, 15, 15, 15));
        inputBar.setOpaque(true);
        inputBar.setBackground(new Color(0, 0, 0, 140));

        txtInput = new JTextField();
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JButton btnSend = new JButton("Search");
        btnSend.setBackground(new Color(150, 50, 200));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setFocusPainted(false);

        inputBar.add(txtInput, BorderLayout.CENTER);
        inputBar.add(btnSend, BorderLayout.EAST);
        bgPanel.add(inputBar, BorderLayout.SOUTH);

        btnSend.addActionListener(e -> sendMessage());
        txtInput.addActionListener(e -> sendMessage());

        addAiTextBubble("Hi! Tell me what kind of book you're looking for " +
                "(e.g. \"a book to learn programming basics\").");

        // MENU BAR
        JMenuBar menubar = new JMenuBar();
        JMenu menuUser = new JMenu("User");
        JMenuItem menuuserprofile = new JMenuItem("Profile");
        JMenuItem logout = new JMenuItem("Logout");
        menuUser.add(menuuserprofile);
        menuUser.add(logout);

        JMenu menubook = new JMenu("Books");
        JMenuItem menusearchbook = new JMenuItem("Browse All Books");
        JMenuItem menurequest = new JMenuItem("Request Book");
        JMenuItem menuborrow = new JMenuItem("My borrowed Books");
        JMenuItem menufinebook = new JMenuItem("Fine Details");

        menubook.add(menusearchbook);
        menubook.add(menurequest);
        menubook.add(menuborrow);
        menubook.add(menufinebook);

        menubar.add(menuUser);
        menubar.add(menubook);
        setJMenuBar(menubar);

        menusearchbook.addActionListener(e -> { dispose(); new BookCatalog(con, userid).setVisible(true); });
        menurequest.addActionListener(e -> { dispose(); new RequestBook(con, userid).setVisible(true); });
        menufinebook.addActionListener(e -> { dispose(); new ViewFineUser(con, userid).setVisible(true); });
        menuuserprofile.addActionListener(e -> { dispose(); new UserProfile(con, userid).setVisible(true); });
        menuborrow.addActionListener(e -> { dispose(); new BorrowedBooks(con, userid).setVisible(true); });

        logout.addActionListener(e -> {
            dispose();
            new LoginGUI(con).setVisible(true);
        });
    }

    private void sendMessage() {
        String query = txtInput.getText().trim();
        if (query.isEmpty()) return;

        // First search auto-expands the chat area and reveals the toggle arrow
        if (!chatScroll.isVisible()) {
            chatScroll.setVisible(true);
            toggleChatBtn.setVisible(true);
            toggleChatBtn.setText("\u25BC");
        }

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
                    addAiTextBubble("Something went wrong: " + ex.getMessage());
                    txtInput.setEnabled(true);
                });
            }
        }).start();
    }

    private void addUserBubble(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bubble = new JLabel("<html><div style='width:300px;'>" + escapeHtml(text) + "</div></html>");
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

        JLabel bubble = new JLabel("<html><div style='width:350px;'>" + escapeHtml(text) + "</div></html>");
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
        wrapper.setMaximumSize(new Dimension(450, 1000));

        if (matchedIds.isEmpty()) {
            JLabel noResult = new JLabel("I couldn't find a good match for \"" + escapeHtml(query) +
                    "\". Try describing it differently.");
            noResult.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            wrapper.add(noResult);
        } else {
            JLabel header = new JLabel("Found " + matchedIds.size() + " book(s) for \"" + escapeHtml(query) + "\":");
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
                        bookRow.setMaximumSize(new Dimension(420, 35));

                        JLabel bookLabel = new JLabel("[ID " + id + "] " + bookTitle + " \u2014 " + author);
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
                wrapper.add(new JLabel("Error loading book details."));
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

    class BackgroundPanel extends JPanel {
        Image bg = new ImageIcon("bgphoto/Udash_bg.jpg").getImage();

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}