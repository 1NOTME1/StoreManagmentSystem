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
    private JTextField filterField;
    private JButton filterButton;
    private JButton buttonDodajDoKoszyka;
    private JButton buttonZobaczKoszyk;
    private JButton buttonZlozZamowienie;

    // Utwórz przyciski RadioButton
    JLabel filterLabel = new JLabel("Filtry:");
    JRadioButton filterNoneButton = new JRadioButton("Odfiltruj", true);
    JRadioButton filterByIdButton = new JRadioButton("ID");
    JRadioButton filterByNameButton = new JRadioButton("Nazwie");
    JRadioButton filterByPriceButton = new JRadioButton("Cenia");
    JRadioButton filterByDescriptionButton = new JRadioButton("Opisie");

    // Additional CRUD buttons for the admin user
    private JButton buttonDodajProdukt;
    private JButton buttonEdytujProdukt;
    private JButton buttonUsunProdukt;

    private DefaultTableModel tableModel;
    private Vector<Produkt> koszyk;

    public PanelKlienta(String login) {
        this.login = login;
        setTitle("Zarządzanie produktami");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 400);
        setLocationRelativeTo(null);
        FajniejszyWyglad();

        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        Vector<String> columnNames = new Vector<String>();

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM produkty")
        ) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int column = 1; column <= columnCount; column++) {
                columnNames.add(metaData.getColumnName(column));
            }

            while (resultSet.next()) {
                Vector<Object> vector = new Vector<Object>();
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    Object value = resultSet.getObject(columnIndex);
                    if (value instanceof BigDecimal) {
                        vector.add(((BigDecimal) value).doubleValue());
                    } else {
                        vector.add(value);
                    }
                }
                data.add(vector);
            }

            tableModel = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Ustawia wszystkie komórki tabeli jako nieedytowalne
                }
            };
            tabelaProduktow = new JTable(tableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(tabelaProduktow);
        add(scrollPane, BorderLayout.CENTER);

        JPanel ButtonPanel = new JPanel();
        JPanel filterPanel = new JPanel();

        filterField = new JTextField(10);
        filterButton = new JButton("Szukaj");
        buttonDodajDoKoszyka = new JButton("Dodaj do koszyka");
        buttonZobaczKoszyk = new JButton("Zobacz koszyk");
        buttonZlozZamowienie = new JButton("Złóż zamówienie");

        // Initialize the additional CRUD buttons for the admin user
        buttonDodajProdukt = new JButton("Dodaj produkt");
        buttonEdytujProdukt = new JButton("Edytuj produkt");
        buttonUsunProdukt = new JButton("Usuń produkt");

        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = filterField.getText();
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
                int selectedRow = tabelaProduktow.getSelectedRow();
                if (selectedRow >= 0) {
                    int produktId = (int) tableModel.getValueAt(selectedRow, 0);
                    String nazwaProduktu = (String) tableModel.getValueAt(selectedRow, 1);
                    double cenaProduktu = (double) tableModel.getValueAt(selectedRow, 2);
                    Produkt produkt = new Produkt(produktId, nazwaProduktu, cenaProduktu, "");
                    dodajProduktDoKoszyka(produkt);
                    JOptionPane.showMessageDialog(null, "Produkt dodany do koszyka: " + nazwaProduktu);
                } else {
                    JOptionPane.showMessageDialog(null, "Proszę zaznaczyć produkt do dodania do koszyka.");
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

        // ActionListener for the "Dodaj produkt" button (only for admin user)
        buttonDodajProdukt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dodajProdukt();
                aktualizujTabele();
            }
        });

        // ActionListener for the "Edytuj produkt" button (only for admin user)
        buttonEdytujProdukt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edytujProdukt();
                aktualizujTabele();
            }
        });

        // ActionListener for the "Usuń produkt" button (only for admin user)
        buttonUsunProdukt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usunProdukt();
                aktualizujTabele();
            }
        });

        ButtonPanel.add(filterField);
        ButtonPanel.add(filterButton);
        ButtonPanel.add(buttonDodajDoKoszyka);
        ButtonPanel.add(buttonZobaczKoszyk);
        ButtonPanel.add(buttonZlozZamowienie);

        // Add the additional CRUD buttons for the admin user
        ButtonPanel.add(buttonDodajProdukt);
        ButtonPanel.add(buttonEdytujProdukt);
        ButtonPanel.add(buttonUsunProdukt);

        add(ButtonPanel, BorderLayout.NORTH);

        // Dodaj przyciski do grupy, aby umożliwić wybór tylko jednego na raz
        ButtonGroup filterButtonGroup = new ButtonGroup();
        filterButtonGroup.add(filterNoneButton);
        filterButtonGroup.add(filterByIdButton);
        filterButtonGroup.add(filterByNameButton);
        filterButtonGroup.add(filterByPriceButton);
        filterButtonGroup.add(filterByDescriptionButton);

// Akcje do RadioButtonów
        filterNoneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Wyłącz filtr
                ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(null);
            }
        });
        filterByIdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Filtruj po ID zaznaczony.");  // Dodaję tę linię, aby zobaczyć, czy akcja jest wywoływana
                String text = filterField.getText();
                if (text.trim().length() != 0) {
                    ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(RowFilter.regexFilter(text, 0));
                }
            }
        });


        filterByNameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = filterField.getText();
                if (text.trim().length() != 0) {
                    ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
                }
            }
        });

        filterByPriceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = filterField.getText();
                if (text.trim().length() != 0) {
                    ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(RowFilter.regexFilter(text, 2));
                }
            }
        });

        filterByDescriptionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = filterField.getText();
                if (text.trim().length() != 0) {
                    ((TableRowSorter) tabelaProduktow.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?i)" + text, 3));
                }
            }
        });

        filterPanel.add(filterLabel); // dodajemy etykietę najpierw
        filterPanel.add(filterNoneButton);
        filterPanel.add(filterByIdButton);
        filterPanel.add(filterByNameButton);
        filterPanel.add(filterByPriceButton);
        filterPanel.add(filterByDescriptionButton);

// Dodaj panel na dole
        add(filterPanel, BorderLayout.SOUTH);

        tabelaProduktow.setRowSorter(new TableRowSorter<>(tabelaProduktow.getModel()));

        koszyk = new Vector<Produkt>();

        // Pobierz ID użytkownika na podstawie jego loginu
        idUzytkownika = pobierzIdUzytkownika(login);

        // Disable the additional CRUD buttons for non-admin users
        if (idUzytkownika != 1) {
            buttonDodajProdukt.setVisible(false);
            buttonEdytujProdukt.setVisible(false);
            buttonUsunProdukt.setVisible(false);
        }

        // Dodawanie paska narzędzi (JMenuBar)
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Menu "Program"
        JMenu menuProgram = new JMenu("Program");
        menuBar.add(menuProgram);

        // Opcja "Zamknij"
        JMenuItem menuItemZamknij = new JMenuItem("Zamknij");
        menuItemZamknij.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        menuProgram.add(menuItemZamknij);

        // Menu "Operacje"
        JMenu menuOperacje = new JMenu("Operacje");
        menuBar.add(menuOperacje);

        // Opcja "Dodaj" z skrótem klawiszowym Ctrl+D
        JMenuItem menuItemDodaj = new JMenuItem("Dodaj");
        menuItemDodaj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemDodaj.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dodajProdukt();
                aktualizujTabele();
            }
        });
        menuOperacje.add(menuItemDodaj);

        // Opcja "Edytuj" z skrótem klawiszowym Ctrl+E
        JMenuItem menuItemEdytuj = new JMenuItem("Edytuj");
        menuItemEdytuj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemEdytuj.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edytujProdukt();
                aktualizujTabele();
            }
        });
        menuOperacje.add(menuItemEdytuj);

        // Opcja "Usuń" z skrótem klawiszowym Ctrl+U
        JMenuItem menuItemUsun = new JMenuItem("Usuń");
        menuItemUsun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemUsun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usunProdukt();
                aktualizujTabele();
            }
        });
        menuOperacje.add(menuItemUsun);

        // Utwórz i uruchom wątek do odświeżania danych w tabeli co określony interwał czasowy
        Thread refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000); // Interwał odświeżania (5 sekund)
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
                sb.append("Cena: ").append(produkt.getCena()).append("\n");
                sb.append("Opis: ").append(produkt.getOpis()).append("\n");
                sb.append("--------------\n");

                sumaCen += produkt.getCena();
            }

            sb.append("Łączna cena: ").append(sumaCen).append("\n");

            int opcja = JOptionPane.showOptionDialog(this, sb.toString(), "Koszyk",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[]{"Usuń z koszyka", "Anuluj"}, null);

            if (opcja == 0) {
                String indeksStr = JOptionPane.showInputDialog(this, "Podaj indeks produktu do usunięcia:");
                try {
                    int indeks = Integer.parseInt(indeksStr);
                    if (indeks >= 0 && indeks < koszyk.size()) {
                        koszyk.remove(indeks);
                        JOptionPane.showMessageDialog(this, "Produkt został usunięty z koszyka.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Nieprawidłowy indeks produktu.");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Nieprawidłowy indeks produktu.");
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
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("id");
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

                // Generowanie pliku .txt z fakturą
                generujFakture(idUzytkownika, koszyk);

                JOptionPane.showMessageDialog(this, "Zamówienie zostało złożone.");
                koszyk.clear(); // Wyczyść koszyk po złożeniu zamówienia
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
                writer.write("Opis: " + produkt.getOpis() + "\n");
                writer.write("--------------\n");
                sumaCen += produkt.getCena();
            }
            writer.write("Łączna cena: " + sumaCen + "\n");
            writer.flush();
        } catch (IOException ex) {
            System.out.println("Błąd podczas generowania faktury: " + ex.getMessage());
        }
    }

    private void dodajProdukt() {
        JTextField nazwaField = new JTextField();
        JTextField cenaField = new JTextField();
        JTextField opisField = new JTextField();

        Object[] message = {
                "Nazwa:", nazwaField,
                "Cena:", cenaField,
                "Opis:", opisField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Dodaj produkt", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String nazwa = nazwaField.getText();
            String cenaStr = cenaField.getText();
            double cena = Double.parseDouble(cenaStr);
            String opis = opisField.getText();

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
        int selectedRow = tabelaProduktow.getSelectedRow();
        if (selectedRow >= 0) {
            int produktId = (int) tableModel.getValueAt(selectedRow, 0);
            String nazwaProduktu = (String) tableModel.getValueAt(selectedRow, 1);
            double cenaProduktu = (double) tableModel.getValueAt(selectedRow, 2);
            String opisProduktu = (String) tableModel.getValueAt(selectedRow, 3);

            JTextField nazwaField = new JTextField(nazwaProduktu);
            JTextField cenaField = new JTextField(String.valueOf(cenaProduktu));
            JTextField opisField = new JTextField(opisProduktu);

            Object[] message = {
                    "Nazwa:", nazwaField,
                    "Cena:", cenaField,
                    "Opis:", opisField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Edytuj produkt", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String nowaNazwa = nazwaField.getText();
                String nowaCenaStr = cenaField.getText();
                double nowaCena = Double.parseDouble(nowaCenaStr);
                String nowyOpis = opisField.getText();

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
        int selectedRow = tabelaProduktow.getSelectedRow();
        if (selectedRow >= 0) {
            int produktId = (int) tableModel.getValueAt(selectedRow, 0);
            String nazwaProduktu = (String) tableModel.getValueAt(selectedRow, 1);

            int opcja = JOptionPane.showOptionDialog(this,
                    "Czy na pewno chcesz usunąć produkt: " + nazwaProduktu + "?",
                    "Potwierdzenie", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

            if (opcja == JOptionPane.YES_OPTION) {
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
        tableModel.setRowCount(0); // Usuń wszystkie wiersze z tabeli
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM produkty")
        ) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Vector<Object> vector = new Vector<Object>();
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    Object value = resultSet.getObject(columnIndex);
                    if (value instanceof BigDecimal) {
                        vector.add(((BigDecimal) value).doubleValue());
                    } else {
                        vector.add(value);
                    }
                }
                tableModel.addRow(vector);
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
