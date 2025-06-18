import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

public class LoginGUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public static final String URL = "jdbc:postgresql://localhost:5432/gestion_salles";
    public static final String USER = "postgres";
    public static final String PASS = "123";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public LoginGUI() {
        setTitle("Login - Gestion de l'Établissement");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored){}

        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("Bienvenue", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 64));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        title.setBorder(new LineBorder(Color.BLUE,2,true));
        title.setOpaque(true);
        title.setBackground(new Color(0,102,204));

        add(title, BorderLayout.NORTH);

        // Main panel
        JPanel main = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(135, 206, 235),
                        0, getHeight(), new Color(25, 25, 112));

                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        main.setLayout(new GridBagLayout());
        main.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Serif", Font.BOLD, 28));
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        main.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Serif", Font.PLAIN, 28));
        gbc.gridx = 0;
        gbc.gridy = 1;
        main.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setFont(new Font("Serif", Font.BOLD, 28));
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        main.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Serif", Font.PLAIN, 28));
        gbc.gridx = 0;
        gbc.gridy = 3;
        main.add(passwordField, gbc);

        JButton loginBtn = new JButton("Se connecter");
        loginBtn.setFont(new Font("Serif", Font.BOLD, 18));
        loginBtn.setBackground(new Color(34, 139, 34));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setPreferredSize(new Dimension(200, 40));

        gbc.gridx = 0;
        gbc.gridy = 4;
        main.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> verifierLogin());

        add(main, BorderLayout.CENTER);
        setVisible(true);
    }

    private void verifierLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez renseigner tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM utilisateurs WHERE Username = ? AND PASSEWORD = ?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                dispose();
                new MainMenu();

            } else {
                JOptionPane.showMessageDialog(this, "Nom d'utilisateur ou mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginGUI::new);
    }
}
