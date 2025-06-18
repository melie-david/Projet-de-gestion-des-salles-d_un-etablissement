import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

public class MaintenanceGUI extends JFrame {

    private JTextField rupeField;
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

    public MaintenanceGUI() {
        setTitle("Maintenances");

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
        rupeField = new JTextField(30);
        JButton addBtn = new JButton("➥ Ajouter");

        addBtn.setBackground(new Color(34, 139, 34));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Serif", Font.BOLD, 14));

        formPanel.add(new JLabel("Type Maintenance:"));
        formPanel.add(rupeField);
        formPanel.add(addBtn);

        addBtn.addActionListener(e -> ajouterMaintenance());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        searchPanel.add(new JLabel("Recherche Maintenance:"));
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
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1)); // col 1 = rupe_evenement
                }
            }
        });

        topPanel.add(formPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new String[]{"ID", "Type Maintenance"}, 0) {
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
        modBtn.addActionListener(e -> modifierMaintenance());

        JButton delBtn = new JButton("🗑 Supprimer");
        delBtn.setBackground(new Color(220, 20, 60));
        delBtn.setForeground(Color.WHITE);
        delBtn.setFont(new Font("Serif", Font.BOLD, 14));
        delBtn.addActionListener(e -> supprimerMaintenance());

        south.add(modBtn);
        south.add(delBtn);

        add(south, BorderLayout.SOUTH);

        chargerMaintenances();

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
                        "Gestion des maintenances\nVersion 1.0\n\nDéveloppé par Groupe 5",
                        "À propos",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        aideMenu.add(aproposItem);

        menuBar.add(fichierMenu);
        menuBar.add(aideMenu);

        setJMenuBar(menuBar);
    }

    private void ajouterMaintenance() {
        String rupe = rupeField.getText().trim();

        if (rupe.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir le Type maintenanace.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO maintenance (type_maintenance) VALUES (?)");
            ps.setString(1, rupe);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Maintenance ajoutée avec succès !");
            chargerMaintenances();

            rupeField.setText("");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierMaintenance() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            int id = (int) model.getValueAt(modelRow, 0);

            String newRupe = JOptionPane.showInputDialog(this, "Nouveau Type maintenance :", model.getValueAt(modelRow, 1));
            if (newRupe == null || newRupe.trim().isEmpty()) return;

            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE maintenance SET type_maintenance = ? WHERE id_maintenance = ?");
                ps.setString(1, newRupe);
                ps.setInt(2, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Maintenance modifiée avec succès !");
                chargerMaintenances();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sélectionnez une ligne à modifier.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void supprimerMaintenance() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            int id = (int) model.getValueAt(modelRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cette maintenance ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM maintenance WHERE id_maintenance = ?");
                    ps.setInt(1, id);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Maintenance supprimée avec succès !");
                    chargerMaintenances();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sélectionnez une ligne à supprimer.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void chargerMaintenances() {
        model.setRowCount(0);
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM maintenance ORDER BY id_maintenance");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id_maintenance"),
                        rs.getString("type_maintenance")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MaintenanceGUI::new);
    }
}
