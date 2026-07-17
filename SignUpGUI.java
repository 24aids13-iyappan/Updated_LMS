import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SignUpGUI extends JFrame implements ActionListener {

    JTextField txtName, txtRegNo, txtEmail, txtPhone;// txtUsername;
    //JPasswordField txtPassword;
    JComboBox<String> cmbDept, cmbYear, cmbGender, cmbRole;
    JButton btnRegister, btnBack;
    JDateChooser dateChooser;   // 🔹 DOB Calendar

    private Connection con;
public SignUpGUI(Connection con) {

    this.con = con;

    setTitle("Library Sign Up");
    setSize(650, 800);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Color brown = new Color(92, 64, 51);
    Color paper = new Color(245, 238, 220);

    Font labelFont = new Font("Segoe UI", Font.BOLD, 18);
    Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);
    Font titleFont = new Font("Segoe UI", Font.BOLD, 26);

    // Background panel
    BackgroundPanel bgPanel = new BackgroundPanel("bgphoto/sign_bg.jpg");
    bgPanel.setLayout(null);
    setContentPane(bgPanel);

    // Title
    JLabel title = new JLabel("Library Sign Up");
    title.setFont(titleFont);
    title.setForeground(brown);
    title.setBounds(210, 40, 250, 35);
    bgPanel.add(title);

    // Labels
    JLabel lblName = new JLabel("Name:");
    lblName.setFont(labelFont);
    lblName.setForeground(brown);
    lblName.setBounds(70, 100, 130, 30);
    bgPanel.add(lblName);

    JLabel lblRegNo = new JLabel("Reg No:");
    lblRegNo.setFont(labelFont);
    lblRegNo.setForeground(brown);
    lblRegNo.setBounds(70, 150, 130, 30);
    bgPanel.add(lblRegNo);

    JLabel lblDept = new JLabel("Department:");
    lblDept.setFont(labelFont);
    lblDept.setForeground(brown);
    lblDept.setBounds(70, 200, 130, 30);
    bgPanel.add(lblDept);

    JLabel lblYear = new JLabel("Admission Year:");
    lblYear.setFont(labelFont);
    lblYear.setForeground(brown);
    lblYear.setBounds(70, 250, 130, 30);
    bgPanel.add(lblYear);

    JLabel lblDOB = new JLabel("DOB:");
    lblDOB.setFont(labelFont);
    lblDOB.setForeground(brown);
    lblDOB.setBounds(70, 300, 130, 30);
    bgPanel.add(lblDOB);

    JLabel lblGender = new JLabel("Gender:");
    lblGender.setFont(labelFont);
    lblGender.setForeground(brown);
    lblGender.setBounds(70, 350, 130, 30);
    bgPanel.add(lblGender);

    JLabel lblEmail = new JLabel("Email:");
    lblEmail.setFont(labelFont);
    lblEmail.setForeground(brown);
    lblEmail.setBounds(70, 400, 130, 30);
    bgPanel.add(lblEmail);

    JLabel lblPhone = new JLabel("Phone:");
    lblPhone.setFont(labelFont);
    lblPhone.setForeground(brown);
    lblPhone.setBounds(70, 450, 130, 30);
    bgPanel.add(lblPhone);

  /*  JLabel lblUsername = new JLabel("Username:");
    lblUsername.setFont(labelFont);
    lblUsername.setForeground(brown);
    lblUsername.setBounds(70, 500, 130, 30);
    bgPanel.add(lblUsername);

    JLabel lblPassword = new JLabel("Password:");
    lblPassword.setFont(labelFont);
    lblPassword.setForeground(brown);
    lblPassword.setBounds(70, 550, 130, 30);
    bgPanel.add(lblPassword);*/

    JLabel lblRole = new JLabel("Role:");
    lblRole.setFont(labelFont);
    lblRole.setForeground(brown);
    lblRole.setBounds(70, 500, 130, 30);
    bgPanel.add(lblRole);

    // Fields
    txtName = new JTextField();
    txtName.setBounds(220, 100, 300, 35);
    txtName.setFont(fieldFont);
    txtName.setBackground(paper);
    txtName.setForeground(Color.BLACK);
    bgPanel.add(txtName);

    txtRegNo = new JTextField();
    txtRegNo.setBounds(220, 150, 300, 35);
    txtRegNo.setFont(fieldFont);
    txtRegNo.setBackground(paper);
    txtRegNo.setForeground(Color.BLACK);
    bgPanel.add(txtRegNo);

    cmbDept = new JComboBox<>(new String[]{"CSE", "IT", "ECE", "EEE", "MECH", "CIVIL", "AI&DS"});
    cmbDept.setBounds(220, 200, 300, 35);
    cmbDept.setFont(fieldFont);
    cmbDept.setBackground(paper);
    cmbDept.setForeground(Color.BLACK);
    bgPanel.add(cmbDept);

    cmbYear = new JComboBox<>();
int currentAcademicYear = AcademicYearUtil.getCurrentAcademicYear();
for (int y = currentAcademicYear; y >= currentAcademicYear - 4; y--) {
    cmbYear.addItem(String.valueOf(y));
}
    cmbYear.setBounds(220, 250, 300, 35);
    cmbYear.setFont(fieldFont);
    cmbYear.setBackground(paper);
    cmbYear.setForeground(Color.BLACK);
    bgPanel.add(cmbYear);

    dateChooser = new JDateChooser();
    dateChooser.setBounds(220, 300, 300, 35);
    dateChooser.setDateFormatString("yyyy-MM-dd");
    bgPanel.add(dateChooser);

    cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Others"});
    cmbGender.setBounds(220, 350, 300, 35);
    cmbGender.setFont(fieldFont);
    cmbGender.setBackground(paper);
    cmbGender.setForeground(Color.BLACK);
    bgPanel.add(cmbGender);

    txtEmail = new JTextField();
    txtEmail.setBounds(220, 400, 300, 35);
    txtEmail.setFont(fieldFont);
    txtEmail.setBackground(paper);
    txtEmail.setForeground(Color.BLACK);
    bgPanel.add(txtEmail);

    txtPhone = new JTextField();
    txtPhone.setBounds(220, 450, 300, 35);
    txtPhone.setFont(fieldFont);
    txtPhone.setBackground(paper);
    txtPhone.setForeground(Color.BLACK);
    bgPanel.add(txtPhone);

   /* txtUsername = new JTextField();
    txtUsername.setBounds(220, 500, 300, 35);
    txtUsername.setFont(fieldFont);
    txtUsername.setBackground(paper);
    txtUsername.setForeground(Color.BLACK);
    bgPanel.add(txtUsername);

    txtPassword = new JPasswordField();
    txtPassword.setBounds(220, 550, 300, 35);
    txtPassword.setFont(fieldFont);
    txtPassword.setBackground(paper);
    txtPassword.setForeground(Color.BLACK);
    bgPanel.add(txtPassword);*/

    cmbRole = new JComboBox<>(new String[]{"user"});
    cmbRole.setBounds(220, 500, 300, 35);
    cmbRole.setFont(fieldFont);
    cmbRole.setBackground(paper);
    cmbRole.setForeground(Color.BLACK);
    bgPanel.add(cmbRole);

    // Buttons
    btnRegister = new JButton("Register");
    btnRegister.setBounds(70, 600, 130, 40);
    btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btnRegister.setBackground(new Color(80, 120, 70));
    btnRegister.setForeground(Color.WHITE);
    btnRegister.setFocusPainted(false);
    bgPanel.add(btnRegister);

    btnBack = new JButton("Back");
    btnBack.setBounds(220, 600, 130, 40);
    btnBack.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btnBack.setBackground(new Color(150, 80, 80));
    btnBack.setForeground(Color.WHITE);
    btnBack.setFocusPainted(false);
    bgPanel.add(btnBack);

    // Events
    btnRegister.addActionListener(this);

    btnBack.addActionListener(e -> {
        dispose();
        new LoginGUI(con).setVisible(true);
    });

    setVisible(true);
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

    public void actionPerformed(ActionEvent e) {

    if (e.getSource() == btnRegister) {

        if (txtName.getText().trim().isEmpty() ||
            txtRegNo.getText().trim().isEmpty() ||
            
            cmbDept.getSelectedItem() == null ||
            cmbYear.getSelectedItem() == null ||
            dateChooser.getDate() == null ||
            txtEmail.getText().trim().isEmpty() ||
            txtPhone.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    null,
                    "All Fields Must Be Filled!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String[] credentials = showCredentialDialog();

if (credentials[0] == null) {
    return;
}

String username = credentials[0];
String password = credentials[1];

if (username.isEmpty() || password.isEmpty()) {
    JOptionPane.showMessageDialog(null,
            "Username and Password cannot be empty!");
    return;
}
            try {

                String newUserId = generateUserId();

                Date selectedDate = dateChooser.getDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dob = sdf.format(selectedDate);

                String query = "INSERT INTO users(user_id,name,reg_no,department,admission_year,dob,gender,email,phone,username,password,role) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement pst = con.prepareStatement(query);

                pst.setString(1, newUserId);
                pst.setString(2, txtName.getText());
                pst.setString(3, txtRegNo.getText());
                pst.setString(4, cmbDept.getSelectedItem().toString());
                pst.setString(5, cmbYear.getSelectedItem().toString());
                pst.setString(6, dob);
                pst.setString(7, cmbGender.getSelectedItem().toString());
                pst.setString(8, txtEmail.getText());
                pst.setString(9, txtPhone.getText());
                pst.setString(10, username);
                pst.setString(11, password);
                pst.setString(12, cmbRole.getSelectedItem().toString());

                pst.executeUpdate();

                String loginQuery = "INSERT INTO login(user_id,username,password,role) VALUES(?,?,?,?)";

                PreparedStatement pst2 = con.prepareStatement(loginQuery);

                pst2.setString(1, newUserId);
                pst2.setString(2, username);
                pst2.setString(3, password);
                pst2.setString(4, cmbRole.getSelectedItem().toString());

                pst2.executeUpdate();

                JOptionPane.showMessageDialog(
                        null,
                        "Registration Successful!\n\n" +
                        "User ID : " + newUserId + "\n" +
                        "Username : " + username + "\n" +
                        "Password : " + password
                );

                clearFields();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        }
    }

private String[] showCredentialDialog() {

    JDialog dialog = new JDialog(this, "Create Login", true);
    dialog.setSize(450, 300);
    dialog.setLocationRelativeTo(this);

    BackgroundPanel bg = new BackgroundPanel("bgphoto/pop_bg.jpg");
    bg.setLayout(null);
    dialog.setContentPane(bg);

    Color brown = new Color(92, 64, 51);
    Color paper = new Color(245, 238, 220);

    JLabel lblUser = new JLabel("Username:");
    lblUser.setBounds(40, 60, 100, 30);
    lblUser.setForeground(brown);
    lblUser.setFont(new Font("Segoe UI", Font.BOLD, 16));
    bg.add(lblUser);

    JTextField txtUser = new JTextField();
    txtUser.setBounds(160, 60, 200, 30);
    txtUser.setBackground(paper);
    txtUser.setForeground(Color.BLACK);
    bg.add(txtUser);

    JLabel lblPass = new JLabel("Password:");
    lblPass.setBounds(40, 120, 100, 30);
    lblPass.setForeground(brown);
    lblPass.setFont(new Font("Segoe UI", Font.BOLD, 16));
    bg.add(lblPass);

    JPasswordField txtPass = new JPasswordField();
    txtPass.setBounds(160, 120, 200, 30);
    txtPass.setBackground(paper);
    txtPass.setForeground(Color.BLACK);
    bg.add(txtPass);

    JButton btnConfirm = new JButton("Confirm");
    btnConfirm.setBounds(90, 200, 110, 35);
    bg.add(btnConfirm);

    JButton btnBack = new JButton("Back");
    btnBack.setBounds(230, 200, 110, 35);
    bg.add(btnBack);

    final String[] data = new String[2];

    btnConfirm.addActionListener(e -> {
        data[0] = txtUser.getText().trim();
        data[1] = new String(txtPass.getPassword());
        dialog.dispose();
    });

    btnBack.addActionListener(e -> {
        data[0] = null;
        data[1] = null;
        dialog.dispose();
    });

    dialog.setVisible(true);

    return data;
}


    private String generateUserId() throws Exception {

        String newUserId = "LBU001";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(
                "SELECT TOP 1 user_id FROM users ORDER BY user_id DESC");

        if (rs.next()) {
            String lastId = rs.getString("user_id");
            int number = Integer.parseInt(lastId.substring(3));
            number++;
            newUserId = String.format("LBU%03d", number);
        }

        return newUserId;
    }


    private void clearFields() {
    txtName.setText("");
    txtRegNo.setText("");
    cmbDept.setSelectedIndex(-1);
    cmbYear.setSelectedIndex(-1);
    txtEmail.setText("");
    txtPhone.setText("");
    dateChooser.setDate(null);
}
}
