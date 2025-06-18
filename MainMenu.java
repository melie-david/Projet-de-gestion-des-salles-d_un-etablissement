import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.UIManager;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("GESTION DES SALLES");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored){}

        setSize(1500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        ImageIcon icon=new ImageIcon("img9f0e.png");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Titre central
        JLabel title = new JLabel("GESTION DES SALLES D'UN ETABLISSEMENT", JLabel.CENTER);
        title.setFont(new Font("Cooper Black", Font.BOLD, 48));
        title.setForeground(new Color(25, 25, 112)); // bleu foncé
        title.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));

        add(title, BorderLayout.CENTER);

        // Menu
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(120, 30, 69));
        menuBar.setForeground(Color.BLUE);

        JMenu gestionMenu = new JMenu("Gestion des tables");
        gestionMenu.setForeground(Color.WHITE);
        gestionMenu.setBackground(new Color(45,45,45));
        gestionMenu.setFont(new Font("Times New Roman", Font.BOLD, 40));
        gestionMenu.setOpaque(true);

        JMenuItem salle = new JMenuItem("Salle");
        salle.setForeground(Color.WHITE);
        salle.setBackground(new Color(60,120,60));
        salle.setOpaque(true);
        salle.setFont(new Font("Times New Roman", Font.BOLD, 30));
        salle.addActionListener(e -> new SalleGUI()); // lancer Salle GUI

        JMenuItem classe = new JMenuItem("Classe");
        classe.setForeground(Color.WHITE);
        classe.setBackground(new Color(60,120,60));
        classe.setOpaque(true);
        classe.setFont(new Font("Times New Roman", Font.BOLD, 30));
        classe.addActionListener(e -> new ClasseGUI());

        JMenuItem enseignant = new JMenuItem("Enseignant");
        enseignant.setForeground(Color.WHITE);
        enseignant.setBackground(new Color(60,120,60));
        enseignant.setOpaque(true);
        enseignant.setFont(new Font("Times New Roman", Font.BOLD, 30));
        enseignant.addActionListener(e -> new EnseignantGUI());

        JMenuItem evenement = new JMenuItem("Événement");
        evenement.setForeground(Color.WHITE);
        evenement.setBackground(new Color(60,120,60));
        evenement.setOpaque(true);
        evenement.setFont(new Font("Times New Roman", Font.BOLD, 30));
        evenement.addActionListener(e -> new EvenementGUI());

        JMenuItem maintenance = new JMenuItem("Maintenance");
        maintenance.setForeground(Color.WHITE);
        maintenance.setBackground(new Color(60,120,60));
        maintenance.setOpaque(true);
        maintenance.setFont(new Font("Times New Roman", Font.BOLD, 30));
        maintenance.addActionListener(e -> new MaintenanceGUI());

        JMenuItem ue = new JMenuItem("UE");
        ue.setForeground(Color.WHITE);
        ue.setBackground(new Color(60,120,60));
        ue.setOpaque(true);
        ue.setFont(new Font("Times New Roman", Font.BOLD, 30));
        ue.addActionListener(e -> new UEGUI());

        JMenuItem utilisateurs = new JMenuItem("Utilisateur");
        utilisateurs.setForeground(Color.WHITE);
        utilisateurs.setBackground(new Color(60,120,60));
        utilisateurs.setOpaque(true);
        utilisateurs.setFont(new Font("Times New Roman", Font.BOLD, 30));
        utilisateurs.addActionListener(e -> new UtilisateursGUI());

        gestionMenu.add(salle);
        gestionMenu.add(classe);
        gestionMenu.add(enseignant);
        gestionMenu.add(evenement);
        gestionMenu.add(maintenance);
        gestionMenu.add(ue);
        gestionMenu.add(utilisateurs);


        JMenu gestionAssociation = new JMenu("Gestion des Relations");
        gestionAssociation.setForeground(Color.WHITE);
        gestionAssociation.setBackground(new Color(45,45,45));
        gestionAssociation.setFont(new Font("Times New Roman", Font.BOLD, 40));
        gestionAssociation.setOpaque(true);

        JMenuItem suivre = new JMenuItem("Suivre un cours");
        suivre.setForeground(Color.WHITE);
        suivre.setBackground(new Color(60,120,60));
        suivre.setOpaque(true);
        suivre.setFont(new Font("Times New Roman", Font.BOLD, 30));
        suivre.addActionListener(e -> new SuivreGUI()); // lancer Salle GUI

        JMenuItem occuper = new JMenuItem("Occuper ");
        occuper.setForeground(Color.WHITE);
        occuper.setBackground(new Color(60,120,60));
        occuper.setOpaque(true);
        occuper.setFont(new Font("Times New Roman", Font.BOLD, 30));
        occuper.addActionListener(e -> new OccuperGUI());

        JMenuItem derouler = new JMenuItem("Dérouler une cérémonie ");
        derouler.setForeground(Color.WHITE);
        derouler.setBackground(new Color(60,120,60));
        derouler.setOpaque(true);
        derouler.setFont(new Font("Times New Roman", Font.BOLD, 30));
        derouler.addActionListener(e -> new DeroulerGUI());

        JMenuItem effectuer = new JMenuItem("Effectuer une maintenance");
        effectuer.setForeground(Color.WHITE);
        effectuer.setBackground(new Color(60,120,60));
        effectuer.setOpaque(true);
        effectuer.setFont(new Font("Times New Roman", Font.BOLD, 30));
        effectuer.addActionListener(e -> new EffectuerGUI());

        JMenuItem dispenser = new JMenuItem(" Dispener un cours");
        dispenser.setForeground(Color.WHITE);
        dispenser.setBackground(new Color(60,120,60));
        dispenser.setOpaque(true);
        dispenser.setFont(new Font("Times New Roman", Font.BOLD, 30));
        dispenser.addActionListener(e -> new DispenserGUI());


        gestionAssociation.add(suivre);
        gestionAssociation.add(occuper);
        gestionAssociation.add(derouler);
        gestionAssociation.add(effectuer);
        gestionAssociation.add(dispenser);

        menuBar.add(gestionMenu);
        menuBar.add(gestionAssociation);

        setJMenuBar(menuBar);

        // Fond coloré
        getContentPane().setBackground(new Color(224, 235, 245));

        setVisible(true);
    }


    public static void main(String[] args) {
        //SwingUtilities.invokeLater(MainMenu::new);
        SwingUtilities.invokeLater(()->new MainMenu().setVisible(true));
    }
}
