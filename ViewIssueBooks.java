import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ViewIssueBooks extends JFrame {

    JTable table;
    DefaultTableModel model;
    private Connection con;
private String userId;
    public ViewIssueBooks(Connection con, String userId) {

        this.con = con;
this.userId = userId;

        setTitle("View Issued Books");
        setSize(1000, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setResizable(false);
        BackgroundPanel bgPanel = new BackgroundPanel("bgphoto/issue_bg.jpg");
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        // Title
        JLabel title = new JLabel("View Issued Books", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(92, 64, 51));
        bgPanel.add(title, BorderLayout.NORTH);

        // Table model
        model = new DefaultTableModel();
        model.addColumn("Issue ID");
        model.addColumn("User ID");
        model.addColumn("User Name");
        model.addColumn("Book ID");
        model.addColumn("Book Name");
        model.addColumn("Issue Date");
        model.addColumn("Due Date");
        model.addColumn("Status");
        model.addColumn("Return Date");
        model.addColumn("Fine Amount");
        model.addColumn("Fine Status");

        table = new JTable(model);
table.setDefaultEditor(Object.class, null);
        // Table style
        table.setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setRowHeight(28);
        table.setOpaque(false);
table.setBackground(new Color(0, 0, 0, 0));
table.setForeground(Color.BLACK);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(
                new Color(92, 64, 51));
        table.getTableHeader().setForeground(Color.WHITE);

         JScrollPane scrollPane = new JScrollPane(table);
scrollPane.setOpaque(false);
scrollPane.getViewport().setOpaque(false);
scrollPane.setBorder(null);
        bgPanel.add(scrollPane, BorderLayout.CENTER);
        // Bottom panel
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JButton backBtn = new JButton("Back");
        backBtn.setBackground(new Color(220, 53, 69));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true);
        });

        panel.add(backBtn);
        bgPanel.add(panel, BorderLayout.SOUTH);

        loadData();

        setVisible(true);
    }

    private void loadData() {

        model.setRowCount(0);

        try {

            String sql = "SELECT * FROM issue";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                model.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getString("status"),
                        rs.getDate("return_date"),
                        rs.getDouble("fine_amount"),
                        rs.getBoolean("fine_status")
                });
            }

            rs.close();
            pst.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading issued books!");
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