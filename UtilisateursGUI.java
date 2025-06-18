import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

public class UtilisateursGUI extends JFrame {

    private JTextField passewordField, usernameField, useremailField, searchField;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> rowSorter;


    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(LoginGUI.URL, LoginGUI.USER, LoginGUI.PASS);
    }

    public UtilisateursGUI() {
        setTitle("Utilisateurs");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        setSize(1500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        createMenuBar();

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        passewordField = new JTextField(12);
        usernameField = new JTextField(12);
        useremailField = new JTextField(15);

        JButton addBtn = new JButton("➥ Ajouter");
        addBtn.setBackground(new Color(34, 139, 34));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Serif", Font.BOLD, 14));

        formPanel.add(new JLabel("PASSEWORD (PK):"));
        formPanel.add(passewordField);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Useremail:"));
        formPanel.add(useremailField);
        formPanel.add(addBtn);

        addBtn.addActionListener(e -> ajouterUtilisateur());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        searchPanel.add(new JLabel("Recherche (PASSEWORD, Username, Email):"));
        searchField = new JTextField(25);
        searchPanel.add(searchField);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1, 2));
                }
            }
        });

        topPanel.add(formPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"PASSEWORD", "Username", "Useremail"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Serif", Font.PLAIN, 14));
        table.setRowHeight(28);

        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton modBtn = new JButton("✏ Modifier");
        modBtn.setBackground(new Color(30, 144, 255));
        modBtn.setForeground(Color.WHITE);
        modBtn.setFont(new Font("Serif", Font.BOLD, 14));
        modBtn.addActionListener(e -> modifierUtilisateur());

        JButton delBtn = new JButton("🗑 Supprimer");
        delBtn.setBackground(new Color(220, 20, 60));
        delBtn.setForeground(Color.WHITE);
        delBtn.setFont(new Font("Serif", Font.BOLD, 14));
        delBtn.addActionListener(e -> supprimerUtilisateur());

        south.add(modBtn);
        south.add(delBtn);

        add(south, BorderLayout.SOUTH);

        chargerUtilisateurs();

        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fichierMenu = new JMenu("Fichier");
        JMenuItem quitterItem = new JMenuItem("Quitter");
        quitterItem.addActionListener(e -> System.exit(0));
        fichierMenu.add(quitterItem);

        JMenu aideMenu = new JMenu("Aide");
        JMenuItem aproposItem = new JMenuItem("À propos");
        aproposItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Gestion des Utilisateurs\nVersion 1.0\n\nDéveloppé par Groupe5",
                        "À propos",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        aideMenu.add(aproposItem);

        menuBar.add(fichierMenu);
        menuBar.add(aideMenu);

        setJMenuBar(menuBar);
    }

    private void ajouterUtilisateur() {
        String passeword = passewordField.getText().trim();
        String username = usernameField.getText().trim();
        String email = useremailField.getText().trim();

        if (passeword.isEmpty() || username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement psCheck = conn.prepareStatement("SELECT COUNT(*) FROM utilisateurs WHERE PASSEWORD = ?");
            psCheck.setString(1, passeword);
            ResultSet rs = psCheck.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Cet utilisateur existe déjà (même PASSEWORD).", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PreparedStatement ps = conn.prepareStatement("INSERT INTO utilisateurs (PASSEWORD, username, useremail) VALUES (?, ?, ?)");
            ps.setString(1, passeword);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Utilisateur ajouté avec succès !");
            chargerUtilisateurs();

            passewordField.setText("");
            usernameField.setText("");
            useremailField.setText("");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierUtilisateur() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);

            String passeword = (String) model.getValueAt(modelRow, 0);

            String newUsername = JOptionPane.showInputDialog(this, "Nouveau Username:", model.getValueAt(modelRow, 1));
            if (newUsername == null || newUsername.trim().isEmpty()) return;

            String newEmail = JOptionPane.showInputDialog(this, "Nouvel Email:", model.getValueAt(modelRow, 2));
            if (newEmail == null || newEmail.trim().isEmpty()) return;

            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE utilisateurs SET username = ?, useremail = ? WHERE PASSEWORD = ?");
                ps.setString(1, newUsername.trim());
                ps.setString(2, newEmail.trim());
                ps.setString(3, passeword);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Utilisateur modifié avec succès !");
                chargerUtilisateurs();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sélectionnez un utilisateur à modifier.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void supprimerUtilisateur() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            String passeword = (String) model.getValueAt(modelRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cet utilisateur ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM utilisateurs WHERE PASSEWORD = ?");
                    ps.setString(1, passeword);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Utilisateur supprimé avec succès !");
                    chargerUtilisateurs();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sélectionnez un utilisateur à supprimer.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void chargerUtilisateurs() {
        model.setRowCount(0);
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM utilisateurs ORDER BY PASSEWORD");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("PASSEWORD"),
                        rs.getString("username"),
                        rs.getString("useremail")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UtilisateursGUI::new);
    }
}
