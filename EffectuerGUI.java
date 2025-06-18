import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EffectuerGUI extends JFrame {
    private Connection con;
    
    

    private JTextField txtIdMaintenance, txtIdSalle, txtDateEff, txtHdEff, txtHfEff;
    private JTable table;
    private DefaultTableModel model;

    public EffectuerGUI() {
        connectDB();

        setTitle("Gestion de la table Effectuer");

        // Look and Feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) { }

        setSize(800, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Actions");

        JMenuItem add = new JMenuItem("Ajouter");
        JMenuItem delete = new JMenuItem("Supprimer");
        JMenuItem clear = new JMenuItem("Effacer champs");

        menu.add(add);
        menu.add(delete);
        menu.add(clear);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        // Title
        JLabel title = new JLabel("Gestion de la table Effectuer", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));

        // Form
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblIdMaint = new JLabel("Id Maintenance : ");
        JLabel lblIdSalle = new JLabel("Id Salle : ");
        JLabel lblDateEff = new JLabel("Date (YYYY-MM-DD) : ");
        JLabel lblHdEff = new JLabel("Heure début (HH:MM:SS) : ");
        JLabel lblHfEff = new JLabel("Heure fin (HH:MM:SS) : ");

        txtIdMaintenance = new JTextField(20);
        txtIdSalle = new JTextField(20);
        txtDateEff = new JTextField(20);
        txtHdEff = new JTextField(20);
        txtHfEff = new JTextField(20);

        form.add(lblIdMaint);
        form.add(txtIdMaintenance);
        form.add(lblIdSalle);
        form.add(txtIdSalle);
        form.add(lblDateEff);
        form.add(txtDateEff);
        form.add(lblHdEff);
        form.add(txtHdEff);
        form.add(lblHfEff);
        form.add(txtHfEff);

        // Table
        model = new DefaultTableModel(new String[]{"Id Maintenance", "Id Salle", "Date", "Heure Début", "Heure Fin"}, 0);
        table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(184, 207, 229));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Données de la table Effectuer"));

        // Layout principal
        setLayout(new BorderLayout(20, 20));
        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);

        // Actions
        add.addActionListener(e -> ajouter());
        delete.addActionListener(e -> supprimer());
        clear.addActionListener(e -> clear());

        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                int i = table.getSelectedRow();
                if (i >= 0){
                    txtIdMaintenance.setText(model.getValueAt(i, 0).toString());
                    txtIdSalle.setText(model.getValueAt(i, 1).toString());
                    txtDateEff.setText(model.getValueAt(i, 2).toString());
                    txtHdEff.setText(model.getValueAt(i, 3).toString());
                    txtHfEff.setText(model.getValueAt(i, 4).toString());
                }
            }
        });

        loadData();

        setVisible(true);
    }

    private void connectDB(){
        try {
            Class.forName("org.postgresql.Driver");
            // Change these credentials to match your own
            con = DriverManager.getConnection(LoginGUI.URL, LoginGUI.USER, LoginGUI.PASS);

        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur connexion : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void loadData(){
        try {
            model.setRowCount(0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM effectuer ORDER BY id_maintenance");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id_maintenance"),
                        rs.getString("id_salle"),
                        rs.getDate("date_eff"),
                        rs.getTime("hd_eff"),
                        rs.getTime("hf_eff")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouter(){
        try {
            int idMaint = Integer.parseInt(txtIdMaintenance.getText().trim());
            String idSalle = txtIdSalle.getText().trim();
            Date dateEff = Date.valueOf(txtDateEff.getText().trim());
            Time hdEff = Time.valueOf(txtHdEff.getText().trim());
            Time hfEff = Time.valueOf(txtHfEff.getText().trim());

            PreparedStatement pst = con.prepareStatement("INSERT INTO effectuer (id_maintenance, id_salle, date_eff, hd_eff, hf_eff) VALUES (?, ?, ?, ?, ?)");
            pst.setInt(1, idMaint);
            pst.setString(2, idSalle);
            pst.setDate(3, dateEff);
            pst.setTime(4, hdEff);
            pst.setTime(5, hfEff);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Enregistrement ajouté avec succès!");
            clear();
            loadData();

        } catch (SQLException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Erreur d'ajout : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimer(){
        try {
            int idMaint = Integer.parseInt(txtIdMaintenance.getText().trim());
            String idSalle = txtIdSalle.getText().trim();
            Date dateEff = Date.valueOf(txtDateEff.getText().trim());

            PreparedStatement pst = con.prepareStatement("DELETE FROM effectuer WHERE id_maintenance = ? AND id_salle = ? AND date_eff = ?");
            pst.setInt(1, idMaint);
            pst.setString(2, idSalle);
            pst.setDate(3, dateEff);
            int res = pst.executeUpdate();

            if (res > 0) {
                JOptionPane.showMessageDialog(this, "Enregistrement supprimé avec succès!");
            } else {
                JOptionPane.showMessageDialog(this, "Aucun élément correspondant!");
            }
            clear();
            loadData();

        } catch (SQLException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Erreur de suppression : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clear(){
        txtIdMaintenance.setText("");
        txtIdSalle.setText("");
        txtDateEff.setText("");
        txtHdEff.setText("");
        txtHfEff.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EffectuerGUI::new);
    }
}
