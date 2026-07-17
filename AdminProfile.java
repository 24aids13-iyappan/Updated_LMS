import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

// 🔹 Background Panel
class AdminBGPanel extends JPanel {
    private Image bg;

    public AdminBGPanel() {
        bg = new ImageIcon("bgphoto/admin_profile_bg.jpg").getImage();
        setLayout(null); // IMPORTANT
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
    }
}

public class AdminProfile extends JFrame {

    private Connection con;
    private String userId;   // ✅ COMMON ID

    public AdminProfile(Connection con, String userId) {

        this.con = con;
        this.userId = userId;

        setTitle("Admin Profile");
        setSize(650, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 🔹 Background
        AdminBGPanel bgPanel = new AdminBGPanel();
        setContentPane(bgPanel);

        // 📌 FORM PANEL (paper area)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 2, 10, 10));
        formPanel.setBounds(260, 120, 320, 400); // adjust based on image
        formPanel.setOpaque(false);

        Font labelFont = new Font("Serif", Font.BOLD, 15);
        Color textColor = new Color(70, 45, 25); // brown

        // 🔹 Fields
        formPanel.add(createLabel("ADMIN ID:", labelFont, textColor));
        JLabel lblId = createValue(textColor); formPanel.add(lblId);

        formPanel.add(createLabel("NAME:", labelFont, textColor));
        JLabel lblName = createValue(textColor); formPanel.add(lblName);

        formPanel.add(createLabel("DEPARTMENT:", labelFont, textColor));
        JLabel lblDept = createValue(textColor); formPanel.add(lblDept);

        formPanel.add(createLabel("DOB:", labelFont, textColor));
        JLabel lblDob = createValue(textColor); formPanel.add(lblDob);

        formPanel.add(createLabel("GENDER:", labelFont, textColor));
        JLabel lblGender = createValue(textColor); formPanel.add(lblGender);

        formPanel.add(createLabel("EMAIL:", labelFont, textColor));
        JLabel lblEmail = createValue(textColor); formPanel.add(lblEmail);

        formPanel.add(createLabel("PHONE:", labelFont, textColor));
        JLabel lblPhone = createValue(textColor); formPanel.add(lblPhone);

        formPanel.add(createLabel("USERNAME:", labelFont, textColor));
        JLabel lblUsername = createValue(textColor); formPanel.add(lblUsername);

        formPanel.add(createLabel("ROLE:", labelFont, textColor));
        JLabel lblRole = createValue(textColor); formPanel.add(lblRole);

        bgPanel.add(formPanel);

        // 🔘 Back Button
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(280, 550, 120, 35);
        backBtn.setBackground(new Color(220,53,69));
        backBtn.setForeground(Color.WHITE);

        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true); // ✅ FIXED
        });

        bgPanel.add(backBtn);

        // 🔥 Load Data
        loadAdminProfile(lblId, lblName, lblDept, lblDob,
                lblGender, lblEmail, lblPhone, lblUsername, lblRole);

        setVisible(true);
    }

    // 🔹 Label creator
    private JLabel createLabel(String text, Font f, Color c) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(f);
        lbl.setForeground(c);
        return lbl;
    }

    // 🔹 Value creator
    private JLabel createValue(Color c) {
        JLabel lbl = new JLabel();
        lbl.setForeground(c);
        return lbl;
    }

    // 🔹 Load admin data
    private void loadAdminProfile(JLabel lblId, JLabel lblName, JLabel lblDept,
                                 JLabel lblDob, JLabel lblGender,
                                 JLabel lblEmail, JLabel lblPhone,
                                 JLabel lblUsername, JLabel lblRole) {

        try {

            String sql = "SELECT * FROM admin WHERE user_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, userId); // ✅ FIXED

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                lblId.setText(rs.getString("user_id"));
                lblName.setText(rs.getString("name"));
                lblDept.setText(rs.getString("department"));

                java.sql.Date dob = rs.getDate("dob");
                if (dob != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    lblDob.setText(sdf.format(dob));
                } else {
                    lblDob.setText("N/A");
                }

                lblGender.setText(rs.getString("gender"));
                lblEmail.setText(rs.getString("email"));
                lblPhone.setText(rs.getString("phone"));
                lblUsername.setText(rs.getString("username"));
                lblRole.setText(rs.getString("role"));
            }

            rs.close();
            pst.close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading admin profile!");
        }
    }
}