import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class ViewBook extends JFrame {

    private Connection con;
    private String userId;
    JTable table;
    DefaultTableModel model;
    JLabel lblCount;
    JTextField txtSearch;

    // ---- Theme (matches the parchment / antique background photo) ----
    private static final Color BROWN_DARK   = new Color(92, 64, 51);
    private static final Color HEADER_BG    = new Color(66, 47, 38, 235);   // translucent dark brown
    private static final Color ROW_A        = new Color(255, 250, 240, 165); // translucent cream
    private static final Color ROW_B        = new Color(232, 214, 188, 150); // translucent tan
    private static final Color ROW_SELECTED = new Color(210, 168, 104, 210); // gold highlight
    private static final Color CARD_BG      = new Color(255, 250, 240, 190); // search bar card
    private static final Color GOLD_BORDER  = new Color(178, 140, 74);
    private static final Color BTN_PRIMARY  = new Color(92, 64, 51);
    private static final Color BTN_SECOND   = new Color(178, 140, 74);
    private static final Color BTN_DANGER   = new Color(196, 48, 63);

    public ViewBook(Connection con, String userId) {

        this.con = con;
        this.userId = userId;

        setTitle("View Books");
        setSize(1000, 650);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(new BorderLayout());
        bgPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(bgPanel);

        // ---- TOP: title + search card ----
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JLabel title = new JLabel("Books Collection", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(BROWN_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        topContainer.add(title);
        topContainer.add(Box.createVerticalStrut(12));

        JPanel searchCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchCard.setOpaque(true);
        searchCard.setBackground(CARD_BG);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD_BORDER, 1),
                new EmptyBorder(6, 12, 6, 12)));
        searchCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSearch = new JLabel("Search Title/Author:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(BROWN_DARK);
        searchCard.add(lblSearch);

        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD_BORDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
        txtSearch.setBackground(Color.WHITE);
        searchCard.add(txtSearch);

        JButton btnSearch = makeStyledButton("Search", BTN_PRIMARY, Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(95, 32));
        searchCard.add(btnSearch);

        JButton btnShowAll = makeStyledButton("Show All", BTN_SECOND, Color.WHITE);
        btnShowAll.setPreferredSize(new Dimension(100, 32));
        searchCard.add(btnShowAll);

        lblCount = new JLabel("Total Books: 0");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCount.setForeground(new Color(30, 90, 160));
        searchCard.add(lblCount);

        topContainer.add(searchCard);
        topContainer.add(Box.createVerticalStrut(12));
        bgPanel.add(topContainer, BorderLayout.NORTH);

        // ---- TABLE ----
        model = new DefaultTableModel(
                new Object[]{"Book_ID", "Book_name", "Author", "Publisher", "Edition", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(ROW_SELECTED);
                    c.setForeground(BROWN_DARK);
                } else {
                    c.setBackground(row % 2 == 0 ? ROW_A : ROW_B);
                    c.setForeground(BROWN_DARK);
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setOpaque(true);
                }
                return c;
            }
        };
        table.setOpaque(false);
        table.setDefaultEditor(Object.class, null);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(BROWN_DARK);
        table.setFillsViewportHeight(true); // table area fills the panel; empty space stays transparent

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int bookId = (int) model.getValueAt(row, 0);
                        new BookDetailView(con, bookId).setVisible(true);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(GOLD_BORDER, 1));
        bgPanel.add(scrollPane, BorderLayout.CENTER);

        // ---- BOTTOM: back button ----
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        JButton backBtn = makeStyledButton("Back", BTN_DANGER, Color.WHITE);
        backBtn.setPreferredSize(new Dimension(140, 40));

        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true);
        });

        bottomPanel.add(backBtn);
        bgPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ---- ACTIONS ----
        btnSearch.addActionListener(e -> loadBooks(txtSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadBooks(""); });
        txtSearch.addActionListener(e -> loadBooks(txtSearch.getText().trim()));

        loadBooks("");

        setVisible(true);
    }

    private void loadBooks(String keyword) {
        model.setRowCount(0);
        int count = 0;

        try {
            String query;
            PreparedStatement pst;

            if (keyword.isEmpty()) {
                query = "SELECT * FROM books";
                pst = con.prepareStatement(query);
            } else {
                query = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?";
                pst = con.prepareStatement(query);
                pst.setString(1, "%" + keyword + "%");
                pst.setString(2, "%" + keyword + "%");
            }

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getString("edition"),
                        rs.getString("quantity")
                });
                count++;
            }

            lblCount.setText("Total Books: " + count);

            rs.close();
            pst.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
    }

    /** Flat rounded button with translucent fill and hover feedback. */
    private JButton makeStyledButton(String text, Color base, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? base.brighter() : base;
                if (getModel().isPressed()) bg = base.darker();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { btn.repaint(); }
        });
        return btn;
    }

    class BackgroundPanel extends JPanel {
        Image bg = new ImageIcon("bgphoto/ViewBook_bg.jpg").getImage();

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}