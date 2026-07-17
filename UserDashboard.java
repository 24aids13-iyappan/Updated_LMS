import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDashboard extends JFrame 
{

private String userid;
private Connection con;
private JTextArea txtresult;

    public UserDashboard(Connection con,String userid) 
    {
this.con = con;
this.userid=userid;

        setTitle("User Dashboard");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
ImageIcon icon = new ImageIcon("bgphoto/Udash_bg.jpg");
Image img = icon.getImage().getScaledInstance(700,500,Image.SCALE_SMOOTH);
JLabel background = new JLabel(new ImageIcon(img));

background.setLayout(new GridBagLayout());

// Transparent overlay panel
JPanel overlay = new JPanel();
overlay.setBackground(new Color(0,0,0,120)); // transparency
overlay.setLayout(new BoxLayout(overlay,BoxLayout.Y_AXIS));

JTextField txtsearch = new JTextField(25);
JButton btnsearch =new JButton("Search");
txtresult = new JTextArea(10,35);
txtresult.setEditable(false);
JScrollPane scroll = new JScrollPane(txtresult);

// Title
JLabel title = new JLabel("Welcome User!");
title.setFont(new Font("Segoe UI",Font.BOLD,32));
title.setForeground(Color.WHITE);
title.setAlignmentX(Component.CENTER_ALIGNMENT);
txtsearch.setMaximumSize(new Dimension(350,30));
btnsearch.setAlignmentX(Component.CENTER_ALIGNMENT);

overlay.add(Box.createVerticalStrut(20));
overlay.add(title);
overlay.add(Box.createVerticalStrut(20));
overlay.add(txtsearch);

overlay.add(Box.createVerticalStrut(20));
overlay.add(btnsearch);

overlay.add(Box.createVerticalStrut(20));
overlay.add(scroll);

background.add(overlay);

setContentPane(background);

JMenuBar menubar =new JMenuBar();
JMenu menuUser = new JMenu("User");
JMenuItem menuuserprofile = new JMenuItem("Profile");
JMenuItem logout = new JMenuItem("Logout");

menuUser.add(menuuserprofile);
menuUser.add(logout);

JMenu menubook = new JMenu("Books");
JMenuItem menusearchbook = new JMenuItem("Search Book");
//JMenuItem menuviewbook = new JMenuItem("view total book");

JMenuItem menurequest = new JMenuItem("Request Book");
JMenuItem menuborrow = new JMenuItem("My borrowed Books");
JMenuItem menufinebook = new JMenuItem("Fine Details");


menubook.add(menusearchbook);
//menubook.add(menuviewbook);
menubook.add(menurequest);
menubook.add(menuborrow);
menubook.add(menufinebook);



menubar.add(menuUser);
menubar.add(menubook);

setJMenuBar(menubar);




/*JButton btnsearchbook = new JButton("SearchBook");
JButton btnrequest = new JButton("Request Book");
JButton btnfinebook = new JButton("view my fine");
JButton btnuserprofile = new JButton("user profile");
JButton btn_logout = new JButton("log out");*/


menusearchbook.addActionListener(e -> { dispose();new BookCatalog(con,userid).setVisible(true);});

menurequest.addActionListener(e ->{ dispose(); new RequestBook(con,userid).setVisible(true);});

menufinebook.addActionListener(e -> { dispose();new ViewFineUser(con,userid).setVisible(true);});

menuuserprofile.addActionListener(e -> { dispose();new UserProfile(con,userid).setVisible(true);});

menuborrow.addActionListener(e -> { dispose();new BorrowedBooks(con,userid).setVisible(true);});

//menuviewbook.addActionListener(e -> { dispose();new BookCatalog(con,userid).setVisible(true);});

btnsearch.addActionListener(e ->{
    String query = txtsearch.getText().toLowerCase();
    String category = txtsearch.getText().trim();
    searchBooks(category);
});
logout.addActionListener(e -> { 
dispose();
new LoginGUI(con).setVisible(true);
});




/*setLayout(new FlowLayout(FlowLayout.CENTER,20,20));
add(btnsearchbook);
add(btnrequest);
add(btnfinebook);
add(btnuserprofile);
add(btn_logout);
*/
        
    }
    private void searchBooks(String category)
{
String sql ="SELECT b.title, b.author " + "FROM books b INNER JOIN book_ai_info a " + "ON b.book_id = a.book_id " + "WHERE a.category = ? ";

try 
{
    PreparedStatement ps = con.prepareStatement(sql);
    ps.setString(1, category);
     ResultSet rs =ps.executeQuery();
     StringBuilder result = new StringBuilder();
     while(rs.next())
     {
        result.append("Book:").append(rs.getString("title")).append("\nAuthor:").append(rs.getString("author")).append("\n\n");
     }
     txtresult.setText(result.toString());
}
catch(Exception ex)
{
    JOptionPane.showMessageDialog(this,"Error: " + ex.getMessage());
}
}

}
