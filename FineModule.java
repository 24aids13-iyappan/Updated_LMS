import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FineModule extends JFrame {

    private JTextField txtIssueId;
    private JLabel lblFine;
    private JButton btnCheck, btnPay;
    private Connection con;
    private double calculatedFine = 0;
private String userId;
    public FineModule(Connection con, String userId) {
this.userId = userId;

        this.con = con;

        setTitle("Fine Module");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setResizable(false);
        Color brown = new Color(92, 64, 51);
        Color paper = new Color(245, 238, 220);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 18);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font titleFont = new Font("Segoe UI", Font.BOLD, 26);

        BackgroundPanel bgPanel = new BackgroundPanel("bgphoto/fine_bg.jpg");
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // Title
        JLabel title = new JLabel("Fine Module");
        title.setFont(titleFont);
        title.setForeground(brown);
        title.setBounds(210, 40, 200, 35);
        bgPanel.add(title);

        // Issue ID label
        JLabel lblIssue = new JLabel("Issue ID:");
        lblIssue.setBounds(80, 130, 120, 30);
        lblIssue.setFont(labelFont);
        lblIssue.setForeground(brown);
        bgPanel.add(lblIssue);

        // Text field
        txtIssueId = new JTextField();
        txtIssueId.setBounds(220, 130, 220, 35);
        txtIssueId.setFont(fieldFont);
        txtIssueId.setBackground(paper);
        txtIssueId.setForeground(Color.BLACK);
        bgPanel.add(txtIssueId);

        // Check button
        btnCheck = new JButton("Check Fine");
        btnCheck.setBounds(80, 220, 150, 40);
        btnCheck.setBackground(new Color(0, 120, 215));
        btnCheck.setForeground(Color.WHITE);
        btnCheck.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCheck.setFocusPainted(false);
        bgPanel.add(btnCheck);

        // Pay button
        btnPay = new JButton("Pay Fine");
        btnPay.setBounds(270, 220, 150, 40);
        btnPay.setBackground(new Color(40, 167, 69));
        btnPay.setForeground(Color.WHITE);
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnPay.setFocusPainted(false);
        bgPanel.add(btnPay);

        // Back button
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(450, 220, 100, 40);
        backBtn.setBackground(new Color(220, 53, 69));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        backBtn.setFocusPainted(false);
        bgPanel.add(backBtn);

        // Fine label
        lblFine = new JLabel("Fine: Rs. 0");
        lblFine.setBounds(220, 320, 200, 30);
        lblFine.setFont(labelFont);
        lblFine.setForeground(brown);
        bgPanel.add(lblFine);

        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true);
        });

        btnCheck.addActionListener(e -> checkFine());
        btnPay.addActionListener(e -> payFine());

        setVisible(true);
    }

    private Connection getConnection() throws Exception {
        String url = "jdbc:ucanaccess://C:/JAVA_SPARKS/library.accdb";
        return DriverManager.getConnection(url);
    }

  private void checkFine() {

    try {

        String issueText = txtIssueId.getText().trim();

        if (issueText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter Issue ID!");
            return;
        }

        int issueId = Integer.parseInt(issueText);

        String sql =
                "SELECT due_date, return_date, status, fine_status, fine_amount " +
                "FROM issue WHERE issue_id=?";

        PreparedStatement pst =
                this.con.prepareStatement(sql);

        pst.setInt(1, issueId);

        ResultSet rs = pst.executeQuery();

        if (!rs.next()) {
            JOptionPane.showMessageDialog(this,
                    "Invalid Issue ID!");
            return;
        }

        String status = rs.getString("status");
        boolean finePaid = rs.getBoolean("fine_status");
        double existingFine = rs.getDouble("fine_amount");

        if (!status.equalsIgnoreCase("Returned")) {
            JOptionPane.showMessageDialog(this,
                    "Book not returned yet!");
            return;
        }

        // 🔥 Important fix
        if (finePaid) {
            lblFine.setText("Fine: Rs.0");
            JOptionPane.showMessageDialog(this,
                    "Fine already paid successfully!");
            return;
        }

        java.sql.Date dueSql = rs.getDate("due_date");
        java.sql.Date returnSql = rs.getDate("return_date");

        if (dueSql == null || returnSql == null) {
            JOptionPane.showMessageDialog(this,
                    "Return date missing!");
            return;
        }

        LocalDate dueDate = dueSql.toLocalDate();
        LocalDate returnDate = returnSql.toLocalDate();

        if (returnDate.isAfter(dueDate)) {

            long daysLate =
                    ChronoUnit.DAYS.between(dueDate, returnDate);

            calculatedFine = daysLate * 5;

        } else {
            calculatedFine = 0;
        }

        lblFine.setText("Fine: Rs." + calculatedFine);

        JOptionPane.showMessageDialog(this,
                "Fine Amount: Rs." + calculatedFine);

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(this,
                "Unable to check fine. Please try again.");
    }
}
    private void payFine() {

    try {

        int issueId = Integer.parseInt(txtIssueId.getText().trim());

        String sql1 =
                "SELECT fine_amount, fine_status FROM issue WHERE issue_id=?";

        PreparedStatement pst1 = con.prepareStatement(sql1);
        pst1.setInt(1, issueId);

        ResultSet rs = pst1.executeQuery();

        if (!rs.next()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid Issue ID!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        double fine = rs.getDouble("fine_amount");
        boolean fineStatus = rs.getBoolean("fine_status");

        if (fine == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "No fine to pay!",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if (fineStatus) {
            JOptionPane.showMessageDialog(
                    this,
                    "Fine already paid!",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String sql =
                "UPDATE issue SET fine_status=? WHERE issue_id=?";

        PreparedStatement pst2 = con.prepareStatement(sql);

        pst2.setBoolean(1, true);
        pst2.setInt(2, issueId);

        pst2.executeUpdate();

        JOptionPane.showMessageDialog(
                this,
                "Fine paid successfully!\nAmount: Rs." + fine,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );

        lblFine.setText("Fine: Rs.0");
        txtIssueId.setText("");

    } catch (NumberFormatException ex) {

        JOptionPane.showMessageDialog(
                this,
                "Please enter numbers only for Issue ID!",
                "Input Error",
                JOptionPane.ERROR_MESSAGE
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                "Payment failed. Please try again!",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
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