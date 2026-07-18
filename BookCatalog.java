import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;

/*
 * BookCatalog - Replaces SearchBook.java + ViewBook2.java (user side).
 * Ella books um default ah kaatum (scrollable), top la search box irundhu
 * andha same table la filter pannalam. Total book count um kaatum.
 *
 * Wiring: UserDashboard.java la:
 *   menusearchbook.addActionListener(e -> { dispose(); new BookCatalog(con,userid).setVisible(true);});
 *   menuviewbook.addActionListener(e -> { dispose(); new BookCatalog(con,userid).setVisible(true);});
 *   (Rendu menu items um same class ku point pannalam, illana "View Book" menu item ah remove
 *    pannitu "Search Book" mattum vachukonga)
 */
public class BookCatalog extends JFrame {

    private Connection con;
    private String userid;
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblCount;

    public BookCatalog(Connection con, String userid) {

        this.con = con;
        this.userid = userid;

        setTitle("Book Catalog");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // TITLE
        JLabel titleLabel = new JLabel("Book Catalog");
        titleLabel.setBounds(430, 30, 300, 35);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        titleLabel.setForeground(new Color(80, 40, 20));
        bgPanel.add(titleLabel);

        // SEARCH LABEL + BOX
        JLabel lblSearch = new JLabel("Search Title/Author:");
        lblSearch.setBounds(150, 90, 180, 30);
        lblSearch.setFont(new Font("Georgia", Font.BOLD, 16));
        lblSearch.setForeground(new Color(80, 40, 20));
        bgPanel.add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setBounds(340, 90, 250, 35);
        txtSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtSearch.setBackground(new Color(255, 248, 220));
        txtSearch.setForeground(Color.BLACK);
        txtSearch.setBorder(BorderFactory.createLineBorder(new Color(120, 70, 30), 2));
        bgPanel.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(600, 90, 110, 35);
        btnSearch.setBackground(new Color(120, 70, 30));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bgPanel.add(btnSearch);

        JButton btnShowAll = new JButton("Show All");
        btnShowAll.setBounds(720, 90, 110, 35);
        btnShowAll.setBackground(new Color(40, 167, 69));
        btnShowAll.setForeground(Color.WHITE);
        btnShowAll.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bgPanel.add(btnShowAll);

        // TOTAL COUNT LABEL
        lblCount = new JLabel("Total Books: 0");
        lblCount.setBounds(150, 130, 300, 25);
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCount.setForeground(new Color(0, 120, 215));
        bgPanel.add(lblCount);

        // BACK BUTTON
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(850, 90, 100, 35);
        backBtn.setBackground(new Color(180, 50, 50));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bgPanel.add(backBtn);
JButton btnAISearch = new JButton("AI Search");
btnAISearch.setBounds(830, 90, 140, 35);
btnAISearch.setBackground(new Color(150, 50, 200));
btnAISearch.setForeground(Color.WHITE);
btnAISearch.setFont(new Font("Segoe UI", Font.BOLD, 15));
bgPanel.add(btnAISearch);
        // TABLE MODEL
        model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Publisher");
        model.addColumn("Edition");
        model.addColumn("Quantity");

        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setRowHeight(30);
        table.setForeground(Color.BLACK);
        table.setDefaultEditor(Object.class, null); // allow double-click detail popup

        table.setOpaque(false);
        ((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class)).setOpaque(false);

        table.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(120, 70, 30));
        table.getTableHeader().setForeground(Color.WHITE);

        // Double-click -> open BookDetailView (image + description)
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
        scrollPane.setBounds(150, 170, 800, 450);   // 👈 scrollable area, ella books um scroll panni paakalam
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(120, 70, 30), 2));
        bgPanel.add(scrollPane);

        // ACTIONS
        btnSearch.addActionListener(e -> loadBooks(txtSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadBooks(""); });

        backBtn.addActionListener(e -> {
            dispose();
            new UserDashboard(con, userid).setVisible(true);
        });
btnAISearch.addActionListener(e -> aiSearchBooks());
        loadBooks(""); // default - show all books, scrollable

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
            JOptionPane.showMessageDialog(this, "Error loading books!");
        }
    }
    private void aiSearchBooks() {

    String userQuery = txtSearch.getText().trim();

    if (userQuery.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Type what kind of book you want!");
        return;
    }

    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    new Thread(() -> {
        try {
            java.util.List<Integer> matchedIds = AIService.smartSearch(con, userQuery);

            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);
                int count = 0;

                if (matchedIds.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No good matches found. Try different words!");
                } else {
                    try {
                        for (int id : matchedIds) {
                            PreparedStatement pst = con.prepareStatement(
                                    "SELECT * FROM books WHERE book_id=?");
                            pst.setInt(1, id);
                            ResultSet rs = pst.executeQuery();
                            if (rs.next()) {
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
                            rs.close();
                            pst.close();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error loading matched books");
                    }
                }
                lblCount.setText("Total Books: " + count);
                setCursor(Cursor.getDefaultCursor());
            });

        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this,
                        "AI search failed: " + ex.getMessage());
            });
        }
    }).start();
}

    class BackgroundPanel extends JPanel {
        Image bg = new ImageIcon("bgphoto/viewbooks_bg.jpg").getImage();

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
