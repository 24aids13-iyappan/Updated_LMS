import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/*
 * StudentDetailView - Admin ku oru specific student oda full detail kaata.
 * Also shows the student's currently issued books + fine summary.
 */
public class StudentDetailView extends JFrame {

    private Connection con;
    private String adminUserId;
    private String studentUserId;

    public StudentDetailView(Connection con, String adminUserId, String studentUserId) {

        this.con = con;
        this.adminUserId = adminUserId;
        this.studentUserId = studentUserId;

        setTitle("Student Detail - " + studentUserId);
        setSize(650, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(main);

        JLabel title = new JLabel("Student Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(title);
        main.add(Box.createVerticalStrut(15));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(formPanel);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        try {
            String sql = "SELECT * FROM users WHERE user_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, studentUserId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                addRow(formPanel, "User ID:", rs.getString("user_id"), labelFont);
                addRow(formPanel, "Reg No:", rs.getString("reg_no"), labelFont);
                addRow(formPanel, "Name:", rs.getString("name"), labelFont);
                addRow(formPanel, "Department:", rs.getString("department"), labelFont);
                int admissionYear = rs.getInt("admission_year");
String currentYear = AcademicYearUtil.calculateYearOfStudy(admissionYear);
addRow(formPanel, "Admission Year:", String.valueOf(admissionYear), labelFont);
addRow(formPanel, "Current Year:", currentYear, labelFont); 

                java.sql.Date dob = rs.getDate("dob");
                String dobStr = dob != null ? new SimpleDateFormat("dd-MM-yyyy").format(dob) : "N/A";
                addRow(formPanel, "DOB:", dobStr, labelFont);

                addRow(formPanel, "Gender:", rs.getString("gender"), labelFont);
                addRow(formPanel, "Email:", rs.getString("email"), labelFont);
                addRow(formPanel, "Phone:", rs.getString("phone"), labelFont);
                addRow(formPanel, "Username:", rs.getString("username"), labelFont);
            } else {
                main.add(new JLabel("Student not found!"));
            }
            rs.close();
            pst.close();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        main.add(Box.createVerticalStrut(20));

        JLabel issuedTitle = new JLabel("Currently Issued Books");
        issuedTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        issuedTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(issuedTitle);
        main.add(Box.createVerticalStrut(8));

        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
        model.addColumn("Issue ID");
        model.addColumn("Book Name");
        model.addColumn("Due Date");
        model.addColumn("Status");
        model.addColumn("Fine");

        JTable table = new JTable(model);
        table.setRowHeight(26);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(600, 180));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(scroll);

        try {
            String sql2 = "SELECT issue_id, book_name, due_date, status, fine_amount FROM issue WHERE user_id=?";
            PreparedStatement pst2 = con.prepareStatement(sql2);
            pst2.setString(1, studentUserId);
            ResultSet rs2 = pst2.executeQuery();

            while (rs2.next()) {
                model.addRow(new Object[]{
                        rs2.getInt("issue_id"),
                        rs2.getString("book_name"),
                        rs2.getDate("due_date"),
                        rs2.getString("status"),
                        "Rs." + rs2.getDouble("fine_amount")
                });
            }
            rs2.close();
            pst2.close();

        } catch (Exception ex) {
            // table stays empty, no crash
        }

        main.add(Box.createVerticalStrut(20));

        JButton backBtn = new JButton("Back to Student List");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> {
            dispose();
            new ViewStudents(con, adminUserId).setVisible(true);
        });
        main.add(backBtn);
    }

    private void addRow(JPanel panel, String label, String value, Font font) {
        JLabel l = new JLabel(label);
        l.setFont(font);
        panel.add(l);
        panel.add(new JLabel(value != null ? value : "N/A"));
    }
}
