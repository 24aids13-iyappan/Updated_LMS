import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class ViewBook extends JFrame {

    private Connection con;
    private String userId;
    JTable table;
    DefaultTableModel model;
    JLabel lblCount;
    JTextField txtSearch;

    public ViewBook(Connection con, String userId) {

        this.con = con;
        this.userId = userId;

        setTitle("View Books");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // TOP - title + search + count, stacked
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Books Collection", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(92, 64, 51));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        topContainer.add(title);
        topContainer.add(Box.createVerticalStrut(10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblSearch = new JLabel("Search Title/Author:");
        searchPanel.add(lblSearch);

        txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        searchPanel.add(btnSearch);

        JButton btnShowAll = new JButton("Show All");
        searchPanel.add(btnShowAll);

        lblCount = new JLabel("Total Books: 0");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCount.setForeground(new Color(0, 120, 215));
        searchPanel.add(lblCount);

        topContainer.add(searchPanel);
        add(topContainer, BorderLayout.NORTH);

        // TABLE
        model = new DefaultTableModel();
        table = new JTable(model);
        table.setDefaultEditor(Object.class, null);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.setForeground(Color.BLACK);

        model.addColumn("Book_ID");
        model.addColumn("Book_name");
        model.addColumn("Author");
        model.addColumn("Publisher");
        model.addColumn("Edition");
        model.addColumn("Quantity");

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));

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
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM - back button
        JPanel bottomPanel = new JPanel();
        JButton backBtn = new JButton("Back");
        backBtn.setBackground(new Color(220, 53, 69));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true);
        });

        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // ACTIONS
        btnSearch.addActionListener(e -> loadBooks(txtSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadBooks(""); });

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
}