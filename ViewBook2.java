import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;

public class ViewBook2 extends JFrame {

    private Connection con;
    private String userid;

    JTable table;
    DefaultTableModel model;

    public ViewBook2(Connection con, String userid) {

        this.con = con;
        this.userid = userid;

        setTitle("View Books");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // TITLE
        JLabel titleLabel = new JLabel("View Books");
        titleLabel.setBounds(460, 80, 200, 35);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        titleLabel.setForeground(new Color(80, 40, 20));
        bgPanel.add(titleLabel);

        // TABLE MODEL
        model = new DefaultTableModel();

        model.addColumn("Book ID");
        model.addColumn("Book Name");
        model.addColumn("Author");
        model.addColumn("Publisher");
        model.addColumn("Edition");
        model.addColumn("Quantity");

        table = new JTable(model);
        table.setDefaultEditor(Object.class, null);
 table.addMouseListener(new java.awt.event.MouseAdapter() {
    public void mouseClicked(java.awt.event.MouseEvent e) {
        System.out.println("Clicked! Count = " + e.getClickCount());
        if (e.getClickCount() == 2) {
            int row = table.getSelectedRow();
            System.out.println("Row = " + row); 
            if (row != -1) {
                int bookId = (int) model.getValueAt(row, 0);
                new BookDetailView(con, bookId).setVisible(true);
            }
        }
    }
});
        // TABLE STYLE
        table.setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.setRowHeight(30);

        // FULL TRANSPARENT TABLE
        table.setOpaque(false);
        ((DefaultTableCellRenderer)
                table.getDefaultRenderer(Object.class))
                .setOpaque(false);
table.setForeground(Color.BLACK);
        // HEADER STYLE
        table.getTableHeader().setFont(
                new Font("Georgia", Font.BOLD, 16));
        table.getTableHeader().setBackground(
                new Color(120, 70, 30));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);

        // PERFECT SCROLL CENTER POSITION
        scrollPane.setBounds(160, 140, 800, 360);

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
            new UserDashboard(con, userid).setVisible(true);
        });

        bgPanel.add(backBtn);

        loadBooks();

        setVisible(true);
    }

    private void loadBooks() {
        try {
            Connection con = DBConnection.getConnection();

            String query = "SELECT * FROM books";
            PreparedStatement pst = con.prepareStatement(query);
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
            }

            con.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading books!");
        }
    }

    // BACKGROUND IMAGE PANEL
    class BackgroundPanel extends JPanel {
        Image bg = new ImageIcon("bgphoto/viewbooks_bg.jpg").getImage();

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}