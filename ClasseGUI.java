import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ClasseGUI extends JFrame {

    private JTextField effectifField;
    private JTextField filiereField;
    private JTextField niveauField;
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> rowSorter;

	/*
	 * private static final String URL =
	 * "jdbc:postgresql://localhost:5432/gestion_salles"; private static final
	 * String USER = "postgres"; private static final String PASS = "2000";
	 */
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

    public ClasseGUI(){
        setTitle("Classes");

        // Look and Feel Nimbus
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored){}

        setSize(1500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Icône de l'application
        ImageIcon icon = new ImageIcon("classe.png"); // chemin de l'icone
        setIconImage(icon.getImage());

        // Barre de menu
        createMenuBar();

        // Panel haut : Formulaire + Recherche
        JPanel topPanel = new JPanel(new BorderLayout(10,10));

        // Formulaire entrée données
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        effectifField = new JTextField(8);
        filiereField = new JTextField(15);
        niveauField = new JTextField(15);
        JButton addBtn = new JButton("➥ Ajouter");

        addBtn.setBackground(new Color(34, 139, 34)); // vert
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Serif", Font.BOLD, 14));

        formPanel.add(new JLabel("Effectif:"));
        formPanel.add(effectifField);
        formPanel.add(new JLabel("Filière:"));
        formPanel.add(filiereField);
        formPanel.add(new JLabel("Niveau:"));
        formPanel.add(niveauField);
        formPanel.add(addBtn);

        addBtn.addActionListener(e -> ajouterClasse());

        // Recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        searchPanel.add(new JLabel("Recherche filière:"));
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
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 2)); // col 2 = filiere
                }
            }
        });

        topPanel.add(formPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new String[]{"Id","Effectif","Filière","Niveau"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // désactive l'édition directe dans la table
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Serif", Font.PLAIN, 14));
        table.setRowHeight(30);

        // RowSorter pour filtrage
        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons bas
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton modBtn = new JButton("✏ Modifier");
        modBtn.setBackground(new Color(30, 144, 255)); // bleu
        modBtn.setForeground(Color.WHITE);
        modBtn.setFont(new Font("Serif", Font.BOLD, 14));
        modBtn.addActionListener(e -> modifierClasse());

        JButton delBtn = new JButton("🗑 Supprimer");
        delBtn.setBackground(new Color(220, 20, 60)); // rouge
        delBtn.setForeground(Color.WHITE);
        delBtn.setFont(new Font("Serif", Font.BOLD, 14));
        delBtn.addActionListener(e -> supprimerClasse());

        south.add(modBtn);
        south.add(delBtn);

        add(south, BorderLayout.SOUTH);

        chargerClasses();

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
                        "Gestion des Classes\nVersion 1.0\n\nDéveloppé par Groupe 5",
                        "À propos",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        aideMenu.add(aproposItem);

        menuBar.add(fichierMenu);
        menuBar.add(aideMenu);

        setJMenuBar(menuBar);
    }

    private void ajouterClasse(){
        String effectifStr = effectifField.getText();
        String filiere = filiereField.getText();
        String niveau = niveauField.getText();

        if (effectifStr.isEmpty() || filiere.isEmpty() || niveau.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Veuillez remplir tous les champs.","Erreur",JOptionPane.ERROR_MESSAGE);
            return;
        }
        int effectif;
        try {
            effectif = Integer.parseInt(effectifStr);
        } catch (NumberFormatException ex){
            JOptionPane.showMessageDialog(this,"Effectif doit être un nombre.","Erreur",JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO classe (effectif, filiere, niveau) VALUES (?, ?, ?)");
            ps.setInt(1, effectif);
            ps.setString(2, filiere);
            ps.setString(3, niveau);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,"Classe ajoutée avec succès!");
            chargerClasses();

            effectifField.setText("");
            filiereField.setText("");
            niveauField.setText("");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Erreur base de données.","Erreur",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierClasse(){
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            int id = (int)model.getValueAt(modelRow, 0);

            String effectifStr = JOptionPane.showInputDialog(this,"Nouveau Effectif :",model.getValueAt(modelRow, 1));
            String filiere = JOptionPane.showInputDialog(this,"Nouvelle Filière :",model.getValueAt(modelRow, 2));
            String niveau = JOptionPane.showInputDialog(this,"Nouveau Niveau :",model.getValueAt(modelRow, 3));

            if (effectifStr == null || filiere == null || niveau == null) return;

            int effectif;
            try {
                effectif = Integer.parseInt(effectifStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,"Effectif doit être un nombre.","Erreur",JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE classe SET effectif=?, filiere=?, niveau=? WHERE id_classe=?");
                ps.setInt(1, effectif);
                ps.setString(2, filiere);
                ps.setString(3, niveau);
                ps.setInt(4, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this,"Classe modifiée avec succès!");
                chargerClasses();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,"Erreur base de données.","Erreur",JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,"Sélectionnez une ligne à modifier.","Info",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void supprimerClasse(){
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            int id = (int)model.getValueAt(modelRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,"Supprimer cette classe ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM classe WHERE id_classe = ?");
                    ps.setInt(1, id);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this,"Classe supprimée avec succès!");
                    chargerClasses();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,"Erreur base de données.","Erreur",JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,"Sélectionnez une ligne à supprimer.","Info",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void chargerClasses(){
        model.setRowCount(0);
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM classe ORDER BY id_classe");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id_classe"),
                        rs.getInt("effectif"),
                        rs.getString("filiere"),
                        rs.getString("niveau")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Erreur base de données.","Erreur",JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClasseGUI::new);
    }
}
