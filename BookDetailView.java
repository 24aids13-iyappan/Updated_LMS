import javax.swing.*;
import java.awt.*;
import java.sql.*;

/*
 * BookDetailView - Book oda image + full detail kaata (popup style).
 *
 * DB CHANGE NEEDED FIRST:
 *   ALTER TABLE books ADD COLUMN image_path VARCHAR(255);
 *   ALTER TABLE books ADD COLUMN description MEMO;   -- (Access) or TEXT in MySQL
 *
 * Then for each book row, store an image path like "bgphoto/books/harrypotter.jpg"
 *
 * WIRING into SearchBook.java / ViewBook.java:
 *   Add a MouseListener on the table so double-click on a row opens this popup:
 *
 *   table.addMouseListener(new java.awt.event.MouseAdapter() {
 *       public void mouseClicked(java.awt.event.MouseEvent e) {
 *           if (e.getClickCount() == 2) {
 *               int row = table.getSelectedRow();
 *               if (row != -1) {
 *                   int bookId = (int) model.getValueAt(row, 0);
 *                   new BookDetailView(con, bookId).setVisible(true);
 *               }
 *           }
 *       }
 *   });
 */
public class BookDetailView extends JDialog {

    private Connection con;
    private int bookId;

    public BookDetailView(Connection con, int bookId) {
        this.con = con;
        this.bookId = bookId;

        setTitle("Book Details");
        setSize(500, 550);
        setLocationRelativeTo(null);
        setModal(true);
setResizable(false);
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(main);

        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(460, 280));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        main.add(imageLabel, BorderLayout.NORTH);

        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        main.add(new JScrollPane(detailArea), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottom = new JPanel();
        bottom.add(closeBtn);
        main.add(bottom, BorderLayout.SOUTH);

        loadBookDetail(imageLabel, detailArea);
    }

    private void loadBookDetail(JLabel imageLabel, JTextArea detailArea) {
        try {
            String sql = "SELECT * FROM books WHERE book_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, bookId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                String title = rs.getString("title");
                String author = rs.getString("author");
                String publisher = rs.getString("publisher");
                String edition = rs.getString("edition");
                String qty = rs.getString("quantity");

                // image_path column - fallback to placeholder if missing/empty
                String imgPath = null;
                try { imgPath = rs.getString("image_path"); } catch (Exception ignore) {}

                String description = null;
                try { description = rs.getString("description"); } catch (Exception ignore) {}

                if (imgPath != null && !imgPath.trim().isEmpty()) {
                    ImageIcon icon = new ImageIcon(imgPath);
                    Image scaled = icon.getImage().getScaledInstance(300, 280, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaled));
                } else {
                    imageLabel.setText("No cover image available");
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Title: ").append(title).append("\n");
                sb.append("Author: ").append(author).append("\n");
                sb.append("Publisher: ").append(publisher).append("\n");
                sb.append("Edition: ").append(edition).append("\n");
                sb.append("Available Copies: ").append(qty).append("\n\n");

                if (description != null && !description.trim().isEmpty()) {
                    sb.append("Description:\n").append(description);
                } else {
                    sb.append("Description: Not added yet.");
                }

                detailArea.setText(sb.toString());
            }

            rs.close();
            pst.close();

        } catch (Exception ex) {
            detailArea.setText("Error loading book details: " + ex.getMessage());
        }
    }
}
