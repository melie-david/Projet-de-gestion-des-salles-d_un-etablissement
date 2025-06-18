import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class EnseignantGUI extends JFrame {

    private JTextField matriculeField;
    private JTextField nomField;
    private JTextField searchField;
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

    public EnseignantGUI(){
        setTitle("Enseignants");

        // Look and Feel Nimbus
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored){}

        setSize(1500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Icône (optionnel)
        // ImageIcon icon = new ImageIcon("enseignant.png"); // mettre ton icône ici si tu veux
        // setIconImage(icon.getImage());

        // Barre de menu
        createMenuBar();

        // Panel haut : Formulaire + Recherche
        JPanel topPanel = new JPanel(new BorderLayout(10,10));

        // Formulaire entrée données
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        matriculeField = new JTextField(15);
        nomField = new JTextField(20);
        JButton addBtn = new JButton("➥ Ajouter");

        addBtn.setBackground(new Color(34, 139, 34));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Serif", Font.BOLD, 14));

        formPanel.add(new JLabel("Matricule:"));
        formPanel.add(matriculeField);
        formPanel.add(new JLabel("Nom:"));
        formPanel.add(nomField);
        formPanel.add(addBtn);

        addBtn.addActionListener(e -> ajouterEnseignant());

        // Recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        searchPanel.add(new JLabel("Recherche nom:"));
        searchField = new JTextField(20);
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
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1)); // col 1 = nom_enseignant
                }
            }
        });

        topPanel.add(formPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new String[]{"Matricule", "Nom Enseignant"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Serif", Font.PLAIN, 14));
        table.setRowHeight(28);

        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons bas
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton modBtn = new JButton("✏ Modifier");
        modBtn.setBackground(new Color(30, 144, 255));
        modBtn.setForeground(Color.WHITE);
        modBtn.setFont(new Font("Serif", Font.BOLD, 14));
        modBtn.addActionListener(e -> modifierEnseignant());

        JButton delBtn = new JButton("🗑 Supprimer");
        delBtn.setBackground(new Color(220, 20, 60));
        delBtn.setForeground(Color.WHITE);
        delBtn.setFont(new Font("Serif", Font.BOLD, 14));
        delBtn.addActionListener(e -> supprimerEnseignant());

        south.add(modBtn);
        south.add(delBtn);

        add(south, BorderLayout.SOUTH);

        chargerEnseignants();

        setVisible(true);
    }

    private void createMenuBar(){
        JMenuBar menuBar = new JMenuBar();

        JMenu fichierMenu = new JMenu("Fichier");
        JMenuItem quitterItem = new JMenuItem("Quitter");
        quitterItem.addActionListener(e -> System.exit(0));
        fichierMenu.add(quitterItem);

        JMenu aideMenu = new JMenu("Aide");
        JMenuItem aproposItem = new JMenuItem("À propos");
        aproposItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Gestion des Enseignants\nVersion 1.0\n\nDéveloppé par Groupe 5",
                        "À propos",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        aideMenu.add(aproposItem);

        menuBar.add(fichierMenu);
        menuBar.add(aideMenu);

        setJMenuBar(menuBar);
    }

    private void ajouterEnseignant(){
        String matricule = matriculeField.getText().trim();
        String nom = nomField.getText().trim();

        if (matricule.isEmpty() || nom.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Veuillez remplir tous les champs.","Erreur",JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO enseignant (matricule, nom_enseignant) VALUES (?, ?)");
            ps.setString(1, matricule);
            ps.setString(2, nom);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,"Enseignant ajouté avec succès !");
            chargerEnseignants();

            matriculeField.setText("");
            nomField.setText("");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Erreur base de données : " + ex.getMessage(),"Erreur",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierEnseignant(){
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            String matricule = (String) model.getValueAt(modelRow, 0);

            String newMatricule = JOptionPane.showInputDialog(this, "Nouveau matricule :", matricule);
            if (newMatricule == null || newMatricule.trim().isEmpty()) return;

            String nom = (String) model.getValueAt(modelRow, 1);
            String newNom = JOptionPane.showInputDialog(this, "Nouveau nom :", nom);
            if (newNom == null || newNom.trim().isEmpty()) return;

            try (Connection conn = getConnection()) {
                // Attention : la clé primaire matricule ne doit pas changer pour simplifier. Sinon il faut gérer ça différemment.
                PreparedStatement ps = conn.prepareStatement("UPDATE enseignant SET nom_enseignant = ? WHERE matricule = ?");
                ps.setString(1, newNom);
                ps.setString(2, matricule);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Enseignant modifié avec succès !");
                chargerEnseignants();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,"Sélectionnez une ligne à modifier.","Info",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void supprimerEnseignant(){
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            String matricule = (String) model.getValueAt(modelRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cet enseignant ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM enseignant WHERE matricule = ?");
                    ps.setString(1, matricule);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Enseignant supprimé avec succès !");
                    chargerEnseignants();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,"Sélectionnez une ligne à supprimer.","Info",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void chargerEnseignants(){
        model.setRowCount(0);
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM enseignant ORDER BY matricule");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getString("matricule"),
                        rs.getString("nom_enseignant")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Erreur base de données.","Erreur",JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnseignantGUI::new);
    }
}
