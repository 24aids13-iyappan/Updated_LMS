import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

// Background Panel
class BackgroundPanel extends JPanel {
    private Image bg;

    public BackgroundPanel() {
        bg = new ImageIcon("bgphoto/u_profile_bg.jpg").getImage();
        setLayout(null); // IMPORTANT
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
    }
}

public class UserProfile extends JFrame {

    String userid;
    private Connection con;

    public UserProfile(Connection con, String userid) {

        this.con = con;
        this.userid = userid;

        setTitle("User Profile");
        setSize(600, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BackgroundPanel bgPanel = new BackgroundPanel();
        setContentPane(bgPanel);

        // 📌 FORM PANEL → paper area ku correct ah set panniruken
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 2, 10, 10));
        formPanel.setBounds(250, 110, 300, 380); // 👈 MAIN MAGIC (adjust panna vendiya place)
        formPanel.setOpaque(false);

        Font labelFont = new Font("Serif", Font.BOLD, 14);
        Color textColor = new Color(60, 40, 20); // brown shade

        // Helper method
        formPanel.add(createLabel("USER ID:", labelFont, textColor));
        JLabel lblUserId = createValue(textColor); formPanel.add(lblUserId);

        formPanel.add(createLabel("REG.NO:", labelFont, textColor));
        JLabel lblreg = createValue(textColor); formPanel.add(lblreg);

        formPanel.add(createLabel("NAME:", labelFont, textColor));
        JLabel lblName = createValue(textColor); formPanel.add(lblName);

        formPanel.add(createLabel("DEPARTMENT:", labelFont, textColor));
        JLabel lbldep = createValue(textColor); formPanel.add(lbldep);

        formPanel.add(createLabel("YEAR:", labelFont, textColor));
        JLabel lblyear = createValue(textColor); formPanel.add(lblyear);

        formPanel.add(createLabel("DOB:", labelFont, textColor));
        JLabel lbldob = createValue(textColor); formPanel.add(lbldob);

        formPanel.add(createLabel("GENDER:", labelFont, textColor));
        JLabel lblgen = createValue(textColor); formPanel.add(lblgen);

        formPanel.add(createLabel("EMAIL:", labelFont, textColor));
        JLabel lblEmail = createValue(textColor); formPanel.add(lblEmail);

        formPanel.add(createLabel("PHONE:", labelFont, textColor));
        JLabel lblPhone = createValue(textColor); formPanel.add(lblPhone);

        formPanel.add(createLabel("ROLE:", labelFont, textColor));
        JLabel lblRole = createValue(textColor); formPanel.add(lblRole);

        bgPanel.add(formPanel);

        // 🔘 Back Button (paper keezha center)
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(250, 500, 100, 35);
        backBtn.setBackground(new Color(220,53,69));
        backBtn.setForeground(Color.WHITE);

        backBtn.addActionListener(e -> {
            dispose();
            new UserDashboard(con, userid).setVisible(true);
        });

        bgPanel.add(backBtn);

        // Load data
        loadProfile(lblUserId, lblreg, lblName, lbldep, lblyear,
                lbldob, lblgen, lblEmail, lblPhone, lblRole);

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

    private void loadProfile(JLabel lblUserId, JLabel lblreg, JLabel lblName,
                             JLabel lbldep, JLabel lblyear, JLabel lbldob,
                             JLabel lblgen, JLabel lblEmail,
                             JLabel lblPhone, JLabel lblRole) {

        try {
            String sql = "SELECT * FROM users WHERE user_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, userid);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                lblUserId.setText(rs.getString("user_id"));
                lblreg.setText(rs.getString("reg_no"));
                lblName.setText(rs.getString("name"));
                lbldep.setText(rs.getString("department"));
                int admissionYear = rs.getInt("admission_year");
                lblyear.setText(AcademicYearUtil.calculateYearOfStudy(admissionYear));
                java.sql.Date dob = rs.getDate("dob");
                if (dob != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    lbldob.setText(sdf.format(dob));
                } else {
                    lbldob.setText("N/A");
                }

                lblgen.setText(rs.getString("gender"));
                lblEmail.setText(rs.getString("email"));
                lblPhone.setText(rs.getString("phone"));
                lblRole.setText(rs.getString("role"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}