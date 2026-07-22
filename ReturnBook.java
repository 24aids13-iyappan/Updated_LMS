import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReturnBook extends JFrame {

    private JTextField txtIssueId;
    private JLabel lblFine;
    private JButton btnReturn;
    private Connection con;
private String userId;

    public ReturnBook(Connection con, String userId) {

        this.con = con;
this.userId = userId;

        setTitle("Return Book");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setResizable(false);

        Color brown = new Color(255,223,100);
        Color paper = new Color(245, 238, 220);


        Font labelFont = new Font("Segoe UI", Font.BOLD, 18);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font titleFont = new Font("Segoe UI", Font.BOLD, 26);

        BackgroundPanel bgPanel = new BackgroundPanel("bgphoto/Returnbook_bg.jpg");
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // Title
        JLabel title = new JLabel("Return Book");
        title.setFont(titleFont);
        title.setForeground(paper);
        title.setBounds(210, 40, 200, 35);
        bgPanel.add(title);

        // Issue ID label
        JLabel lblIssue = new JLabel("Issue ID:");
        lblIssue.setFont(labelFont);
        lblIssue.setForeground(brown);
        lblIssue.setBounds(180, 140, 120, 30);
        bgPanel.add(lblIssue);

        // Text field
        txtIssueId = new JTextField();
        txtIssueId.setBounds(310, 140, 220, 35);
        txtIssueId.setFont(fieldFont);
        txtIssueId.setBackground(paper);
        txtIssueId.setForeground(Color.BLACK);
        bgPanel.add(txtIssueId);

        // Return button
        btnReturn = new JButton("Return Book");
        btnReturn.setBounds(180, 240, 150, 40);
        btnReturn.setBackground(new Color(23, 162, 184));
        btnReturn.setForeground(Color.WHITE);
        btnReturn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnReturn.setFocusPainted(false);
        bgPanel.add(btnReturn);

        // Back button
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(380, 240, 150, 40);
        backBtn.setBackground(new Color(220, 53, 69));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        backBtn.setFocusPainted(false);
        bgPanel.add(backBtn);

        // Fine label
        lblFine = new JLabel("Fine: Rs.0");
        lblFine.setFont(labelFont);
        lblFine.setForeground(brown);
        lblFine.setBounds(270, 330, 200, 30);
        bgPanel.add(lblFine);

        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true);
        });

        btnReturn.addActionListener(e -> returnBook());

        setVisible(true);
    }

    private void returnBook() {

        try {

            int issueId = Integer.parseInt(txtIssueId.getText());

            String selectSql =
                    "SELECT due_date, book_id FROM issue WHERE issue_id=? AND status='Issued'";

            PreparedStatement pst1 =
                    con.prepareStatement(selectSql);

            pst1.setInt(1, issueId);
            ResultSet rs = pst1.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Issue ID or Already Returned");
                return;
            }

            LocalDate dueDate =
                    rs.getDate("due_date").toLocalDate();

            int bookId = rs.getInt("book_id");

            LocalDate today = LocalDate.now();
            double fine = 0;

            // Fine calculation
            if (today.isAfter(dueDate)) {
                long daysLate =
                        ChronoUnit.DAYS.between(dueDate, today);
                fine = daysLate * 5;
            }

            // Update issue table
            String updateSql =
                    "UPDATE issue SET status=?, return_date=?, fine_amount=? WHERE issue_id=?";

            PreparedStatement pst2 =
                    con.prepareStatement(updateSql);

            pst2.setString(1, "Returned");
            pst2.setDate(2,
                    java.sql.Date.valueOf(today));
            pst2.setDouble(3, fine);
            pst2.setInt(4, issueId);
            pst2.executeUpdate();

            // Increase stock
            String stockSql =
                    "UPDATE books SET quantity = quantity + 1 WHERE book_id=?";

            PreparedStatement pst3 =
                    con.prepareStatement(stockSql);

            pst3.setInt(1, bookId);
            pst3.executeUpdate();

            lblFine.setText("Fine: Rs." + fine);

            JOptionPane.showMessageDialog(this,
                    "Book Returned Successfully!");

            txtIssueId.setText("");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error Returning Book");
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