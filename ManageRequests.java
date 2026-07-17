import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageRequests extends JFrame {

    JTable table;
    DefaultTableModel model;
    private Connection con;
private String userId;
    public ManageRequests(Connection con, String userId) {

        this.con = con;
this.userId = userId;

        setTitle("Manage Requests");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bgPanel = new BackgroundPanel("bgphoto/ManageRq_bg.jpg");
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        // Title
        JLabel title = new JLabel("Manage Requests", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(92, 64, 51));
        bgPanel.add(title, BorderLayout.NORTH);

        // Table model
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
                "Request ID", "User ID", "User Name",
                "Book ID", "Book Name", "Date", "Status"
        });

        table = new JTable(model);
table.setDefaultEditor(Object.class, null);
        // Table style
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

        // Button panel
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JButton btnApprove = new JButton("Approve");
        btnApprove.setBackground(new Color(23, 162, 184));
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnReject = new JButton("Reject");
        btnReject.setBackground(new Color(220, 53, 69));
        btnReject.setForeground(Color.WHITE);
        btnReject.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton backBtn = new JButton("Back");
        backBtn.setBackground(new Color(108, 117, 125));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        panel.add(btnApprove);
        panel.add(btnReject);
        panel.add(backBtn);

        bgPanel.add(panel, BorderLayout.SOUTH);

        btnApprove.addActionListener(e -> approveRequest());
        btnReject.addActionListener(e -> rejectRequest());

        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true);
        });

        loadRequests();

        setVisible(true);
    }

    private void loadRequests() {

        try {

            String query = "SELECT * FROM requests";
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            model.setRowCount(0);

            while (rs.next()) {

                model.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getDate("request_date"),
                        rs.getString("status")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    private void approveRequest() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row!");
            return;
        }

        String status = (String) model.getValueAt(selectedRow, 6);

        if (status.equalsIgnoreCase("Issued") ||
            status.equalsIgnoreCase("Approved") ||
            status.equalsIgnoreCase("Rejected")) {

            JOptionPane.showMessageDialog(this,
                    "Already processed: " + status);
            return;
        }

        int requestId = (int) model.getValueAt(selectedRow, 0);
        int bookId = (int) model.getValueAt(selectedRow, 3);

        try {

            String updateRequest =
                    "UPDATE requests SET status='Approved' WHERE request_id=?";
            PreparedStatement pst1 = con.prepareStatement(updateRequest);
            pst1.setInt(1, requestId);
            pst1.executeUpdate();


            loadRequests();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    private void rejectRequest() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row!");
            return;
        }

        String status = (String) model.getValueAt(selectedRow, 6);

        if (status.equalsIgnoreCase("Issued") ||
            status.equalsIgnoreCase("Approved") ||
            status.equalsIgnoreCase("Rejected")) {

            JOptionPane.showMessageDialog(this,
                    "Already processed: " + status);
            return;
        }

        int requestId = (int) model.getValueAt(selectedRow, 0);

        try {

            String query =
                    "UPDATE requests SET status='Rejected' WHERE request_id=?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, requestId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Rejected Successfully");

            loadRequests();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
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