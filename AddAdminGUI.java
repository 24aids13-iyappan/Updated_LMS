import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddAdminGUI extends JFrame implements ActionListener 
	{

    JTextField txtName, txtEmail, txtPhone, txtUsername;
    JPasswordField txtPassword;
    JComboBox<String> cmbGender, cmbRole,cmbDept;
    JButton btnRegister, btnBack;
    JDateChooser dateChooser;

    private Connection con;
private String userId;
    public AddAdminGUI(Connection con, String userId) {
        this.con = con;
this.userId = userId;
        setTitle("Admin Registration");
        setSize(600, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setResizable(false);
        // Background image panel
        BackgroundPanel bgPanel = new BackgroundPanel("bgphoto/admin_bg.jpg");
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        JLabel title = new JLabel("Add Admin");
        title.setBounds(220, 50, 200, 30);
        title.setForeground(Color.BLACK);
        bgPanel.add(title);

        JLabel lblName = new JLabel("Name:");
        lblName.setBounds(80, 100, 100, 30);
        bgPanel.add(lblName);

        txtName = new JTextField();
        txtName.setBounds(220, 100, 250, 30);
        bgPanel.add(txtName);

        JLabel lblDept = new JLabel("Department:");
        lblDept.setBounds(80, 150, 100, 30);
        bgPanel.add(lblDept);

        cmbDept = new JComboBox<>(new String[]{
        "CSE", "IT", "ECE", "EEE", "MECH", "CIVIL", "AI&DS"
});
        cmbDept.setBounds(220, 150, 250, 30);
        bgPanel.add(cmbDept);

        JLabel lblDOB = new JLabel("Date of Birth:");
        lblDOB.setBounds(80, 200, 100, 30);
        bgPanel.add(lblDOB);

        dateChooser = new JDateChooser();
        dateChooser.setBounds(220, 200, 250, 30);
        bgPanel.add(dateChooser);

        JLabel lblGender = new JLabel("Gender:");
        lblGender.setBounds(80, 250, 100, 30);
        bgPanel.add(lblGender);

        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Others"});
        cmbGender.setBounds(220, 250, 250, 30);
        bgPanel.add(cmbGender);

        JLabel lblPhone = new JLabel("Phone:");
        lblPhone.setBounds(80, 300, 100, 30);
        bgPanel.add(lblPhone);

        txtPhone = new JTextField();
        txtPhone.setBounds(220, 300, 250, 30);
        bgPanel.add(txtPhone);

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setBounds(80, 350, 100, 30);
        bgPanel.add(lblUsername);

        txtUsername = new JTextField();
        txtUsername.setBounds(220, 350, 250, 30);
        bgPanel.add(txtUsername);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(80, 400, 100, 30);
        bgPanel.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(220, 400, 250, 30);
        bgPanel.add(txtPassword);

        JLabel lblRole = new JLabel("Role:");
        lblRole.setBounds(80, 450, 100, 30);
        bgPanel.add(lblRole);

        cmbRole = new JComboBox<>(new String[]{"admin"});
        cmbRole.setBounds(220, 450, 250, 30);
        bgPanel.add(cmbRole);

Color brown = new Color(92, 64, 51);
Color paper = new Color(245, 238, 220);

Font labelFont = new Font("Segoe UI", Font.BOLD, 18);
Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);
Font titleFont = new Font("Segoe UI", Font.BOLD, 26);

// Title
title.setFont(titleFont);
//title.setForeground(brown);

// Labels
lblName.setFont(labelFont);
lblDept.setFont(labelFont);
lblDOB.setFont(labelFont);
lblGender.setFont(labelFont);
lblPhone.setFont(labelFont);
lblUsername.setFont(labelFont);
lblPassword.setFont(labelFont);
lblRole.setFont(labelFont);

lblName.setForeground(brown);
lblDept.setForeground(brown);
lblDOB.setForeground(brown);
lblGender.setForeground(brown);
lblPhone.setForeground(brown);
lblUsername.setForeground(brown);
lblPassword.setForeground(brown);
lblRole.setForeground(brown);

// Text fields
txtName.setFont(fieldFont);
cmbDept.setFont(fieldFont);
txtPhone.setFont(fieldFont);
txtUsername.setFont(fieldFont);
txtPassword.setFont(fieldFont);

txtName.setBackground(paper);
cmbDept.setBackground(paper);
txtPhone.setBackground(paper);
txtUsername.setBackground(paper);
txtPassword.setBackground(paper);

txtName.setForeground(Color.BLACK);
cmbDept.setForeground(Color.BLACK);
txtPhone.setForeground(Color.BLACK);
txtUsername.setForeground(Color.BLACK);
txtPassword.setForeground(Color.BLACK);

// Combo boxes
cmbGender.setFont(fieldFont);
cmbRole.setFont(fieldFont);
cmbDept.setFont(fieldFont);

cmbGender.setBackground(paper);
cmbRole.setBackground(paper);
cmbDept.setBackground(paper);

cmbGender.setForeground(Color.BLACK);
cmbRole.setForeground(Color.BLACK);
cmbDept.setForeground(Color.BLACK);

// Date chooser
dateChooser.setBackground(paper);
dateChooser.setForeground(Color.BLACK);
dateChooser.setFont(fieldFont);

        btnRegister = new JButton("Register");
        btnRegister.setBounds(180, 530, 120, 35);
	btnRegister.setBackground(new Color(40,167,69));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        bgPanel.add(btnRegister);

        btnBack = new JButton("Back");
        btnBack.setBounds(320, 530, 120, 35);
	btnBack.setBackground(new Color(220,53,69));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        bgPanel.add(btnBack);

        btnRegister.addActionListener(this);

        btnBack.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true);
        });

        setVisible(true);
    }

    // Background panel class
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

     public void actionPerformed(ActionEvent e) {

        if(e.getSource() == btnRegister) {

            if(txtName.getText().trim().isEmpty() ||
               cmbDept.getSelectedItem() == null ||
               dateChooser.getDate() == null ||
               txtEmail.getText().trim().isEmpty() ||
               txtPhone.getText().trim().isEmpty() ||
               txtUsername.getText().trim().isEmpty() ||
               txtPassword.getPassword().length == 0) {

                JOptionPane.showMessageDialog(null,
                        "All Fields Must Be Filled!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {

                String newUserId = generateUserId();

                Date dob = dateChooser.getDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = sdf.format(dob);

                String query = "INSERT INTO admin(user_id,name,department,dob,gender,email,phone,username,password,role) VALUES(?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement pst = con.prepareStatement(query);

                pst.setString(1, newUserId);
                pst.setString(2, txtName.getText());
                pst.setString(3, cmbDept.getSelectedItem().toString());
                pst.setString(4, formattedDate);
                pst.setString(5, cmbGender.getSelectedItem().toString());
                pst.setString(6, txtEmail.getText());
                pst.setString(7, txtPhone.getText());
                pst.setString(8, txtUsername.getText());
                pst.setString(9, String.valueOf(txtPassword.getPassword()));
                pst.setString(10, cmbRole.getSelectedItem().toString());

                pst.executeUpdate();

                String loginQuery = "INSERT INTO login(user_id,username,password,role) VALUES(?,?,?,?)";

                PreparedStatement pst2 = con.prepareStatement(loginQuery);

                pst2.setString(1, newUserId);
                pst2.setString(2, txtUsername.getText());
                pst2.setString(3, String.valueOf(txtPassword.getPassword()));
                pst2.setString(4, cmbRole.getSelectedItem().toString());

                pst2.executeUpdate();

                JOptionPane.showMessageDialog(null,
                        "Admin Registered Successfully!\n\nUser ID: " + newUserId +
                        "\nUser Name: " + txtUsername.getText() +
                        "\nPassword: " + new String(txtPassword.getPassword()));

                clearFields();

            } catch(Exception ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        }
    }

    private String generateUserId() throws Exception {

        String newUserId = "LBA001";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT TOP 1 user_id FROM admin ORDER BY user_id DESC");

        if (rs.next()) {
            String lastId = rs.getString("user_id");
            int number = Integer.parseInt(lastId.substring(3));
            number++;
            newUserId = String.format("LBA%03d", number);
        }

        return newUserId;
    }

    private void clearFields() {
        txtName.setText("");
        cmbDept.setSelectedIndex(0);
        dateChooser.setDate(null);
        txtEmail.setText("");
        txtPhone.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
    }
}
