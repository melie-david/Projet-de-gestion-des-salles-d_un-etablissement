import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class SuivreGUI extends JFrame {
    private Connection con;

    private JTextField txtIdClasse, txtIdUE;
    private JTable table;
    private DefaultTableModel model;

    public SuivreGUI() {
        connectDB();

        setTitle("Gestion de la table Suivre");

        // Look and Feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) { }

        setSize(800, 730);
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
        JLabel title = new JLabel("Gestion de la table Suivre", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));

        // Form
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblIdClass = new JLabel("Id Classe : ");
        lblIdClass.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        JLabel lblIdUE = new JLabel("Id UE : ");

        txtIdClasse = new JTextField(20);
        txtIdUE = new JTextField(20);

        form.add(lblIdClass);
        form.add(txtIdClasse);
        form.add(lblIdUE);
        form.add(txtIdUE);

        // Table
        model = new DefaultTableModel(new String[]{"Id Classe", "Id UE"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(184, 207, 229));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Données de la table Suivre"));

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
                    txtIdClasse.setText(model.getValueAt(i, 0).toString());
                    txtIdUE.setText(model.getValueAt(i, 1).toString());
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
            ResultSet rs = st.executeQuery("SELECT * FROM suivre ORDER BY id_classe");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id_classe"),
                        rs.getString("id_ue")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouter(){
        try {
            int idClasse = Integer.parseInt(txtIdClasse.getText().trim());
            String idUE = txtIdUE.getText().trim();

            PreparedStatement pst = con.prepareStatement("INSERT INTO suivre (id_classe, id_ue) VALUES (?, ?)");
            pst.setInt(1, idClasse);
            pst.setString(2, idUE);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Enregistrement ajouté avec succès !");
            clear();
            loadData();

        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erreur d'ajout : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimer(){
        try {
            int idClasse = Integer.parseInt(txtIdClasse.getText().trim());
            String idUE = txtIdUE.getText().trim();

            PreparedStatement pst = con.prepareStatement("DELETE FROM suivre WHERE id_classe = ? AND id_ue = ?");
            pst.setInt(1, idClasse);
            pst.setString(2, idUE);
            int res = pst.executeUpdate();

            if (res > 0) {
                JOptionPane.showMessageDialog(this, "Enregistrement supprimé avec succès!");
            } else {
                JOptionPane.showMessageDialog(this, "Aucun élément correspondant!");
            }
            clear();
            loadData();

        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erreur de suppression : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clear(){
        txtIdClasse.setText("");
        txtIdUE.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SuivreGUI::new);
    }
}
