import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/*
 * ViewStudents - Admin ku ella students record ah table la kaata,
 * ovoru row la "View" button irukum, click pannuna StudentDetailView open aagum.
 *
 * Wiring: AdminDashboard menu "Book" pakkathula/oru pudhu menu "Students" add pannu:
 *   JMenu menuStudents = new JMenu("Students");
 *   JMenuItem viewStudents = new JMenuItem("View Students");
 *   menuStudents.add(viewStudents);
 *   menubar.add(menuStudents);
 *   viewStudents.addActionListener(e -> { dispose(); new ViewStudents(con, userid).setVisible(true); });
 */
public class ViewStudents extends JFrame {

    private Connection con;
    private String adminUserId;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
     JLabel lblCount;

    public ViewStudents(Connection con, String adminUserId) {

        this.con = con;
        this.adminUserId = adminUserId;

        setTitle("Student Records");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout());
        setContentPane(main);

        JPanel northContainer = new JPanel();
    northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));

        // Top - Title + Search
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search (Name/RegNo):"));
        txtSearch = new JTextField(15);
        top.add(txtSearch);

        top.add(new JLabel("Dept:"));
        JComboBox<String> cmbDeptFilter = new JComboBox<>(new String[]{
        "All", "CSE", "IT", "ECE", "EEE", "MECH", "CIVIL", "AI&DS"});
        top.add(cmbDeptFilter);

        top.add(new JLabel("Year:"));
        JComboBox<String> cmbYearFilter = new JComboBox<>(new String[]{
        "All", "1st Year", "2nd Year", "3rd Year", "4th Year"});
        top.add(cmbYearFilter);

        JButton btnSearch = new JButton("Search");
        top.add(btnSearch);
        JButton btnRefresh = new JButton("Refresh");
        top.add(btnRefresh);

  JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblCount = new JLabel("Total Students: 0");
lblCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
lblCount.setForeground(new Color(0, 120, 215));

countRow.add(lblCount);

    northContainer.add(top);
    northContainer.add(countRow);

    main.add(northContainer, BorderLayout.NORTH);


        // Table - last column will render a button
        model = new DefaultTableModel(
                new Object[]{"User ID", "Reg No", "Name", "Department", "Year", "Email", "Phone", "View"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 7; // only "View" button column clickable
            }
        };

        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        table.getColumn("View").setCellRenderer(new ButtonRenderer());
        table.getColumn("View").setCellEditor(new ButtonEditor(new JCheckBox()));

        main.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, adminUserId).setVisible(true);
        });
        JPanel bottom = new JPanel();
        bottom.add(backBtn);
        main.add(bottom, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> loadStudents(txtSearch.getText().trim(), (String) cmbDeptFilter.getSelectedItem(), (String) cmbYearFilter.getSelectedItem()));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbDeptFilter.setSelectedIndex(0);
            cmbYearFilter.setSelectedIndex(0);
            loadStudents("", "All", "All");
        });

        loadStudents("", "All", "All");

        setVisible(true);
    }

    private void loadStudents(String keyword, String dept, String year) {
        model.setRowCount(0);
            int count = 0;

        try {
           StringBuilder sql = new StringBuilder(
            "SELECT user_id, reg_no, name, department, admission_year, email, phone FROM users WHERE 1=1");

        if (!keyword.isEmpty()) {
            sql.append(" AND (name LIKE ? OR reg_no LIKE ?)");
        }
        if (!dept.equals("All")) {
            sql.append(" AND department = ?");
        }
        if (!year.equals("All")) {
            sql.append(" AND year = ?");
        }

        PreparedStatement pst = con.prepareStatement(sql.toString());
        int idx = 1;

        if (!keyword.isEmpty()) {
            pst.setString(idx++, "%" + keyword + "%");
            pst.setString(idx++, "%" + keyword + "%");
        }
        if (!dept.equals("All")) {
            pst.setString(idx++, dept);
        }
        if (!year.equals("All")) {
            pst.setString(idx++, year);
        }


            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int admissionYear = rs.getInt("admission_year");
                String currentYear = AcademicYearUtil.calculateYearOfStudy(admissionYear);
                
                if (!year.equals("All") && !currentYear.equals(year)) {
        continue; // skip this row, filter is not match
    }
                model.addRow(new Object[]{
                        rs.getString("user_id"),
                        rs.getString("reg_no"),
                        rs.getString("name"),
                        rs.getString("department"),
                        currentYear,
                        rs.getString("email"),
                        rs.getString("phone"),
                        "View"
                });
                count++;
            }

            lblCount.setText("Total Students: " + count);
            rs.close();
            pst.close();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
        }
    }

    // Renders a button inside the table cell
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(0, 120, 215));
            setForeground(Color.WHITE);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("View");
            return this;
        }
    }

    // Handles the click on the button inside the table cell
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean clicked;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(new Color(0, 120, 215));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = "View";
            button.setText(label);
            clicked = true;
            currentRow = row;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                String studentUserId = (String) model.getValueAt(currentRow, 0);
                dispose();
                new StudentDetailView(con, adminUserId, studentUserId).setVisible(true);
            }
            clicked = false;
            return label;
        }
    }
}
