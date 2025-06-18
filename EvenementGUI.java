import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

public class EvenementGUI extends JFrame {

    private JTextField typeField;
    private JTextField nbreField;
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

    public EvenementGUI() {
        setTitle("Evénements");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        setSize(1500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        createMenuBar();

        // Formulaire + recherche
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        typeField = new JTextField(20);
        nbreField = new JTextField(7);
        JButton addBtn = new JButton("➥ Ajouter");

        addBtn.setBackground(new Color(34, 139, 34));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Serif", Font.BOLD, 14));

        formPanel.add(new JLabel("Type événement:"));
        formPanel.add(typeField);
        formPanel.add(new JLabel("Nb participants:"));
        formPanel.add(nbreField);
        formPanel.add(addBtn);

        addBtn.addActionListener(e -> ajouterEvenement());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        searchPanel.add(new JLabel("Recherche type:"));
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
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1)); // col 1 = type_evenement
                }
            }
        });

        topPanel.add(formPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new String[]{"ID", "Type événement", "Nb participants"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Serif", Font.PLAIN, 14));
        table.setRowHeight(28);

        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Boutons modifier et supprimer
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton modBtn = new JButton("✏ Modifier");
        modBtn.setBackground(new Color(30, 144, 255));
        modBtn.setForeground(Color.WHITE);
        modBtn.setFont(new Font("Serif", Font.BOLD, 14));
        modBtn.addActionListener(e -> modifierEvenement());

        JButton delBtn = new JButton("🗑 Supprimer");
        delBtn.setBackground(new Color(220, 20, 60));
        delBtn.setForeground(Color.WHITE);
        delBtn.setFont(new Font("Serif", Font.BOLD, 14));
        delBtn.addActionListener(e -> supprimerEvenement());

        south.add(modBtn);
        south.add(delBtn);

        add(south, BorderLayout.SOUTH);

        chargerEvenements();

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
                        "Gestion des événements\nVersion 1.0\n\nDéveloppé par Groupe 5",
                        "À propos",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        aideMenu.add(aproposItem);

        menuBar.add(fichierMenu);
        menuBar.add(aideMenu);

        setJMenuBar(menuBar);
    }

    private void ajouterEvenement() {
        String type = typeField.getText().trim();
        String nbreStr = nbreField.getText().trim();

        if (type.isEmpty() || nbreStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int nbre;
        try {
            nbre = Integer.parseInt(nbreStr);
            if (nbre < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Le nombre de participants doit être un entier positif.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO evenement (type_evenement, nbre_participants) VALUES (?, ?)");
            ps.setString(1, type);
            ps.setInt(2, nbre);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Événement ajouté avec succès !");
            chargerEvenements();

            typeField.setText("");
            nbreField.setText("");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierEvenement() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            int id = (int) model.getValueAt(modelRow, 0);

            String newType = JOptionPane.showInputDialog(this, "Nouveau type d'événement :", model.getValueAt(modelRow, 1));
            if (newType == null || newType.trim().isEmpty()) return;

            String newNbreStr = JOptionPane.showInputDialog(this, "Nouveau nombre de participants :", model.getValueAt(modelRow, 2));
            if (newNbreStr == null || newNbreStr.trim().isEmpty()) return;

            int newNbre;
            try {
                newNbre = Integer.parseInt(newNbreStr);
                if (newNbre < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Le nombre de participants doit être un entier positif.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE evenement SET type_evenement = ?, nbre_participants = ? WHERE id_evenement = ?");
                ps.setString(1, newType);
                ps.setInt(2, newNbre);
                ps.setInt(3, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Événement modifié avec succès !");
                chargerEvenements();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sélectionnez une ligne à modifier.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void supprimerEvenement() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            int id = (int) model.getValueAt(modelRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cet événement ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM evenement WHERE id_evenement = ?");
                    ps.setInt(1, id);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Événement supprimé avec succès !");
                    chargerEvenements();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sélectionnez une ligne à supprimer.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void chargerEvenements() {
        model.setRowCount(0);
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM evenement ORDER BY id_evenement");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id_evenement"),
                        rs.getString("type_evenement"),
                        rs.getInt("nbre_participants")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EvenementGUI::new);
    }
}
