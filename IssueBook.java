import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class IssueBook extends JFrame {

    JTable table;
    DefaultTableModel model;
    private Connection con;
private String userId;
    public IssueBook(Connection con, String userId) {

        this.con = con;
this.userId = userId;

        setTitle("Issue Book");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bgPanel = new BackgroundPanel("bgphoto/issue_bg.jpg");
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        // Title
        JLabel title = new JLabel("Issue Book", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(92, 64, 51));
        bgPanel.add(title, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel();
        model.addColumn("Request ID");
        model.addColumn("User ID");
        model.addColumn("User Name");
        model.addColumn("Book ID");
        model.addColumn("Book Name");
        model.addColumn("Request Date");

        table = new JTable(model);
        table.setDefaultEditor(Object.class, null);
         table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.setOpaque(false);
table.setBackground(new Color(0, 0, 0, 0));
table.setForeground(Color.BLACK);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
scrollPane.setOpaque(false);
scrollPane.getViewport().setOpaque(false);
scrollPane.setBorder(null);
        bgPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JButton issueBtn = new JButton("Issue Book");
        issueBtn.setBackground(new Color(23, 162, 184));
        issueBtn.setForeground(Color.WHITE);
        issueBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton backBtn = new JButton("Back");
        backBtn.setBackground(new Color(220, 53, 69));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        panel.add(issueBtn);
        panel.add(backBtn);

        bgPanel.add(panel, BorderLayout.SOUTH);

        loadData();

        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true);
        });

        issueBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                issueSelectedBook();
            }
        });

        setVisible(true);
    }

    // Load Approved Requests
    private void loadData() {

        model.setRowCount(0);

        try {

            String sql =
                    "SELECT * FROM requests WHERE status='Approved'";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getDate("request_date")
                });
            }

            rs.close();
            pst.close();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex);
        }
    }

    // Issue Logic
    private void issueSelectedBook() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first!");
            return;
        }

        int requestId = (int) model.getValueAt(selectedRow, 0);
        String userId = (String) model.getValueAt(selectedRow, 1);
        String name = (String) model.getValueAt(selectedRow, 2);
        int bookId = (int) model.getValueAt(selectedRow, 3);
        String bookName = (String) model.getValueAt(selectedRow, 4);

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(7);

        try {

            // Insert into issue table
            String insertSql =
                    "INSERT INTO issue (user_id,name,book_id,book_name,issue_date,due_date,status,fine_amount,fine_status) VALUES (?,?,?,?,?,?,?,?,?)";

            PreparedStatement pst1 =
                    con.prepareStatement(insertSql);

            pst1.setString(1, userId);
            pst1.setString(2, name);
            pst1.setInt(3, bookId);
            pst1.setString(4, bookName);
            pst1.setDate(5,
                    java.sql.Date.valueOf(issueDate));
            pst1.setDate(6,
                    java.sql.Date.valueOf(dueDate));
            pst1.setString(7, "Issued");
            pst1.setDouble(8, 0.0);
            pst1.setBoolean(9, false);

            pst1.executeUpdate();

            // Update request status
            String updateSql =
                    "UPDATE requests SET status='Issued' WHERE request_id=?";
            PreparedStatement pst2 =
                    con.prepareStatement(updateSql);

            pst2.setInt(1, requestId);
            pst2.executeUpdate();

            // Reduce quantity
            String updateBook =
                    "UPDATE books SET quantity = quantity - 1 WHERE book_id=?";
            PreparedStatement pst3 =
                    con.prepareStatement(updateBook);

            pst3.setInt(1, bookId);
            pst3.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Book Issued Successfully!");

            loadData();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error issuing book!");
        }
    }

    class BackgroundPanel extends JPanel {
        private Image image;

        public BackgroundPanel(String path) {
            image = new ImageIcon(path).getImage();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0,
                    getWidth(), getHeight(), this);
        }
    }
}