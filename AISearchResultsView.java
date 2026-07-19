import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;

/*
 * AISearchResultsView - User Dashboard la Google-maadhiri search box la
 * type panna, AI matched books ah idhu la kaatum.
 */
public class AISearchResultsView extends JFrame {

    private Connection con;
    private String userId;

    public AISearchResultsView(Connection con, String userId, String query, List<Integer> matchedIds) {

        this.con = con;
        this.userId = userId;

        setTitle("AI Search Results");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(main);

        JLabel title = new JLabel("Results for: \"" + query + "\"");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(92, 64, 51));
        main.add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Title", "Author", "Publisher", "Edition", "Quantity"}, 0);
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setDefaultEditor(Object.class, null);

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

        main.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel status;
        if (matchedIds.isEmpty()) {
            status = new JLabel("No good matches found. Try different words!");
            status.setForeground(Color.RED);
        } else {
            try {
                for (int id : matchedIds) {
                    PreparedStatement pst = con.prepareStatement("SELECT * FROM books WHERE book_id=?");
                    pst.setInt(1, id);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("book_id"), rs.getString("title"), rs.getString("author"),
                                rs.getString("publisher"), rs.getString("edition"), rs.getString("quantity")
                        });
                    }
                    rs.close();
                    pst.close();
                }
            } catch (Exception ex) {
                // ignore, table stays partially filled
            }
            status = new JLabel(matchedIds.size() + " book(s) found");
            status.setForeground(new Color(40, 167, 69));
        }
        status.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.addActionListener(e -> {
            dispose();
            new UserDashboard(con, userId).setVisible(true);
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(status, BorderLayout.WEST);
        bottom.add(backBtn, BorderLayout.EAST);
        main.add(bottom, BorderLayout.SOUTH);

        setVisible(true);
    }
}
