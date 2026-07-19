import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
 
public class UserDashboard extends JFrame
{
 
private String userid;
private Connection con;
private JTextField txtAISearch;
 
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
 
// GOOGLE-STYLE AI SEARCH BOX
txtAISearch = new JTextField(35);
txtAISearch.setFont(new Font("Segoe UI", Font.PLAIN, 18));
txtAISearch.setMaximumSize(new Dimension(500, 45));
txtAISearch.setAlignmentX(Component.CENTER_ALIGNMENT);
txtAISearch.setToolTipText("Ask AI: e.g. 'book to learn programming basics'");
 
JButton btnAISearch = new JButton("Search with AI");
btnAISearch.setFont(new Font("Segoe UI", Font.BOLD, 15));
btnAISearch.setBackground(new Color(150, 50, 200));
btnAISearch.setForeground(Color.WHITE);
btnAISearch.setFocusPainted(false);
btnAISearch.setAlignmentX(Component.CENTER_ALIGNMENT);
btnAISearch.setMaximumSize(new Dimension(220, 40));
 
// Title
JLabel title = new JLabel("Welcome User!");
title.setFont(new Font("Segoe UI",Font.BOLD,32));
title.setForeground(Color.WHITE);
title.setAlignmentX(Component.CENTER_ALIGNMENT);
 
overlay.add(Box.createVerticalStrut(30));
overlay.add(title);
overlay.add(Box.createVerticalStrut(25));
overlay.add(txtAISearch);
overlay.add(Box.createVerticalStrut(10));
overlay.add(btnAISearch);
overlay.add(Box.createVerticalStrut(20));
 
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
JMenuItem menurequest = new JMenuItem("Request Book");
JMenuItem menuborrow = new JMenuItem("My borrowed Books");
JMenuItem menufinebook = new JMenuItem("Fine Details");
 
menubook.add(menusearchbook);
menubook.add(menurequest);
menubook.add(menuborrow);
menubook.add(menufinebook);
 
menubar.add(menuUser);
menubar.add(menubook);
 
setJMenuBar(menubar);
 
menusearchbook.addActionListener(e -> { dispose();new BookCatalog(con,userid).setVisible(true);});
 
menurequest.addActionListener(e ->{ dispose(); new RequestBook(con,userid).setVisible(true);});
 
menufinebook.addActionListener(e -> { dispose();new ViewFineUser(con,userid).setVisible(true);});
 
menuuserprofile.addActionListener(e -> { dispose();new UserProfile(con,userid).setVisible(true);});
 
menuborrow.addActionListener(e -> { dispose();new BorrowedBooks(con,userid).setVisible(true);});
 
btnAISearch.addActionListener(e -> openAiChat());
 
logout.addActionListener(e -> {
dispose();
new LoginGUI(con).setVisible(true);
});
 
    }
 
    // Opens the chat-style AI assistant window.
    // If the user already typed something in the dashboard search box,
    // that query is sent automatically as the first chat message.
    private void openAiChat() {
        String query = txtAISearch.getText().trim();
        dispose();
        new ChatAISearchView(con, userid, query.isEmpty() ? null : query).setVisible(true);
    }
}