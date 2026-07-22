import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;

public class LoginGUI extends JFrame implements ActionListener {

    JTextField txtUsername;
    JPasswordField txtPassword;
    JButton btnLogin, btnsignup, btnForgot;

    private Connection con;

    public LoginGUI(Connection con) {

        this.con = con;

        setTitle("Library Management - Login");
        setSize(800,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setResizable(false);
        // 🔥 Background Image
        ImageIcon bgIcon = new ImageIcon("bgphoto/Login_bg.png");
        Image img = bgIcon.getImage().getScaledInstance(800,500,Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(img));
        background.setLayout(null);   // IMPORTANT

        setContentPane(background);   // set as main panel

        // 🔥 Transparent Login Panel
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(250, 80, 300, 300);

        // transparency
        panel.setBackground(new Color(0,0,0,150)); // semi transparent

        background.add(panel);

        // Title
        JLabel lblTitle = new JLabel("LOGIN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(100, 20, 150, 30);
        panel.add(lblTitle);

        // Username
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setBounds(30, 80, 100, 25);
        panel.add(lblUsername);

        txtUsername = new JTextField();
        txtUsername.setBounds(130, 80, 140, 25);

        // transparent textfield
        txtUsername.setOpaque(false);
        txtUsername.setForeground(Color.WHITE);
        txtUsername.setCaretColor(Color.WHITE);
        txtUsername.setBorder(BorderFactory.createLineBorder(Color.WHITE));

        panel.add(txtUsername);

        // Password
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setBounds(30, 120, 100, 25);
        panel.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(130, 120, 140, 25);

        txtPassword.setOpaque(false);
        txtPassword.setForeground(Color.WHITE);
        txtPassword.setCaretColor(Color.WHITE);
        txtPassword.setBorder(BorderFactory.createLineBorder(Color.WHITE));

        panel.add(txtPassword);

        // Login Button
        ImageIcon btnLoginIcon = new ImageIcon("bgphoto/New folder/icon/btnLogin_icon.png");

// size adjust panna
Image img3 = btnLoginIcon.getImage().getScaledInstance(20,20,Image.SCALE_SMOOTH);
btnLoginIcon = new ImageIcon(img3);

btnLogin = new JButton("Login", btnLoginIcon);

// text + icon alignment
btnLogin.setHorizontalTextPosition(SwingConstants.RIGHT);
btnLogin.setIconTextGap(10);

        btnLogin.setBounds(20, 180, 120, 35);
        btnLogin.setBackground(new Color(0,120,215));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(this);
        panel.add(btnLogin);

        // Signup Button         
ImageIcon signupIcon = new ImageIcon("bgphoto/New folder/icon/signup_icon2.jpg");

// size adjust panna
Image img2 = signupIcon.getImage().getScaledInstance(20,20,Image.SCALE_SMOOTH);
signupIcon = new ImageIcon(img2);

btnsignup = new JButton("Sign Up", signupIcon);

// text + icon alignment
btnsignup.setHorizontalTextPosition(SwingConstants.RIGHT);
btnsignup.setIconTextGap(10);
        btnsignup.setBounds(160, 180, 120, 35);
        btnsignup.setBackground(new Color(40,167,69));
        btnsignup.setForeground(Color.WHITE);
        btnsignup.setFocusPainted(false);
        btnsignup.addActionListener(e -> {
            dispose();
            new SignUpGUI(con).setVisible(true);});
        panel.add(btnsignup);

        // Forgot Button
        btnForgot = new JButton("Forgot");
        btnForgot.setBounds(90, 230, 120, 30);
        btnForgot.setBackground(new Color(108,117,125));
        btnForgot.setForeground(Color.WHITE);
        btnForgot.setFocusPainted(false);
        btnForgot.addActionListener(e -> forgotPassword());
        panel.add(btnForgot);
    }

    private void forgotPassword() {

        String userId = JOptionPane.showInputDialog(this, "Enter User ID:");
        if(userId == null || userId.trim().isEmpty()) return;

        String email = JOptionPane.showInputDialog(this, "Enter Email ID:");
        if(email == null || email.trim().isEmpty()) return;

        try {

            String query = "SELECT username,password FROM users WHERE user_id=? AND email=?";
            PreparedStatement pst = con.prepareStatement(query);

            pst.setString(1, userId);
            pst.setString(2, email);

            ResultSet rs = pst.executeQuery();

            if(rs.next()) {

                JOptionPane.showMessageDialog(this,
                        "Username: " + rs.getString("username") +
                        "\nPassword: " + rs.getString("password"));

            } else {

                JOptionPane.showMessageDialog(this,"Invalid Details!");

            }

        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, ex);
        }
    }

    public void actionPerformed(ActionEvent e) {

        String username = txtUsername.getText().trim();
        String password = String.valueOf(txtPassword.getPassword()).trim();

        try {

            String query = "SELECT * FROM login WHERE username=? AND password=?";
            PreparedStatement pst = con.prepareStatement(query);

            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if(rs.next()) {

                String userid = rs.getString("user_id");
                String role = rs.getString("role");

                dispose();

                if(role.equalsIgnoreCase("admin")) {
                    new AdminDashboard(con, userid).setVisible(true);
                } else {
                    new UserDashboard(con, userid).setVisible(true);
                }

            } else {

                JOptionPane.showMessageDialog(this,"Invalid Username or Password");

            }

        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, ex);
        }
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch(Exception e) {}

        Connection con = DBConnection.getConnection();
        new LoginGUI(con).setVisible(true);
    }
}