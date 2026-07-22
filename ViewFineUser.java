import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;

public class ViewFineUser extends JFrame {

    private JTable table;
    private JLabel lblTotal;
    private Connection con;
    private String userid;
    private DefaultTableModel model;

    public ViewFineUser(Connection con, String userid) {

        this.con = con;
        this.userid = userid;

        setTitle("My Fine Details");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setResizable(false);
        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // TITLE
        JLabel titleLabel = new JLabel("My Fine Details");
        titleLabel.setBounds(430, 60, 250, 35);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        titleLabel.setForeground(new Color(80, 40, 20));
        bgPanel.add(titleLabel);

        // TABLE MODEL
        model = new DefaultTableModel();

        model.addColumn("Issue ID");
        model.addColumn("Book ID");
        model.addColumn("Book Name");
        model.addColumn("Status");
        model.addColumn("Due Date");
        model.addColumn("Return Date");
        model.addColumn("Fine Amount");
        model.addColumn("Paid Status");

        table = new JTable(model);
table.setDefaultEditor(Object.class, null);
        // TABLE STYLE
        table.setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setRowHeight(30);

        // TRANSPARENT TABLE
        table.setOpaque(false);
        ((DefaultTableCellRenderer)
                table.getDefaultRenderer(Object.class))
                .setOpaque(false);
table.setForeground(Color.BLACK);
table.setSelectionBackground(new Color(245,222,179));
table.setSelectionForeground(new Color(90,50,20));

        // HEADER STYLE
        table.getTableHeader().setFont(
                new Font("Georgia", Font.BOLD, 15));
        table.getTableHeader().setBackground(
                new Color(120, 70, 30));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);

        // INSIDE SCROLL CREAM AREA
        scrollPane.setBounds(100, 140, 890, 330);

        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(120, 70, 30), 2));

        bgPanel.add(scrollPane);

        // TOTAL FINE LABEL
        lblTotal = new JLabel("Total Pending Fine: ₹0");
        lblTotal.setBounds(300, 500, 350, 30);
        lblTotal.setFont(new Font("Georgia", Font.BOLD, 18));
        lblTotal.setForeground(new Color(100, 40, 20));
        bgPanel.add(lblTotal);

        // BACK BUTTON
        JButton btnBack = new JButton("Back");
        btnBack.setBounds(670, 495, 130, 35);
        btnBack.setBackground(new Color(220, 53, 69));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 16));

        btnBack.addActionListener(e -> {
            dispose();
            new UserDashboard(con, userid).setVisible(true);
        });

        bgPanel.add(btnBack);

        loadFineData();

        setVisible(true);
    }

    private void loadFineData() {

        try {

            String sql = "SELECT issue_id, book_id, book_name, status, due_date, return_date, fine_amount, fine_status, " +
                    "IIf(status='Issued' AND Date()>due_date, DateDiff('d', due_date, Date())*5, fine_amount) AS TodayFine " +
                    "FROM issue WHERE user_id=?";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, userid);

            ResultSet rs = pst.executeQuery();

            double totalPending = 0;

            while (rs.next()) {

                double fine = rs.getDouble("TodayFine");
                boolean paid = rs.getBoolean("fine_status");

                if (!paid) {
                    totalPending += fine;
                }

                model.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getString("book_id"),
                        rs.getString("book_name"),
                        rs.getString("status"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date"),
                        "Rs. " + fine,
                        paid ? "Paid" : "Unpaid"
                });
            }

            lblTotal.setText("Total Pending Fine: Rs." + totalPending);

            rs.close();
            pst.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading fine data");
        }
    }

    // BACKGROUND PANEL
    class BackgroundPanel extends JPanel {
        Image bg = new ImageIcon("bgphoto/userfine_bg.jpg").getImage();

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}