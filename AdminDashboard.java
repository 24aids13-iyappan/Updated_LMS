import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.*;
public class AdminDashboard extends JFrame {
private Connection con;
private String userid;
    public AdminDashboard(Connection con, String userid) {
        this.con=con;
this.userid = userid;


        setTitle("Admin Dashboard");
        setSize(700, 500);
setMinimumSize(new Dimension(700,500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setResizable(false);
        

        
ImageIcon icon = new ImageIcon("bgphoto/Adash_bg.jpg");
Image img = icon.getImage().getScaledInstance(700,500,Image.SCALE_SMOOTH);
JLabel background = new JLabel(new ImageIcon(img));

background.setLayout(new GridBagLayout());

// Transparent overlay panel
JPanel overlay = new JPanel();
overlay.setBackground(new Color(0,0,0,120)); // transparency
overlay.setLayout(new GridBagLayout());

// Title
JLabel title = new JLabel("Welcome Admin!");
title.setFont(new Font("Segoe UI",Font.BOLD,32));
title.setForeground(Color.WHITE);

overlay.add(title);
background.add(overlay);

setContentPane(background);
JMenuBar menubar =new JMenuBar();
JMenu menuUser = new JMenu("Admin");
JMenuItem profile = new JMenuItem("Profile");
JMenuItem AddAdmin = new JMenuItem("Add Admin");
JMenuItem logout = new JMenuItem("Logout");
menuUser.add(profile);
menuUser.add(AddAdmin);
menuUser.add(logout);
JMenu menuBook = new JMenu("Book");
JMenuItem AddBook = new JMenuItem("Add Book");
JMenuItem ViewBook = new JMenuItem("View Book");

menuBook.add(AddBook);
menuBook.add(ViewBook);

JMenu menuBookmaneg = new JMenu("Book Management");

JMenuItem viewrequests = new JMenuItem("View Request");
JMenuItem issuebook = new JMenuItem("Issue Book");
JMenuItem returnBook = new JMenuItem("Return Book");
JMenuItem fine_check = new JMenuItem("Fine Book");
JMenuItem viewissue = new JMenuItem("view issue details");
JMenuItem reportItem = new JMenuItem("Reports"); 

JMenu menuStudents = new JMenu("Students");
JMenuItem viewStudents = new JMenuItem("View Students");

menuStudents.add(viewStudents);

menuBookmaneg.add(viewrequests);
menuBookmaneg.add(issuebook);
menuBookmaneg.add(returnBook);
menuBookmaneg.add(fine_check);
menuBookmaneg.add(viewissue);
menuBookmaneg.add(reportItem);

menubar.add(menuUser);
menubar.add(menuBook);
menubar.add(menuBookmaneg);
menubar.add(menuStudents);   // 👈 NEW LI

setJMenuBar(menubar);

/*JButton btn_AddUser = new JButton("Add Admin");
JButton btn_AddBook = new JButton("Add Book");
JButton btn_viewBook = new JButton("view Book");
JButton btn_viewrequests = new JButton("view requests");
JButton btn_issuebook = new JButton("issue book");
JButton btn_returnBook = new JButton("Return book");
JButton btn_fine_check = new JButton("fine_check");
JButton btn_viewissue = new JButton("view issue details");
JButton btn_logout = new JButton("log out");*/

profile.addActionListener(e -> {
    dispose();
    new AdminProfile(con, userid).setVisible(true); });

AddAdmin.addActionListener(e -> { dispose(); 
new AddAdminGUI(con, userid).setVisible(true);});

AddBook.addActionListener(e -> { dispose();
new AddBook(con, userid).setVisible(true);});

ViewBook.addActionListener(e -> { dispose();
new ViewBook(con, userid).setVisible(true);});

viewrequests.addActionListener(e -> { dispose();
new ManageRequests(con, userid).setVisible(true);});

issuebook.addActionListener(e -> { dispose();
new IssueBook(con, userid).setVisible(true);});

returnBook.addActionListener(e -> { dispose();
new ReturnBook(con, userid).setVisible(true);});

fine_check.addActionListener(e -> { dispose();
new FineModule(con, userid).setVisible(true);});

viewissue.addActionListener(e -> { dispose();
new ViewIssueBooks(con, userid).setVisible(true);});

viewStudents.addActionListener(e -> { 
    dispose(); 
    new ViewStudents(con, userid).setVisible(true);});

    reportItem.addActionListener(e -> { 
    dispose();
    new ReportModule(con, userid).setVisible(true);
});

logout.addActionListener(e -> { 
dispose();
new LoginGUI(con).setVisible(true);
});




/*setLayout(new FlowLayout(FlowLayout.CENTER,20,20));
add(btn_AddUser);
add(btn_AddBook);
add(btn_viewBook);
add(btn_viewrequests);
add(btn_issuebook);
add(btn_returnBook);
add(btn_fine_check);
add(btn_viewissue);
add(btn_logout);

*/
       
    }
}
