import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RequestBook extends JFrame {

    private String userId;
    JTextField txtBookId;
    JButton btnRequest, backBtn;
    private Connection con;

    public RequestBook(Connection con, String userId) {

        this.con = con;
        this.userId = userId;

        setTitle("Request Book");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
setResizable(false);
        // ===== Background Image Panel =====
        JPanel bgPanel = new JPanel() {
            Image bg = new ImageIcon("bgphoto/requstbook_bg.jpg").getImage();

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };

        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // ===== Label =====
        JLabel lblBookId = new JLabel("Enter Book ID:");
        lblBookId.setBounds(240, 160, 150, 30);
        lblBookId.setFont(new Font("Serif", Font.BOLD, 20));
        lblBookId.setForeground(new Color(255,215,0));
        bgPanel.add(lblBookId);

        // ===== Text Field =====
        txtBookId = new JTextField();
        txtBookId.setBounds(400, 160, 200, 35);
        txtBookId.setFont(new Font("Arial", Font.BOLD, 16));
        txtBookId.setBackground(Color.WHITE);
        txtBookId.setForeground(Color.BLACK);
        bgPanel.add(txtBookId);

        // ===== Request Button =====
        btnRequest = new JButton("Request Book");
        btnRequest.setBounds(240, 240, 150, 40);
        btnRequest.setBackground(new Color(40, 167, 69));
        btnRequest.setForeground(Color.WHITE);
        btnRequest.setFont(new Font("Arial", Font.BOLD, 15));
        bgPanel.add(btnRequest);

        // ===== Back Button =====
        backBtn = new JButton("Back");
        backBtn.setBounds(460, 240, 100, 40);
        backBtn.setBackground(new Color(220, 53, 69));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 15));
        bgPanel.add(backBtn);

        btnRequest.addActionListener(e -> requestBook());

        backBtn.addActionListener(e -> {
            dispose();
            new UserDashboard(con, userId).setVisible(true);
        });

        setVisible(true);
    }

    private void requestBook() {

        try {

            String bookIdText = txtBookId.getText();

            if (bookIdText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Book ID");
                return;
            }

            int bookId = Integer.parseInt(bookIdText);

            Connection con = DBConnection.getConnection();

            String bookName = "";
            String bookQuery = "SELECT title FROM books WHERE book_id = ?";
            PreparedStatement pst1 = con.prepareStatement(bookQuery);
            pst1.setInt(1, bookId);
            ResultSet rs = pst1.executeQuery();

            if (rs.next()) {
                bookName = rs.getString("title");
            } else {
                JOptionPane.showMessageDialog(this, "Book not found!");
                return;
            }

            String Name = "";
            String NQuery = "SELECT name FROM users WHERE user_id = ?";
            PreparedStatement pst2 = con.prepareStatement(NQuery);
            pst2.setString(1, userId);
            ResultSet rs1 = pst2.executeQuery();

            if (rs1.next()) {
                Name = rs1.getString("name");
            } else {
                JOptionPane.showMessageDialog(this, "User not found!");
                return;
            }

            String query =
                    "INSERT INTO requests(user_id, name, book_id, book_name, request_date, status) " +
                    "VALUES (?, ?, ?, ?, CURDATE(), 'Pending')";

            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, userId);
            pst.setString(2, Name);
            pst.setInt(3, bookId);
            pst.setString(4, bookName);

            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Book Requested Successfully!");
                txtBookId.setText("");
            }

            con.close();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Book ID must be number!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}