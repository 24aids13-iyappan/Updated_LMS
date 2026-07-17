import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;

public class SearchBook extends JFrame {

    JTextField txtSearch;
    JButton btnSearch, backbtn;
    JTable table;
    DefaultTableModel model;
    private String userid;
    private Connection con;

    public SearchBook(Connection con, String userid) {
        this.con = con;
        this.userid = userid;

        setTitle("Search Book");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(null);
        setContentPane(bgPanel);
        
        // SEARCH LABEL
        JLabel lblSearch = new JLabel("Enter Title :");
        lblSearch.setBounds(230, 90, 180, 30);
        lblSearch.setFont(new Font("Georgia", Font.BOLD, 18));
        lblSearch.setForeground(new Color(80, 40, 20));
        bgPanel.add(lblSearch);

        // SEARCH BOX
        txtSearch = new JTextField();
        txtSearch.setBounds(420, 90, 180, 35);
        txtSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtSearch.setBackground(new Color(255, 248, 220));
        txtSearch.setForeground(Color.BLACK);
        txtSearch.setBorder(BorderFactory.createLineBorder(
                new Color(120, 70, 30), 2));
        bgPanel.add(txtSearch);

        // SEARCH BUTTON
        btnSearch = new JButton("Search");
        btnSearch.setBounds(610, 90, 120, 35);
        btnSearch.setBackground(new Color(120, 70, 30));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bgPanel.add(btnSearch);

        // BACK BUTTON
        backbtn = new JButton("Back");
        backbtn.setBounds(740, 90, 100, 35);
        backbtn.setBackground(new Color(180, 50, 50));
        backbtn.setForeground(Color.WHITE);
        backbtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bgPanel.add(backbtn);

        // TABLE MODEL
        model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Publisher");
        model.addColumn("Edition");
        model.addColumn("Quantity");

        table = new JTable(model);
table.setDefaultEditor(Object.class, null);
        // TABLE FONT
        table.setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setRowHeight(30);
table.setForeground(Color.BLACK);


        // FULL TRANSPARENT TABLE
        table.setOpaque(false);
        ((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class))
                .setOpaque(false);

        // HEADER STYLE
        table.getTableHeader().setFont(
                new Font("Georgia", Font.BOLD, 16));
        table.getTableHeader().setBackground(
                new Color(120, 70, 30));
        table.getTableHeader().setForeground(Color.WHITE);

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


        JScrollPane scrollPane = new JScrollPane(table);

        // SCROLL INSIDE CREAM CENTER
        scrollPane.setBounds(230, 160, 640, 360);

        // TRANSPARENT SCROLL
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(120, 70, 30), 2));

        bgPanel.add(scrollPane);

        // BUTTON ACTIONS
        btnSearch.addActionListener(e -> searchBooks());

        backbtn.addActionListener(e -> {
            dispose();
            new UserDashboard(con, userid).setVisible(true);
        });

        setVisible(true);
    }

    private void searchBooks() {
        try {
            model.setRowCount(0);

            Connection con = DBConnection.getConnection();

            String keyword = txtSearch.getText();

            String query =
                    "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?";

            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");

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
                    "Error while searching books!");
        }
    }

    // BACKGROUND PANEL
    class BackgroundPanel extends JPanel {
        Image bg = new ImageIcon("bgphoto/searchbook_bg.jpg").getImage();

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}