import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DeroulerGUI extends JFrame {
    private Connection con;

    private JTextField txtIdEvenement, txtIdSalle, txtDateDer, txtHdDer, txtHfDer;
    private JTable table;
    private DefaultTableModel model;

    public DeroulerGUI() {
        connectDB();

        setTitle("Gestion de la table Derouler");

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

        JMenuItem add = new JMenuItem("Ajouter", new ImageIcon("add.png"));
        JMenuItem delete = new JMenuItem("Supprimer", new ImageIcon("delete.png"));
        JMenuItem clear = new JMenuItem("Effacer champs", new ImageIcon("clear.png"));
        JMenuItem quit = new JMenuItem("Quitter", new ImageIcon("exit.png"));

        menu.add(add);
        menu.add(delete);
        menu.add(clear);
        menu.add(quit);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        // Title
        JLabel title = new JLabel("Gestion de la table Derouler", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));

        // Form
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblIdEvent = new JLabel("Id Evenement : ");
        JLabel lblIdSalle = new JLabel("Id Salle : ");
        JLabel lblDateDer = new JLabel("Date (YYYY-MM-DD) : ");
        JLabel lblHdDer = new JLabel("Heure début (HH:MM:SS) : ");
        JLabel lblHfDer = new JLabel("Heure fin (HH:MM:SS) : ");

        txtIdEvenement = new JTextField(20);
        txtIdSalle = new JTextField(20);
        txtDateDer = new JTextField(20);
        txtHdDer = new JTextField(20);
        txtHfDer = new JTextField(20);

        form.add(lblIdEvent);
        form.add(txtIdEvenement);
        form.add(lblIdSalle);
        form.add(txtIdSalle);
        form.add(lblDateDer);
        form.add(txtDateDer);
        form.add(lblHdDer);
        form.add(txtHdDer);
        form.add(lblHfDer);
        form.add(txtHfDer);

        // Table
        model = new DefaultTableModel(new String[]{"Id Evenement", "Id Salle", "Date", "Heure Début", "Heure Fin"}, 0);
        table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(184, 207, 229));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Données de la table Derouler"));

        // Layout principal
        setLayout(new BorderLayout(20, 20));
        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);

        // Actions
        add.addActionListener(e -> ajouter());
        delete.addActionListener(e -> supprimer());
        clear.addActionListener(e -> clear());
        quit.addActionListener(e -> System.exit(0));

        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                int i = table.getSelectedRow();
                if (i >= 0){
                    txtIdEvenement.setText(model.getValueAt(i, 0).toString());
                    txtIdSalle.setText(model.getValueAt(i, 1).toString());
                    txtDateDer.setText(model.getValueAt(i, 2).toString());
                    txtHdDer.setText(model.getValueAt(i, 3).toString());
                    txtHfDer.setText(model.getValueAt(i, 4).toString());
                }
            }
        });

        loadData();

        setVisible(true);
    }
 
    private void connectDB(){
        try {
            Class.forName("org.postgresql.Driver");
            // Change credentials to match your settings
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
            ResultSet rs = st.executeQuery("SELECT * FROM derouler ORDER BY id_evenement");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id_evenement"),
                        rs.getString("id_salle"),
                        rs.getDate("date_der"),
                        rs.getTime("hd_der"),
                        rs.getTime("hf_der")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouter(){
        try {
            int idEvent = Integer.parseInt(txtIdEvenement.getText().trim());
            String idSalle = txtIdSalle.getText().trim();
            Date dateDer = Date.valueOf(txtDateDer.getText().trim());
            Time hdDer = Time.valueOf(txtHdDer.getText().trim());
            Time hfDer = Time.valueOf(txtHfDer.getText().trim());

            PreparedStatement pst = con.prepareStatement("INSERT INTO derouler (id_evenement, id_salle, date_der, hd_der, hf_der) VALUES (?, ?, ?, ?, ?)");
            pst.setInt(1, idEvent);
            pst.setString(2, idSalle);
            pst.setDate(3, dateDer);
            pst.setTime(4, hdDer);
            pst.setTime(5, hfDer);
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
            int idEvent = Integer.parseInt(txtIdEvenement.getText().trim());
            String idSalle = txtIdSalle.getText().trim();
            Date dateDer = Date.valueOf(txtDateDer.getText().trim());

            PreparedStatement pst = con.prepareStatement("DELETE FROM derouler WHERE id_evenement = ? AND id_salle = ? AND date_der = ?");
            pst.setInt(1, idEvent);
            pst.setString(2, idSalle);
            pst.setDate(3, dateDer);
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
        txtIdEvenement.setText("");
        txtIdSalle.setText("");
        txtDateDer.setText("");
        txtHdDer.setText("");
        txtHfDer.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DeroulerGUI::new);
    }
}
