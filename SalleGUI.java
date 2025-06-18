import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

public class SalleGUI extends JFrame {

    private JTextField idSalleField, capaciteField, typeSalleField, searchField;
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

    public SalleGUI() {
        setTitle("Salles");

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

        idSalleField = new JTextField(10);
        capaciteField = new JTextField(5);
        typeSalleField = new JTextField(15);

        JButton addBtn = new JButton("➥ Ajouter");
        addBtn.setBackground(new Color(34, 139, 34));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Serif", Font.BOLD, 14));

        formPanel.add(new JLabel("ID Salle:"));
        formPanel.add(idSalleField);
        formPanel.add(new JLabel("Capacité:"));
        formPanel.add(capaciteField);
        formPanel.add(new JLabel("Type Salle:"));
        formPanel.add(typeSalleField);
        formPanel.add(addBtn);

        addBtn.addActionListener(e -> ajouterSalle());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        searchPanel.add(new JLabel("Recherche (ID ou Type):"));
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
                    // filtre sur colonne 0 (id_salle) ou 2 (type_salle)
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 2));
                }
            }
        });

        topPanel.add(formPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new String[]{"ID Salle", "Capacité", "Type Salle"}, 0) {
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
        modBtn.addActionListener(e -> modifierSalle());

        JButton delBtn = new JButton("🗑 Supprimer");
        delBtn.setBackground(new Color(220, 20, 60));
        delBtn.setForeground(Color.WHITE);
        delBtn.setFont(new Font("Serif", Font.BOLD, 14));
        delBtn.addActionListener(e -> supprimerSalle());

        south.add(modBtn);
        south.add(delBtn);

        add(south, BorderLayout.SOUTH);

        chargerSalles();

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
                        "Gestion des Salles\nVersion 1.0\n\nDéveloppé par Groupe 5",
                        "À propos",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        aideMenu.add(aproposItem);

        menuBar.add(fichierMenu);
        menuBar.add(aideMenu);

        setJMenuBar(menuBar);
    }

    private void ajouterSalle() {
        String id = idSalleField.getText().trim();
        String capStr = capaciteField.getText().trim();
        String type = typeSalleField.getText().trim();

        if (id.isEmpty() || capStr.isEmpty() || type.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int capacite;
        try {
            capacite = Integer.parseInt(capStr);
            if (capacite < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Capacité doit être un entier positif.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement psCheck = conn.prepareStatement("SELECT COUNT(*) FROM salle WHERE id_salle = ?");
            psCheck.setString(1, id);
            ResultSet rs = psCheck.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Cette salle existe déjà.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PreparedStatement ps = conn.prepareStatement("INSERT INTO salle (id_salle, capacite, type_salle) VALUES (?, ?, ?)");
            ps.setString(1, id);
            ps.setInt(2, capacite);
            ps.setString(3, type);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Salle ajoutée avec succès !");
            chargerSalles();

            idSalleField.setText("");
            capaciteField.setText("");
            typeSalleField.setText("");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierSalle() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);

            String id = (String) model.getValueAt(modelRow, 0);
            String newCapStr = JOptionPane.showInputDialog(this, "Nouvelle capacité:", model.getValueAt(modelRow, 1));
            if (newCapStr == null || newCapStr.trim().isEmpty()) return;

            String newType = JOptionPane.showInputDialog(this, "Nouveau type salle:", model.getValueAt(modelRow, 2));
            if (newType == null || newType.trim().isEmpty()) return;

            int newCap;
            try {
                newCap = Integer.parseInt(newCapStr.trim());
                if (newCap < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Capacité doit être un entier positif.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE salle SET capacite = ?, type_salle = ? WHERE id_salle = ?");
                ps.setInt(1, newCap);
                ps.setString(2, newType.trim());
                ps.setString(3, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Salle modifiée avec succès !");
                chargerSalles();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sélectionnez une salle à modifier.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void supprimerSalle() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            String id = (String) model.getValueAt(modelRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cette salle ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM salle WHERE id_salle = ?");
                    ps.setString(1, id);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Salle supprimée avec succès !");
                    chargerSalles();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sélectionnez une salle à supprimer.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void chargerSalles() {
        model.setRowCount(0);
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM salle ORDER BY id_salle");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("id_salle"),
                        rs.getInt("capacite"),
                        rs.getString("type_salle")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SalleGUI::new);
    }
}
