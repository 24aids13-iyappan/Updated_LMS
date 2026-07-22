import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.print.*;
import java.sql.*;
import java.time.LocalDate;
import com.toedter.calendar.JDateChooser;

/*
 * ReportModule - Admin ku "survey"/activity report:
 *   - Quick filter: Today / This Month / This Year
 *   - Custom "From Date" - "To Date" range
 *   - Shows issued books, returns, and fines in that range
 *   - Print button (uses java.awt.print - no extra library needed)
 *   - Background image + vintage/antique themed table & controls
 *
 * VISUAL THEME NOTES:
 *   Pulled from the ornate parchment/antique-scroll background:
 *     - Deep walnut brown  : #3E2A1D  (headers, primary buttons, borders)
 *     - Aged gold/bronze   : #B08D57  (accents, border lines, secondary button)
 *     - Warm parchment     : #F3E9D2  (table rows, input backgrounds)
 *     - Muted parchment 2  : #E8D9B5  (alternating row)
 *     - Ink brown text     : #2B1B0F
 *   Fonts switched to Serif ("Georgia"/"Garamond"-style fallback) to match
 *   the antique look instead of the default Segoe UI sans-serif.
 *
 * Wiring: AdminDashboard la oru menu add pannu:
 *   JMenuItem reportItem = new JMenuItem("Reports");
 *   menuBookmaneg.add(reportItem);
 *   reportItem.addActionListener(e -> { dispose(); new ReportModule(con, userid).setVisible(true); });
 *
 * NOTE: Update BG_IMAGE_PATH below to point to your actual image file
 * (put it inside your project, e.g. "images/library_bg.jpg", or use an
 * absolute path / classpath resource as needed).
 */
public class ReportModule extends JFrame {

    private static final String BG_IMAGE_PATH = "bgphoto/report.jpg";

    // ---------- Theme palette ----------
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
    private String userId;
    private JTable table;
    private DefaultTableModel model;
    private JDateChooser fromDate, toDate;
    private JComboBox<String> cmbQuickFilter;
    private JLabel lblSummary;

    public ReportModule(Connection con, String userId) {

        this.con = con;
        this.userId = userId;

        setTitle("Reports & Survey");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // --- Background panel replaces the plain JPanel as content pane ---
        BackgroundPanel main = new BackgroundPanel(BG_IMAGE_PATH);
        main.setLayout(new BorderLayout());
        // Padding so components stay clear of the ornate corner artwork
        main.setBorder(BorderFactory.createEmptyBorder(38, 55, 38, 55));
        setContentPane(main);

        // --- Filter Panel (sits inside a soft parchment strip, not floating on bare bg) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterPanel.setOpaque(true);
        filterPanel.setBackground(new Color(0xF3, 0xE9, 0xD2, 210));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JLabel lblQuick = new JLabel("Quick:");
        lblQuick.setFont(FONT_LABEL);
        lblQuick.setForeground(INK);
        filterPanel.add(lblQuick);

        cmbQuickFilter = new JComboBox<>(new String[]{
                "Custom Range", "Today", "This Week", "This Month", "This Year"
        });
        styleCombo(cmbQuickFilter);
        filterPanel.add(cmbQuickFilter);

        JLabel lblFrom = new JLabel("From:");
        lblFrom.setFont(FONT_LABEL);
        lblFrom.setForeground(INK);
        filterPanel.add(lblFrom);

        fromDate = new JDateChooser();
        fromDate.setDateFormatString("yyyy-MM-dd");
        fromDate.setPreferredSize(new Dimension(135, 28));
        styleDateChooser(fromDate);
        filterPanel.add(fromDate);

        JLabel lblTo = new JLabel("To:");
        lblTo.setFont(FONT_LABEL);
        lblTo.setForeground(INK);
        filterPanel.add(lblTo);

        toDate = new JDateChooser();
        toDate.setDateFormatString("yyyy-MM-dd");
        toDate.setPreferredSize(new Dimension(135, 28));
        toDate.setDate(new java.util.Date());
        styleDateChooser(toDate);
        filterPanel.add(toDate);

        JButton btnGenerate = new JButton("Generate Report");
        styleButton(btnGenerate, MAROON, MAROON_DARK, GOLD_LIGHT);
        filterPanel.add(btnGenerate);

        JButton btnPrint = new JButton("Print Report");
        styleButton(btnPrint, WALNUT, WALNUT_DARK, GOLD_LIGHT);
        filterPanel.add(btnPrint);

        main.add(filterPanel, BorderLayout.NORTH);

        // --- Table ---
        model = new DefaultTableModel(new Object[]{
                "Issue ID", "User ID", "Student Name", "Book Name",
                "Issue Date", "Due Date", "Status", "Return Date", "Fine"
        }, 0);
        table = new JTable(model);
        table.setRowHeight(28);
        makeTableThemed(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(14, 0, 14, 0),
                BorderFactory.createLineBorder(GOLD, 2)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        main.add(scrollPane, BorderLayout.CENTER);

        // --- Summary bar + Back button (bottom-right) ---
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(true);
        southPanel.setBackground(new Color(0xF3, 0xE9, 0xD2, 190));
        southPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, GOLD));

        lblSummary = new JLabel("Select a filter and click Generate Report");
        lblSummary.setFont(FONT_TITLE);
        lblSummary.setForeground(WALNUT_DARK);
        lblSummary.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
        lblSummary.setOpaque(false);
        southPanel.add(lblSummary, BorderLayout.CENTER);

        JButton backBtn = new JButton("Back");
        styleButton(backBtn, new Color(0x6E, 0x63, 0x52), new Color(0x55, 0x4B, 0x3E), Color.WHITE);
        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true);
        });
        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backWrap.setOpaque(false);
        backWrap.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        backWrap.add(backBtn);
        southPanel.add(backWrap, BorderLayout.EAST);

        main.add(southPanel, BorderLayout.SOUTH);

        // --- Quick filter behavior ---
        cmbQuickFilter.addActionListener(e -> applyQuickFilter());

        btnGenerate.addActionListener(e -> generateReport());
        btnPrint.addActionListener(e -> printReport());

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

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(darker);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(base);
            }
        });
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(FONT_LABEL);
        combo.setBackground(PARCHMENT);
        combo.setForeground(INK);
        combo.setBorder(BorderFactory.createLineBorder(GOLD, 1));
        combo.setPreferredSize(new Dimension(130, 28));
    }

    private void styleDateChooser(JDateChooser chooser) {
        chooser.setFont(FONT_LABEL);
        chooser.setForeground(INK);
        chooser.setBackground(PARCHMENT);
        chooser.setBorder(BorderFactory.createLineBorder(GOLD, 1));
        JTextField editor = (JTextField) chooser.getDateEditor().getUiComponent();
        editor.setBackground(PARCHMENT);
        editor.setForeground(INK);
        editor.setFont(FONT_LABEL);
        editor.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
    }

    /**
     * Themes the JTable to look like an antique ledger: parchment rows
     * with alternating shading, walnut/gold header, and warm gridlines,
     * while still letting a touch of the background show at the row edges.
     */
    private void makeTableThemed(JTable table) {
        table.setOpaque(false);
        table.setFont(FONT_LABEL);
        table.setForeground(INK);
        table.setShowGrid(true);
        table.setGridColor(GOLD.darker());
        table.setSelectionBackground(new Color(0xB0, 0x8D, 0x57, 160));
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
                    setBackground(new Color(0xB0, 0x8D, 0x57, 170));
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

    private void applyQuickFilter() {
        String choice = (String) cmbQuickFilter.getSelectedItem();
        LocalDate today = LocalDate.now();
        LocalDate from;

        switch (choice) {
            case "Today":
                from = today;
                break;
            case "This Week":
                from = today.minusDays(today.getDayOfWeek().getValue() - 1);
                break;
            case "This Month":
                from = today.withDayOfMonth(1);
                break;
            case "This Year":
                from = today.withDayOfYear(1);
                break;
            default: // Custom Range - user picks manually
                return;
        }

        fromDate.setDate(java.sql.Date.valueOf(from));
        toDate.setDate(java.sql.Date.valueOf(today));
    }

    private void generateReport() {

        if (fromDate.getDate() == null || toDate.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Please select both From and To dates!");
            return;
        }

        java.sql.Date from = new java.sql.Date(fromDate.getDate().getTime());
        java.sql.Date to = new java.sql.Date(toDate.getDate().getTime());

        model.setRowCount(0);
        int totalIssued = 0;
        double totalFine = 0;

        try {
            String sql = "SELECT * FROM issue WHERE issue_date BETWEEN ? AND ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setDate(1, from);
            pst.setDate(2, to);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("book_name"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getString("status"),
                        rs.getDate("return_date"),
                        "Rs." + rs.getDouble("fine_amount")
                });
                totalIssued++;
                totalFine += rs.getDouble("fine_amount");
            }

            rs.close();
            pst.close();

            lblSummary.setText(String.format(
                    "Report: %s to %s   |   Total Records: %d   |   Total Fine Collected: Rs.%.2f",
                    from, to, totalIssued, totalFine));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage());
        }
    }

    // Simple native Java print - no extra library needed
    private void printReport() {
        try {
            boolean complete = table.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat("Library Report"),
                    new java.text.MessageFormat("Page {0}"));
            if (complete) {
                JOptionPane.showMessageDialog(this, "Report sent to printer / saved as PDF!");
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + e.getMessage());
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
                // fallback plain background if image missing
                g.setColor(new Color(0xEF, 0xE3, 0xC8));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}