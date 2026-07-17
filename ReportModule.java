import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
 *
 * Wiring: AdminDashboard la oru menu add pannu:
 *   JMenuItem reportItem = new JMenuItem("Reports");
 *   menuBookmaneg.add(reportItem);
 *   reportItem.addActionListener(e -> { dispose(); new ReportModule(con, userid).setVisible(true); });
 */
public class ReportModule extends JFrame {

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

        JPanel main = new JPanel(new BorderLayout());
        setContentPane(main);

        // --- Filter Panel ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        filterPanel.add(new JLabel("Quick:"));
        cmbQuickFilter = new JComboBox<>(new String[]{
                "Custom Range", "Today", "This Week", "This Month", "This Year"
        });
        filterPanel.add(cmbQuickFilter);

        filterPanel.add(new JLabel("From:"));
        fromDate = new JDateChooser();
        fromDate.setDateFormatString("yyyy-MM-dd");
        fromDate.setPreferredSize(new Dimension(130, 28));
        filterPanel.add(fromDate);

        filterPanel.add(new JLabel("To:"));
        toDate = new JDateChooser();
        toDate.setDateFormatString("yyyy-MM-dd");
        toDate.setPreferredSize(new Dimension(130, 28));
        toDate.setDate(new java.util.Date());
        filterPanel.add(toDate);

        JButton btnGenerate = new JButton("Generate Report");
        btnGenerate.setBackground(new Color(0, 120, 215));
        btnGenerate.setForeground(Color.WHITE);
        filterPanel.add(btnGenerate);

        JButton btnPrint = new JButton("Print Report");
        btnPrint.setBackground(new Color(40, 167, 69));
        btnPrint.setForeground(Color.WHITE);
        filterPanel.add(btnPrint);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(con, userId).setVisible(true);
        });
        filterPanel.add(backBtn);

        main.add(filterPanel, BorderLayout.NORTH);

        // --- Table ---
        model = new DefaultTableModel(new Object[]{
                "Issue ID", "User ID", "Student Name", "Book Name",
                "Issue Date", "Due Date", "Status", "Return Date", "Fine"
        }, 0);
        table = new JTable(model);
        table.setRowHeight(26);
        main.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Summary bar ---
        lblSummary = new JLabel("Select a filter and click Generate Report");
        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSummary.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
        main.add(lblSummary, BorderLayout.SOUTH);

        // --- Quick filter behavior ---
        cmbQuickFilter.addActionListener(e -> applyQuickFilter());

        btnGenerate.addActionListener(e -> generateReport());
        btnPrint.addActionListener(e -> printReport());

        setVisible(true);
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
}
