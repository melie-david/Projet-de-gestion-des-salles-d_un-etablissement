import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class UEGUI extends JFrame {

    private JTextField idUEField;
    private JTextField intituleField;
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

    public UEGUI(){
        setTitle("UE");

        // Look and Feel Nimbus
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored){}

        setSize(1500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Barre de menu
        createMenuBar();

        // Panel haut : Formulaire + Recherche
        JPanel topPanel = new JPanel(new BorderLayout(10,10));

        // Formulaire entrée données
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        idUEField = new JTextField(15);
        intituleField = new JTextField(25);
        JButton addBtn = new JButton("➥ Ajouter");

        addBtn.setBackground(new Color(34, 139, 34));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Serif", Font.BOLD, 14));

        formPanel.add(new JLabel("ID UE:"));
        formPanel.add(idUEField);
        formPanel.add(new JLabel("Intitulé:"));
        formPanel.add(intituleField);
        formPanel.add(addBtn);

        addBtn.addActionListener(e -> ajouterUE());

        // Recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        searchPanel.add(new JLabel("Recherche intitulé:"));
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
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1)); // col 1 = intitule
                }
            }
        });

        topPanel.add(formPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new String[]{"ID UE", "Intitulé"}, 0) {
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
        modBtn.addActionListener(e -> modifierUE());

        JButton delBtn = new JButton("🗑 Supprimer");
        delBtn.setBackground(new Color(220, 20, 60));
        delBtn.setForeground(Color.WHITE);
        delBtn.setFont(new Font("Serif", Font.BOLD, 14));
        delBtn.addActionListener(e -> supprimerUE());

        south.add(modBtn);
        south.add(delBtn);

        add(south, BorderLayout.SOUTH);

        chargerUEs();

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
                        "Gestion des UE\nVersion 1.0\n\nDéveloppé par Groupe 5",
                        "À propos",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        aideMenu.add(aproposItem);

        menuBar.add(fichierMenu);
        menuBar.add(aideMenu);

        setJMenuBar(menuBar);
    }

    private void ajouterUE(){
        String idUE = idUEField.getText().trim();
        String intitule = intituleField.getText().trim();

        if (idUE.isEmpty() || intitule.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Veuillez remplir tous les champs.","Erreur",JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO ue (id_ue, intitule) VALUES (?, ?)");
            ps.setString(1, idUE);
            ps.setString(2, intitule);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,"UE ajoutée avec succès !");
            chargerUEs();

            idUEField.setText("");
            intituleField.setText("");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Erreur base de données : " + ex.getMessage(),"Erreur",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierUE(){
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            String idUE = (String) model.getValueAt(modelRow, 0);

            String newIntitule = JOptionPane.showInputDialog(this, "Nouvel intitulé :", model.getValueAt(modelRow, 1));
            if (newIntitule == null || newIntitule.trim().isEmpty()) return;

            try (Connection conn = getConnection()) {
                // Ne modifie que l'intitulé, pas la clé primaire id_ue
                PreparedStatement ps = conn.prepareStatement("UPDATE ue SET intitule = ? WHERE id_ue = ?");
                ps.setString(1, newIntitule);
                ps.setString(2, idUE);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "UE modifiée avec succès !");
                chargerUEs();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,"Sélectionnez une ligne à modifier.","Info",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void supprimerUE(){
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            String idUE = (String) model.getValueAt(modelRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cette UE ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM ue WHERE id_ue = ?");
                    ps.setString(1, idUE);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "UE supprimée avec succès !");
                    chargerUEs();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur base de données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,"Sélectionnez une ligne à supprimer.","Info",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void chargerUEs(){
        model.setRowCount(0);
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM ue ORDER BY id_ue");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getString("id_ue"),
                        rs.getString("intitule")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Erreur base de données.","Erreur",JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UEGUI::new);
    }
}
