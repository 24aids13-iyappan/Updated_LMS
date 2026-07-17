import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AddBook extends JFrame implements ActionListener {

    private Connection con;
private String userId;
    JTextField txtTitle, txtAuthor, txtPublisher, txtEdition, txtQuantity;
    JButton btnAdd, backBtn;

    public AddBook(Connection con, String userId) {

        this.con = con;
this.userId = userId;

        setTitle("Add Book");
        setSize(650, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Color brown = new Color(92, 64, 51);
        Color paper = new Color(245, 238, 220);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 18);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font titleFont = new Font("Segoe UI", Font.BOLD, 26);

        BackgroundPanel bgPanel = new BackgroundPanel("bgphoto/addbook_bg.jpg");
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // Title
        JLabel title = new JLabel("Add New Book");
        title.setFont(titleFont);
        title.setForeground(brown);
        title.setBounds(250, 40, 200, 35);
        bgPanel.add(title);

        // Labels
        JLabel lblTitle = new JLabel("Title:");
        lblTitle.setFont(labelFont);
        lblTitle.setForeground(brown);
        lblTitle.setBounds(80, 130, 120, 30);
        bgPanel.add(lblTitle);

        JLabel lblAuthor = new JLabel("Author:");
        lblAuthor.setFont(labelFont);
        lblAuthor.setForeground(brown);
        lblAuthor.setBounds(80, 190, 120, 30);
        bgPanel.add(lblAuthor);

        JLabel lblPublisher = new JLabel("Publisher:");
        lblPublisher.setFont(labelFont);
        lblPublisher.setForeground(brown);
        lblPublisher.setBounds(80, 250, 120, 30);
        bgPanel.add(lblPublisher);

        JLabel lblEdition = new JLabel("Edition:");
        lblEdition.setFont(labelFont);
        lblEdition.setForeground(brown);
        lblEdition.setBounds(80, 310, 120, 30);
        bgPanel.add(lblEdition);

        JLabel lblQuantity = new JLabel("Quantity:");
        lblQuantity.setFont(labelFont);
        lblQuantity.setForeground(brown);
        lblQuantity.setBounds(80, 370, 120, 30);
        bgPanel.add(lblQuantity);

        // Text Fields
        txtTitle = new JTextField();
        txtTitle.setBounds(220, 130, 300, 35);
        txtTitle.setFont(fieldFont);
        txtTitle.setBackground(paper);
        txtTitle.setForeground(Color.BLACK);
        bgPanel.add(txtTitle);

        txtAuthor = new JTextField();
        txtAuthor.setBounds(220, 190, 300, 35);
        txtAuthor.setFont(fieldFont);
        txtAuthor.setBackground(paper);
        txtAuthor.setForeground(Color.BLACK);
        bgPanel.add(txtAuthor);

        txtPublisher = new JTextField();
        txtPublisher.setBounds(220, 250, 300, 35);
        txtPublisher.setFont(fieldFont);
        txtPublisher.setBackground(paper);
        txtPublisher.setForeground(Color.BLACK);
        bgPanel.add(txtPublisher);

        txtEdition = new JTextField();
        txtEdition.setBounds(220, 310, 300, 35);
        txtEdition.setFont(fieldFont);
        txtEdition.setBackground(paper);
        txtEdition.setForeground(Color.BLACK);
        bgPanel.add(txtEdition);

        txtQuantity = new JTextField();
        txtQuantity.setBounds(220, 370, 300, 35);
        txtQuantity.setFont(fieldFont);
        txtQuantity.setBackground(paper);
        txtQuantity.setForeground(Color.BLACK);
        bgPanel.add(txtQuantity);

        // Buttons
        btnAdd = new JButton("Add Book");
        btnAdd.setBounds(170, 500, 140, 40);
        btnAdd.setBackground(new Color(40, 167, 69));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnAdd.setFocusPainted(false);
        bgPanel.add(btnAdd);

        backBtn = new JButton("Back");
        backBtn.setBounds(340, 500, 140, 40);
        backBtn.setBackground(new Color(220, 53, 69));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        backBtn.setFocusPainted(false);
        bgPanel.add(backBtn);

        btnAdd.addActionListener(this);

        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                new AdminDashboard(con, userId).setVisible(true);
            }
        });

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        try {

            String sql = "INSERT INTO books (title,author,publisher,edition,quantity) VALUES (?,?,?,?,?)";

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, txtTitle.getText());
            pst.setString(2, txtAuthor.getText());
            pst.setString(3, txtPublisher.getText());
            pst.setString(4, txtEdition.getText());
            pst.setInt(5, Integer.parseInt(txtQuantity.getText()));

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Book Added Successfully!");

            clearFields();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex);
        }
    }

    private void clearFields() {
        txtTitle.setText("");
        txtAuthor.setText("");
        txtPublisher.setText("");
        txtEdition.setText("");
        txtQuantity.setText("");
    }

    class BackgroundPanel extends JPanel {
        private Image image;

        public BackgroundPanel(String path) {
            image = new ImageIcon(path).getImage();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }
}