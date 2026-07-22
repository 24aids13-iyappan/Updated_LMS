import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

/*
 * StudentDetailView - Admin ku oru specific student oda full detail kaata.
 * Also shows the student's currently issued books + fine summary.
 *
 * UPDATED: readable info card over the background photo, styled table,
 * styled rounded button, and a search bar to filter issued books by name.
 */
public class StudentDetailView extends JFrame {

    private Connection con;
    private String adminUserId;
    private String studentUserId;

    // Theme colors
    private static final Color NAVY        = new Color(31, 45, 74);
    private static final Color NAVY_LIGHT  = new Color(52, 73, 110);
    private static final Color GOLD        = new Color(178, 140, 74);
    private static final Color CARD_BG     = new Color(255, 255, 255, 215); // translucent card over bg photo
    private static final Color TABLE_ALT   = new Color(238, 241, 247);
    private static final Color TABLE_SEL   = new Color(201, 214, 235);

    private DefaultTableModel model;
    private Vector<Vector<Object>> allRows = new Vector<>();

    public StudentDetailView(Connection con, String adminUserId, String studentUserId) {

        this.con = con;
        this.adminUserId = adminUserId;
        this.studentUserId = studentUserId;

        setTitle("Student Detail - " + studentUserId);
        setSize(700, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        StudentDetailBackgroundPanel main = new StudentDetailBackgroundPanel("bgphoto/stdview.jpg");
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(main);

        JLabel title = new JLabel("Student Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(title);
        main.add(Box.createVerticalStrut(15));

        // ---- Info card (translucent white so it stays readable over the photo) ----
        JPanel infoCard = new JPanel(new GridLayout(0, 2, 12, 10));
        infoCard.setOpaque(true);
        infoCard.setBackground(CARD_BG);
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                new EmptyBorder(16, 18, 16, 18)));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 14);

        try {
            String sql = "SELECT * FROM users WHERE user_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, studentUserId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                addRow(infoCard, "User ID:", rs.getString("user_id"), labelFont, valueFont);
                addRow(infoCard, "Reg No:", rs.getString("reg_no"), labelFont, valueFont);
                addRow(infoCard, "Name:", rs.getString("name"), labelFont, valueFont);
                addRow(infoCard, "Department:", rs.getString("department"), labelFont, valueFont);

                int admissionYear = rs.getInt("admission_year");
                String currentYear = AcademicYearUtil.calculateYearOfStudy(admissionYear);
                addRow(infoCard, "Admission Year:", String.valueOf(admissionYear), labelFont, valueFont);
                addRow(infoCard, "Current Year:", currentYear, labelFont, valueFont);

                java.sql.Date dob = rs.getDate("dob");
                String dobStr = dob != null ? new SimpleDateFormat("dd-MM-yyyy").format(dob) : "N/A";
                addRow(infoCard, "DOB:", dobStr, labelFont, valueFont);

                addRow(infoCard, "Gender:", rs.getString("gender"), labelFont, valueFont);
                addRow(infoCard, "Email:", rs.getString("email"), labelFont, valueFont);
                addRow(infoCard, "Phone:", rs.getString("phone"), labelFont, valueFont);
                addRow(infoCard, "Username:", rs.getString("username"), labelFont, valueFont);
            } else {
                JLabel notFound = new JLabel("Student not found!");
                notFound.setForeground(Color.RED);
                infoCard.add(notFound);
            }
            rs.close();
            pst.close();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        main.add(infoCard);
        main.add(Box.createVerticalStrut(22));

        JLabel issuedTitle = new JLabel("Currently Issued Books");
        issuedTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        issuedTitle.setForeground(NAVY);
        issuedTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(issuedTitle);
        main.add(Box.createVerticalStrut(8));

        // ---- Search bar ----
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.setMaximumSize(new Dimension(640, 40));

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NAVY_LIGHT, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        searchField.putClientProperty("JTextField.placeholderText", "Search by book name...");

        JButton searchBtn = makeStyledButton("Search", NAVY, Color.WHITE);
        searchBtn.setPreferredSize(new Dimension(90, 34));

        JButton clearBtn = makeStyledButton("Clear", GOLD, Color.WHITE);
        clearBtn.setPreferredSize(new Dimension(80, 34));

        JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBtns.setOpaque(false);
        searchBtns.add(searchBtn);
        searchBtns.add(clearBtn);

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtns, BorderLayout.EAST);

        main.add(searchPanel);
        main.add(Box.createVerticalStrut(10));

        // ---- Table ----
        model = new DefaultTableModel(new Object[]{"Issue ID", "Book Name", "Due Date", "Status", "Fine"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALT);
                } else {
                    c.setBackground(TABLE_SEL);
                }
                return c;
            }
        };
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(TABLE_SEL);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(NAVY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(640, 200));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(GOLD, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        main.add(scroll);

        try {
            String sql2 = "SELECT issue_id, book_name, due_date, status, fine_amount FROM issue WHERE user_id=?";
            PreparedStatement pst2 = con.prepareStatement(sql2);
            pst2.setString(1, studentUserId);
            ResultSet rs2 = pst2.executeQuery();

            while (rs2.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs2.getInt("issue_id"));
                row.add(rs2.getString("book_name"));
                row.add(rs2.getDate("due_date"));
                row.add(rs2.getString("status"));
                row.add("Rs." + rs2.getDouble("fine_amount"));
                allRows.add(row);
                model.addRow(row);
            }
            rs2.close();
            pst2.close();

        } catch (Exception ex) {
            // table stays empty, no crash
        }

        // search behaviour
        Runnable doSearch = () -> {
            String term = searchField.getText().trim().toLowerCase();
            model.setRowCount(0);
            for (Vector<Object> row : allRows) {
                String bookName = String.valueOf(row.get(1)).toLowerCase();
                if (term.isEmpty() || bookName.contains(term)) {
                    model.addRow(row);
                }
            }
        };
        searchBtn.addActionListener(e -> doSearch.run());
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            doSearch.run();
        });
        searchField.addActionListener(e -> doSearch.run());

        main.add(Box.createVerticalStrut(22));

        JButton backBtn = makeStyledButton("Back to Student List", NAVY, Color.WHITE);
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setPreferredSize(new Dimension(200, 40));
        backBtn.setMaximumSize(new Dimension(200, 40));
        backBtn.addActionListener(e -> {
            dispose();
            new ViewStudents(con, adminUserId).setVisible(true);
        });
        main.add(backBtn);
    }

    private void addRow(JPanel panel, String label, String value, Font labelFont, Font valueFont) {
        JLabel l = new JLabel(label);
        l.setFont(labelFont);
        l.setForeground(NAVY);
        panel.add(l);

        JLabel v = new JLabel(value != null ? value : "N/A");
        v.setFont(valueFont);
        v.setForeground(Color.DARK_GRAY);
        panel.add(v);
    }

    /** Simple flat rounded button with hover feedback. */
    private JButton makeStyledButton(String text, Color base, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? base.brighter() : base;
                if (getModel().isPressed()) bg = base.darker();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { btn.repaint(); }
        });
        return btn;
    }
}

class StudentDetailBackgroundPanel extends JPanel {

    private Image bgImage;

    public StudentDetailBackgroundPanel(String imagePath) {
        bgImage = new ImageIcon(imagePath).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}