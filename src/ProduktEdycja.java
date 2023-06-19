import javax.swing.*;
import java.awt.*;

public class ProduktEdycja extends JDialog {
    private Produkt produkt;
    private JTextField textFieldNazwa;
    private JTextField textFieldCena;
    private int userId;

    public ProduktEdycja(Frame owner, Produkt produkt, int userId) {
        super(owner, "Edytuj Produkt", true);
        this.produkt = produkt;
        this.userId = userId;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));

        panel.add(new JLabel("Nazwa:"));
        textFieldNazwa = new JTextField(produkt.getNazwa());
        panel.add(textFieldNazwa);

        panel.add(new JLabel("Cena:"));
        textFieldCena = new JTextField(Double.toString(produkt.getCena()));
        panel.add(textFieldCena);

        JButton buttonZapisz = new JButton("Zapisz");
        buttonZapisz.addActionListener(e -> zapiszProdukt());
        panel.add(buttonZapisz);

        add(panel);
        pack();
    }

    private void zapiszProdukt() {
        produkt.setNazwa(textFieldNazwa.getText());
        produkt.setCena(Double.parseDouble(textFieldCena.getText()));

        if (produkt.getId() == 0) {
            ProduktDAO.dodajProdukt(produkt, userId);
        } else {
            ProduktDAO.aktualizujProdukt(produkt, userId);
        }

        setVisible(false);
        dispose();
    }
}
