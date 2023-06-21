import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.UUID;
import java.util.Vector;
import java.math.BigDecimal;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PanelKlienta extends JFrame {
    private JTable tabelaProduktow;
    private String login;
    private int idUzytkownika;
    private JTextField PoleDoFiltrowania;
    private JButton filtrButton;

    //Buttony
    private JButton buttonDodajDoKoszyka;
    private JButton buttonZobaczKoszyk;
    private JButton buttonZlozZamowienie;
    private JButton buttonDodajProdukt;
    private JButton buttonEdytujProdukt;
    private JButton buttonUsunProdukt;

    //RadioButtony
    JLabel filterLabel = new JLabel("Filtry:");
    JRadioButton BrakFiltraButton = new JRadioButton("Odfiltruj", true);
    JRadioButton FIltrIdButton = new JRadioButton("ID");
    JRadioButton FiltrNazwaButton = new JRadioButton("Nazwie");
    JRadioButton FiltrCenaButton = new JRadioButton("Cenia");
    JRadioButton FiltrOpisButton = new JRadioButton("Opisie");

    private DefaultTableModel ModelTabeli;
    private Vector<Produkt> koszyk;

    public PanelKlienta(String login) {
        this.login = login;
        setTitle("StoreManagmentSystem");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 500);
        setLocationRelativeTo(null);
        FajniejszyWyglad();

        Vector<Vector<Object>> daneTabeli = new Vector<Vector<Object>>();
        Vector<String> nazwyKolumn = new Vector<String>();

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
             Statement statement = connection.createStatement();
             ResultSet query = statement.executeQuery("SELECT * FROM produkty")
        ) {
            ResultSetMetaData dataQuery = query.getMetaData();
            int liczbaKolumn = dataQuery.getColumnCount();

            for (int kolumna = 1; kolumna <= liczbaKolumn; kolumna++) {
                nazwyKolumn.add(dataQuery.getColumnName(kolumna));
            }

            while (query.next()) {
                Vector<Object> wiersz = new Vector<Object>();
                for (int kolumna = 1; kolumna <= liczbaKolumn; kolumna++) {
                    Object wartoscKomorki = query.getObject(kolumna);
                    if (wartoscKomorki instanceof BigDecimal) {
                        wiersz.add(((BigDecimal) wartoscKomorki).doubleValue());
                    } else {
                        wiersz.add(wartoscKomorki);
                    }
                }
                daneTabeli.add(wiersz);
            }

            ModelTabeli = new DefaultTableModel(daneTabeli, nazwyKolumn) {
                @Override
                public boolean isCellEditable(int wiersz, int kolumna) {
                    return false;
                }
            };
            tabelaProduktow = new JTable(ModelTabeli);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(tabelaProduktow);
        add(scrollPane, BorderLayout.CENTER);

        JPanel PanelDlaPrzyciskow = new JPanel();
        JPanel PanelDlaFiltrow = new JPanel();

        PoleDoFiltrowania = new JTextField(10);
        filtrButton = new JButton("Szukaj");
        buttonDodajDoKoszyka = new JButton("Dodaj do koszyka");
        buttonZobaczKoszyk = new JButton("Zobacz koszyk");
        buttonZlozZamowienie = new JButton("Złóż zamówienie");
        buttonDodajProdukt = new JButton("Dodaj produkt");
        buttonEdytujProdukt = new JButton("Edytuj produkt");
        buttonUsunProdukt = new JButton("Usuń produkt");

        filtrButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = PoleDoFiltrowania.getText();
                if (text.trim().length() == 0) {
                    ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(null);
                } else {
                    ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        buttonDodajDoKoszyka.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int AktwynyWiersz = tabelaProduktow.getSelectedRow();
                if (AktwynyWiersz >= 0) {
                    int produktId = (int) ModelTabeli.getValueAt(AktwynyWiersz, 0);
                    String nazwaProduktu = (String) ModelTabeli.getValueAt(AktwynyWiersz, 1);
                    double cenaProduktu = (double) ModelTabeli.getValueAt(AktwynyWiersz, 2);
                    Produkt produkt = new Produkt(produktId, nazwaProduktu, cenaProduktu, "");
                    dodajProduktDoKoszyka(produkt);
                    JOptionPane.showMessageDialog(null, "Produkt dodany do koszyka: " + nazwaProduktu);
                } else {
                    JOptionPane.showMessageDialog(null, "Proszę zaznaczyć produkt do dodania do koszyka!");
                }
            }
        });
        buttonZobaczKoszyk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pokazKoszyk();
            }
        });
        buttonZlozZamowienie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zlozZamowienie();
            }
        });
//////////////////////////////////////////////////////// TYLKO DLA ADMINA //////////////////////////////////////////////////////////////////////////////////
        buttonDodajProdukt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dodajProdukt();
                aktualizujTabele();
            }
        });
        buttonEdytujProdukt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edytujProdukt();
                aktualizujTabele();
            }
        });
        buttonUsunProdukt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usunProdukt();
                aktualizujTabele();
            }
        });

        PanelDlaPrzyciskow.add(PoleDoFiltrowania);
        PanelDlaPrzyciskow.add(filtrButton);
        PanelDlaPrzyciskow.add(buttonDodajDoKoszyka);
        PanelDlaPrzyciskow.add(buttonZobaczKoszyk);
        PanelDlaPrzyciskow.add(buttonZlozZamowienie);
        PanelDlaPrzyciskow.add(buttonDodajProdukt);
        PanelDlaPrzyciskow.add(buttonEdytujProdukt);
        PanelDlaPrzyciskow.add(buttonUsunProdukt);

        add(PanelDlaPrzyciskow, BorderLayout.NORTH);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ButtonGroup filtrButtonGroup = new ButtonGroup();
        filtrButtonGroup.add(BrakFiltraButton);
        filtrButtonGroup.add(FIltrIdButton);
        filtrButtonGroup.add(FiltrNazwaButton);
        filtrButtonGroup.add(FiltrCenaButton);
        filtrButtonGroup.add(FiltrOpisButton);

        BrakFiltraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(null);
            }
        });
        FIltrIdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = PoleDoFiltrowania.getText();
                if (text.trim().length() != 0) {
                    ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(RowFilter.regexFilter(text, 0));
                }
            }
        });
        FiltrNazwaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = PoleDoFiltrowania.getText();
                if (text.trim().length() != 0) {
                    ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
                }
            }
        });
        FiltrCenaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = PoleDoFiltrowania.getText();
                if (text.trim().length() != 0) {
                    ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(RowFilter.regexFilter(text, 2));
                }
            }
        });
        FiltrOpisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = PoleDoFiltrowania.getText();
                if (text.trim().length() != 0) {
                    ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?i)" + text, 3));
                }
            }
        });

        PanelDlaFiltrow.add(filterLabel);
        PanelDlaFiltrow.add(BrakFiltraButton);
        PanelDlaFiltrow.add(FIltrIdButton);
        PanelDlaFiltrow.add(FiltrNazwaButton);
        PanelDlaFiltrow.add(FiltrCenaButton);
        PanelDlaFiltrow.add(FiltrOpisButton);
        add(PanelDlaFiltrow, BorderLayout.SOUTH);

        tabelaProduktow.setRowSorter(new TableRowSorter<>(tabelaProduktow.getModel()));
        koszyk = new Vector<Produkt>();
        idUzytkownika = pobierzIdUzytkownika(login);

        if (idUzytkownika != 1) {
            buttonDodajProdukt.setVisible(false);
            buttonEdytujProdukt.setVisible(false);
            buttonUsunProdukt.setVisible(false);
        }

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menuProgram = new JMenu("Program");
        menuBar.add(menuProgram);

        JMenuItem menuItemZamknij = new JMenuItem("Zamknij");
        menuItemZamknij.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        menuProgram.add(menuItemZamknij);

        JMenu menuOperacje = new JMenu("Operacje");
        menuBar.add(menuOperacje);

        JMenuItem menuItemDodaj = new JMenuItem("Dodaj");
        menuItemDodaj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuItemDodaj.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dodajProdukt();
                aktualizujTabele();
            }
        });
        menuOperacje.add(menuItemDodaj);

        JMenuItem menuItemEdytuj = new JMenuItem("Edytuj");
        menuItemEdytuj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuItemEdytuj.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edytujProdukt();
                aktualizujTabele();
            }
        });
        menuOperacje.add(menuItemEdytuj);

        JMenuItem menuItemUsun = new JMenuItem("Usuń");
        menuItemUsun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        menuItemUsun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usunProdukt();
                aktualizujTabele();
            }
        });
        menuOperacje.add(menuItemUsun);

        Thread refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                aktualizujTabele();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }
    private void dodajProduktDoKoszyka(Produkt produkt) {
        koszyk.add(produkt);
    }
    private void pokazKoszyk() {
        if (koszyk.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Koszyk jest pusty.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Zawartość koszyka:\n");

            double sumaCen = 0.0;

            for (int i = 0; i < koszyk.size(); i++) {
                Produkt produkt = koszyk.get(i);
                sb.append("Indeks: ").append(i).append("\n");
                sb.append("Nazwa: ").append(produkt.getNazwa()).append("\n");
                sb.append("Cena: ").append(String.format("%.2f", produkt.getCena())).append("\n");
                sb.append("----------------------------------------------------------------\n");

                sumaCen += produkt.getCena();
            }

            sb.append("Łączna cena: ").append(String.format("%.2f", sumaCen)).append(" zł\n");

            int option = JOptionPane.showOptionDialog(this, sb.toString(), "Koszyk",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[]{"Usuń z koszyka", "Anuluj"}, null);

            if (option == 0) {
                String UserIndex = JOptionPane.showInputDialog(this, "Podaj indeks produktu do usunięcia:");
                try {
                    int index = Integer.parseInt(UserIndex);
                    if (index >= 0 && index < koszyk.size()) {
                        koszyk.remove(index);
                        JOptionPane.showMessageDialog(this, "Produkt został usunięty z koszyka.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Nieprawidłowy indeks produktu!");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Nieprawidłowy indeks produktu!");
                }
            }
        }
    }

    private int pobierzIdUzytkownika(String login) {
        int id = 0;
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM uzytkownicy WHERE login = ?")
        ) {
            statement.setString(1, login);
            ResultSet query = statement.executeQuery();
            if (query.next()) {
                id = query.getInt("id");
            }
        } catch (SQLException ex) {
            System.out.println("Błąd podczas pobierania ID użytkownika: " + ex.getMessage());
        }
        return id;
    }

    private void zlozZamowienie() {
        if (koszyk.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Koszyk jest pusty. Dodaj produkty przed złożeniem zamówienia.");
        } else {
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO zamowienia (id_uzytkownika, nazwa_produktu, cena_produktu) VALUES (?, ?, ?)")
            ) {
                for (Produkt produkt : koszyk) {
                    statement.setInt(1, idUzytkownika);
                    statement.setString(2, produkt.getNazwa());
                    statement.setDouble(3, produkt.getCena());
                    statement.executeUpdate();
                }
                generujFakture(idUzytkownika, koszyk);

                JOptionPane.showMessageDialog(this, "Zamówienie zostało złożone pomyślnie.");
                koszyk.clear();
            } catch (SQLException ex) {
                System.out.println("Błąd podczas złożenia zamówienia: " + ex.getMessage());
            }
        }
    }

    private void generujFakture(int idUzytkownika, List<Produkt> koszyk) {
        String fakturaId = UUID.randomUUID().toString();
        String nazwaPliku = "faktura_" + fakturaId + ".txt";
        try (FileWriter writer = new FileWriter(nazwaPliku)) {
            writer.write("FAKTURA\n\n");
            writer.write("ID użytkownika: " + idUzytkownika + "\n");
            writer.write("Zawartość koszyka:\n");
            double sumaCen = 0.0;
            for (Produkt produkt : koszyk) {
                writer.write("Nazwa: " + produkt.getNazwa() + "\n");
                writer.write("Cena: " + produkt.getCena() + "\n");
                writer.write("----------------------------------------------------------------\n");
                sumaCen += produkt.getCena();
            }
            writer.write("Łączna cena: " + sumaCen + "\n\n");
            writer.write("Dziękujemy za zakupy!\n");
            writer.write("Prosimy o dokonanie płatności w ciągu 14 dni.\n");
            writer.write("W razie pytań prosimy o kontakt e-mail: storemanagment@system.com\n");
            writer.flush();
        } catch (IOException ex) {
            System.out.println("Błąd podczas generowania faktury: " + ex.getMessage());
        }
    }

    private void dodajProdukt() {
        JTextField poleNazwa = new JTextField();
        JTextField poleCena = new JTextField();
        JTextField poleOpis = new JTextField();

        Object[] polaFormularza = {
                "Nazwa:", poleNazwa,
                "Cena:", poleCena,
                "Opis:", poleOpis
        };

        int option = JOptionPane.showConfirmDialog(null, polaFormularza, "Dodaj produkt", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String nazwa = poleNazwa.getText();
            String cenaStr = poleCena.getText();
            double cena = Double.parseDouble(cenaStr);
            String opis = poleOpis.getText();

            try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO produkty (nazwa, cena, opis) VALUES (?, ?, ?)")
            ) {
                statement.setString(1, nazwa);
                statement.setDouble(2, cena);
                statement.setString(3, opis);
                statement.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("Błąd podczas dodawania produktu: " + ex.getMessage());
            }
        }
    }

    private void edytujProdukt() {
        int AktwynyWiersz = tabelaProduktow.getSelectedRow();
        if (AktwynyWiersz >= 0) {
            int produktId = (int) ModelTabeli.getValueAt(AktwynyWiersz, 0);
            String nazwaProduktu = (String) ModelTabeli.getValueAt(AktwynyWiersz, 1);
            double cenaProduktu = (double) ModelTabeli.getValueAt(AktwynyWiersz, 2);
            String opisProduktu = (String) ModelTabeli.getValueAt(AktwynyWiersz, 3);

            JTextField poleNazwa = new JTextField(nazwaProduktu);
            JTextField poleCena = new JTextField(String.valueOf(cenaProduktu));
            JTextField poleOpis = new JTextField(opisProduktu);

            Object[] polaFormularza = {
                    "Nazwa:", poleNazwa,
                    "Cena:", poleCena,
                    "Opis:", poleOpis
            };

            int option = JOptionPane.showConfirmDialog(null, polaFormularza, "Edytuj produkt", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String nowaNazwa = poleNazwa.getText();
                String nowaCenaStr = poleCena.getText();
                double nowaCena = Double.parseDouble(nowaCenaStr);
                String nowyOpis = poleOpis.getText();

                try (Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                     PreparedStatement statement = connection.prepareStatement(
                             "UPDATE produkty SET nazwa = ?, cena = ?, opis = ? WHERE id = ?")
                ) {
                    statement.setString(1, nowaNazwa);
                    statement.setDouble(2, nowaCena);
                    statement.setString(3, nowyOpis);
                    statement.setInt(4, produktId);
                    statement.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println("Błąd podczas edycji produktu: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Proszę zaznaczyć produkt do edycji.");
        }
    }

    private void usunProdukt() {
        int AktwynyWiersz = tabelaProduktow.getSelectedRow();
        if (AktwynyWiersz >= 0) {
            int produktId = (int) ModelTabeli.getValueAt(AktwynyWiersz, 0);
            String nazwaProduktu = (String) ModelTabeli.getValueAt(AktwynyWiersz, 1);

            int option = JOptionPane.showOptionDialog(this,
                    "Czy na pewno chcesz usunąć produkt: " + nazwaProduktu + "?",
                    "Potwierdzenie", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

            if (option == JOptionPane.YES_OPTION) {
                try (Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                     PreparedStatement statement = connection.prepareStatement(
                             "DELETE FROM produkty WHERE id = ?")
                ) {
                    statement.setInt(1, produktId);
                    statement.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println("Błąd podczas usuwania produktu: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Proszę zaznaczyć produkt do usunięcia.");
        }
    }

    private void aktualizujTabele() {
        ModelTabeli.setRowCount(0);
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
             Statement statement = connection.createStatement();
             ResultSet query = statement.executeQuery("SELECT * FROM produkty")
        ) {
            ResultSetMetaData dataQuery = query.getMetaData();
            int liczbaKolumn = dataQuery.getColumnCount();

            while (query.next()) {
                Vector<Object> wiersz = new Vector<Object>();
                for (int kolumna = 1; kolumna <= liczbaKolumn; kolumna++) {
                    Object wartoscKomorki = query.getObject(kolumna);
                    if (wartoscKomorki instanceof BigDecimal) {
                        wiersz.add(((BigDecimal) wartoscKomorki).doubleValue());
                    } else {
                        wiersz.add(wartoscKomorki);
                    }
                }
                ModelTabeli.addRow(wiersz);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void FajniejszyWyglad() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PanelKlienta.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PanelKlienta.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PanelKlienta.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PanelKlienta.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
