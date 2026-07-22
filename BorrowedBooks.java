import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;

public class BorrowedBooks extends JFrame {

    private JTable table;
    private Connection con;
    private String userId;
    private DefaultTableModel model;

    public BorrowedBooks(Connection con, String userId) {

        this.con = con;
        this.userId = userId;

        setTitle("My Borrowed Books");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setResizable(false);

        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // TITLE
        JLabel titleLabel = new JLabel("My Borrowed Books");
        titleLabel.setBounds(410, 60, 300, 35);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        titleLabel.setForeground(new Color(80, 40, 20));
        bgPanel.add(titleLabel);

        // TABLE MODEL
        model = new DefaultTableModel();

        model.addColumn("Issue ID");
        model.addColumn("Book ID");
        model.addColumn("Book Name");
        model.addColumn("Issue Date");
        model.addColumn("Due Date");
        model.addColumn("Status");

        table = new JTable(model);
table.setDefaultEditor(Object.class, null);
        // TABLE STYLE
        table.setFont(new Font("Segoe UI", Font.BOLD, 15));
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
                new Font("Georgia", Font.BOLD, 16));
        table.getTableHeader().setBackground(
                new Color(120, 70, 30));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);

        // PERFECT CENTER SCROLL POSITION
        scrollPane.setBounds(120, 140, 800, 360);

        // TRANSPARENT SCROLL
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(120, 70, 30), 2));

        bgPanel.add(scrollPane);

        // BACK BUTTON
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(470, 540, 140, 35);
        backBtn.setBackground(new Color(220, 53, 69));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));

        backBtn.addActionListener(e -> {
            dispose();
            new UserDashboard(con, userId).setVisible(true);
        });

        bgPanel.add(backBtn);

        loadBooks();

        setVisible(true);
    }

    private void loadBooks() {

        try {

            String sql = "SELECT issue_id, book_id, book_name, issue_date, due_date, status " +
                         "FROM issue WHERE user_id=?";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, userId);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                model.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getString("status")
                });
            }

            rs.close();
            pst.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading borrowed books");
        }
    }

    // BACKGROUND IMAGE PANEL
    class BackgroundPanel extends JPanel {
        Image bg = new ImageIcon("bgphoto/barrow_bg.jpg").getImage();

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}