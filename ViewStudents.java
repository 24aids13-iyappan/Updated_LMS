import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/*
 * ViewStudents - Admin ku ella students record ah table la kaata,
 * ovoru row la "View" button irukum, click pannuna StudentDetailView open aagum.
 * Background image + vintage/antique themed table added (matches ReportModule theme).
 *
 * Wiring: AdminDashboard menu "Book" pakkathula/oru pudhu menu "Students" add pannu:
 *   JMenu menuStudents = new JMenu("Students");
 *   JMenuItem viewStudents = new JMenuItem("View Students");
 *   menuStudents.add(viewStudents);
 *   menubar.add(menuStudents);
 *   viewStudents.addActionListener(e -> { dispose(); new ViewStudents(con, userid).setVisible(true); });
 *
 * NOTE: Update BG_IMAGE_PATH below to point to your actual image file.
 */
public class ViewStudents extends JFrame {

    private static final String BG_IMAGE_PATH = "bgphoto/studentdetail.jpg";

    // ---------- Theme palette (same as ReportModule) ----------
    private static final Color WALNUT      = new Color(0x3E, 0x2A, 0x1D);
    private static final Color WALNUT_DARK = new Color(0x2B, 0x1B, 0x0F);
    private static final Color GOLD        = new Color(0xB0, 0x8D, 0x57);
    private static final Color GOLD_LIGHT  = new Color(0xD8, 0xC0, 0x8E);
    private static final Color PARCHMENT   = new Color(0xF3, 0xE9, 0xD2, 235);
    private static final Color PARCHMENT_2 = new Color(0xE8, 0xD9, 0xB5, 235);
    private static final Color INK         = new Color(0x2B, 0x1B, 0x0F);
    private static final Color MAROON      = new Color(0x6B, 0x22, 0x1E);
    private static final Color MAROON_DARK = new Color(0x52, 0x18, 0x15);

    private static final Font  FONT_LABEL  = new Font("Georgia", Font.PLAIN, 13);
    private static final Font  FONT_HEADER = new Font("Georgia", Font.BOLD, 13);
    private static final Font  FONT_BTN    = new Font("Georgia", Font.BOLD, 13);
    private static final Font  FONT_TITLE  = new Font("Georgia", Font.BOLD, 15);

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
        setResizable(false);

        // --- Background panel replaces the plain JPanel as content pane ---
        BackgroundPanel main = new BackgroundPanel(BG_IMAGE_PATH);
        main.setLayout(new BorderLayout());
        // Padding so controls stay clear of the ornate corner artwork
        main.setBorder(BorderFactory.createEmptyBorder(30, 45, 30, 45));
        setContentPane(main);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setOpaque(false);

        // --- Top - Search / filter strip (parchment panel, not floating on bare bg) ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        top.setOpaque(true);
        top.setBackground(new Color(0xF3, 0xE9, 0xD2, 210));
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JLabel lblSearch = new JLabel("Search (Name/RegNo):");
        lblSearch.setFont(FONT_LABEL);
        lblSearch.setForeground(INK);
        top.add(lblSearch);

        txtSearch = new JTextField(15);
        styleTextField(txtSearch);
        top.add(txtSearch);

        JLabel lblDept = new JLabel("Dept:");
        lblDept.setFont(FONT_LABEL);
        lblDept.setForeground(INK);
        top.add(lblDept);

        JComboBox<String> cmbDeptFilter = new JComboBox<>(new String[]{
                "All", "CSE", "IT", "ECE", "EEE", "MECH", "CIVIL", "AI&DS"});
        styleCombo(cmbDeptFilter);
        top.add(cmbDeptFilter);

        JLabel lblYear = new JLabel("Year:");
        lblYear.setFont(FONT_LABEL);
        lblYear.setForeground(INK);
        top.add(lblYear);

        JComboBox<String> cmbYearFilter = new JComboBox<>(new String[]{
                "All", "1st Year", "2nd Year", "3rd Year", "4th Year"});
        styleCombo(cmbYearFilter);
        top.add(cmbYearFilter);

        JButton btnSearch = new JButton("Search");
        styleButton(btnSearch, MAROON, MAROON_DARK, GOLD_LIGHT);
        top.add(btnSearch);

        JButton btnRefresh = new JButton("Refresh");
        styleButton(btnRefresh, WALNUT, WALNUT_DARK, GOLD_LIGHT);
        top.add(btnRefresh);

        // --- Count row (own parchment strip so it stays legible) ---
        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        countRow.setOpaque(true);
        countRow.setBackground(new Color(0xF3, 0xE9, 0xD2, 190));
        countRow.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        lblCount = new JLabel("Total Students: 0");
        lblCount.setFont(FONT_TITLE);
        lblCount.setForeground(WALNUT_DARK);
        countRow.add(lblCount);

        northContainer.add(top);
        northContainer.add(Box.createVerticalStrut(6));
        northContainer.add(countRow);
        northContainer.add(Box.createVerticalStrut(10));

        main.add(northContainer, BorderLayout.NORTH);

        // --- Table - last column will render a button ---
        model = new DefaultTableModel(
                new Object[]{"User ID", "Reg No", "Name", "Department", "Year", "Email", "Phone", "View"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 7; // only "View" button column clickable
            }
        };

        table = new JTable(model);
        table.setRowHeight(34);
        makeTableThemed(table);

        table.getColumn("View").setCellRenderer(new ButtonRenderer());
        table.getColumn("View").setCellEditor(new ButtonEditor(new JCheckBox()));
        table.getColumn("View").setPreferredWidth(90);
        table.getColumn("View").setMaxWidth(110);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 12, 0),
                BorderFactory.createLineBorder(GOLD, 2)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        main.add(scrollPane, BorderLayout.CENTER);

        // --- Bottom bar: Back button pinned bottom-right ---
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(true);
        bottom.setBackground(new Color(0xF3, 0xE9, 0xD2, 190));
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, GOLD));

        JButton backBtn = new JButton("Back");
        styleButton(backBtn, new Color(0x6E, 0x63, 0x52), new Color(0x55, 0x4B, 0x3E), Color.WHITE);
        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, adminUserId).setVisible(true);
        });
        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backWrap.setOpaque(false);
        backWrap.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        backWrap.add(backBtn);
        bottom.add(backWrap, BorderLayout.EAST);

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

    // ---------- Styling helpers ----------

    private void styleButton(JButton btn, Color base, Color darker, Color textColor) {
        btn.setFont(FONT_BTN);
        btn.setBackground(base);
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(darker);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(base);
            }
        });
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(FONT_LABEL);
        combo.setBackground(PARCHMENT);
        combo.setForeground(INK);
        combo.setBorder(BorderFactory.createLineBorder(GOLD, 1));
        combo.setPreferredSize(new Dimension(110, 28));
    }

    private void styleTextField(JTextField field) {
        field.setFont(FONT_LABEL);
        field.setForeground(INK);
        field.setBackground(PARCHMENT);
        field.setCaretColor(INK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
    }

    /**
     * Themes the JTable to look like an antique ledger: parchment rows with
     * alternating shading, walnut/gold header, warm gridlines, and dark ink
     * text so it stays legible over the background image (fixes the washed
     * -out white-on-parchment look from the previous version). The "View"
     * button column keeps its own renderer/editor so it is unaffected.
     */
    private void makeTableThemed(JTable table) {
        table.setOpaque(false);
        table.setFont(FONT_LABEL);
        table.setForeground(INK);
        table.setShowGrid(true);
        table.setGridColor(GOLD.darker());
        table.setSelectionBackground(new Color(0xB0, 0x8D, 0x57, 170));
        table.setSelectionForeground(WALNUT_DARK);
        table.setIntercellSpacing(new Dimension(1, 1));

        DefaultTableCellRenderer themedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setOpaque(true);
                setFont(FONT_LABEL);
                setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                if (isSelected) {
                    setBackground(new Color(0xB0, 0x8D, 0x57, 180));
                    setForeground(WALNUT_DARK);
                } else {
                    setBackground(row % 2 == 0 ? PARCHMENT : PARCHMENT_2);
                    setForeground(INK);
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, themedRenderer);

        JTableHeader header = table.getTableHeader();
        header.setOpaque(true);
        header.setBackground(WALNUT);
        header.setForeground(GOLD_LIGHT);
        header.setFont(FONT_HEADER);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, GOLD));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
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

    // Renders a button inside the table cell (themed to match the rest of the UI)
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(FONT_BTN);
            setBackground(MAROON);
            setForeground(GOLD_LIGHT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GOLD, 1),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)));
            setFocusPainted(false);
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
            button.setFont(FONT_BTN);
            button.setBackground(MAROON_DARK);
            button.setForeground(GOLD_LIGHT);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GOLD, 1),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)));
            button.setFocusPainted(false);
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

    /**
     * A JPanel that paints a background image stretched to fill the panel.
     * Any child component placed on it should be setOpaque(false) to let
     * the image show through.
     */
    static class BackgroundPanel extends JPanel {
        private Image bgImage;

        public BackgroundPanel(String imagePath) {
            try {
                ImageIcon icon = new ImageIcon(imagePath);
                if (icon.getIconWidth() > 0) {
                    bgImage = icon.getImage();
                } else {
                    System.err.println("Background image not found at: " + imagePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            setOpaque(true); // this panel itself paints the image, so keep it opaque
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(0xEF, 0xE3, 0xC8));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}