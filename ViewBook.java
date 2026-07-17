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
    public ViewBook(Connection con, String userId) {

        this.con = con;
this.userId = userId;

        setTitle("View Books");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

JLabel title = new JLabel("Books Collection", SwingConstants.CENTER);
title.setFont(new Font("Segoe UI", Font.BOLD, 24));
title.setForeground(new Color(92,64,51));


    lblCount = new JLabel("Total Books: 0", SwingConstants.CENTER);
lblCount.setFont(new Font("Segoe UI", Font.BOLD, 16));
lblCount.setForeground(new Color(0, 120, 215));

JPanel topPanel = new JPanel(new BorderLayout());
topPanel.add(title, BorderLayout.NORTH);
topPanel.add(lblCount, BorderLayout.SOUTH);
add(topPanel, BorderLayout.NORTH);  

        BackgroundPanel bgPanel = new BackgroundPanel("bgphoto/ViewBook_bg.jpg");
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        // Table model
        model = new DefaultTableModel();
        table = new JTable(model);
        table.setDefaultEditor(Object.class, null);
table.setOpaque(false);
table.setBackground(new Color(0, 0, 0, 0));
table.setForeground(Color.BLACK);

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

((DefaultTableCellRenderer)table.getDefaultRenderer(Object.class))
        .setOpaque(false);

        model.addColumn("Book_ID");
        model.addColumn("Book_name");
        model.addColumn("Author");
        model.addColumn("Publisher");
        model.addColumn("Edition");
        model.addColumn("Quantity");

        // Table style
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 15));

               /* JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(
                new Color(245, 238, 220));*/

JScrollPane scrollPane = new JScrollPane(table);

scrollPane.setOpaque(false);
scrollPane.getViewport().setOpaque(false);
scrollPane.setBorder(null);

add(scrollPane, BorderLayout.CENTER);

        // Bottom button panel
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

        loadBooks();
        

       // add(scrollPane, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadBooks() {
        try {

            String query = "SELECT * FROM books";
            PreparedStatement pst =
                    con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
int count = 0;
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