import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;
import java.math.BigDecimal;

public class PanelKlienta extends JFrame {
    private JTable tabelaProduktow;
    private String login;
    private int idUzytkownika;
    private JTextField filterField;
    private JButton filterButton;
    private JButton buttonDodajDoKoszyka;
    private JButton buttonZobaczKoszyk;
    private JButton buttonZlozZamowienie;

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

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM produkty")
        ) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            Vector<Vector<Object>> data = new Vector<Vector<Object>>();
            Vector<String> columnNames = new Vector<String>();

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

            tableModel = new DefaultTableModel(data, columnNames);
            tabelaProduktow = new JTable(tableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(tabelaProduktow);
        add(scrollPane, BorderLayout.CENTER);

        JPanel filterPanel = new JPanel();
        filterField = new JTextField(10);
        filterButton = new JButton("Filtruj");
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

        filterPanel.add(filterField);
        filterPanel.add(filterButton);
        filterPanel.add(buttonDodajDoKoszyka);
        filterPanel.add(buttonZobaczKoszyk);
        filterPanel.add(buttonZlozZamowienie);

        // Add the additional CRUD buttons for the admin user
        filterPanel.add(buttonDodajProdukt);
        filterPanel.add(buttonEdytujProdukt);
        filterPanel.add(buttonUsunProdukt);

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
                JOptionPane.showMessageDialog(this, "Zamówienie zostało złożone.");
                koszyk.clear(); // Wyczyść koszyk po złożeniu zamówienia
            } catch (SQLException ex) {
                System.out.println("Błąd podczas złożenia zamówienia: " + ex.getMessage());
            }
        }
    }

    // Method for adding a new product (only for admin user)
    private void dodajProdukt() {
        JTextField nazwaField = new JTextField();
        JTextField cenaField = new JTextField();
        JTextField opisField = new JTextField();

        Object[] message = {
                "Nazwa:", nazwaField,
                "Cena:", cenaField,
                "Opis:", opisField
        };

        boolean isPriceValid;
        do {
            int option = JOptionPane.showConfirmDialog(null, message, "Dodaj produkt", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    double cenaProduktu = Double.parseDouble(cenaField.getText());

                    try (Connection connection = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                         PreparedStatement statement = connection.prepareStatement(
                                 "INSERT INTO produkty (nazwa, cena, opis) VALUES (?, ?, ?)")
                    ) {
                        statement.setString(1, nazwaField.getText());
                        statement.setDouble(2, cenaProduktu);
                        statement.setString(3, opisField.getText());
                        statement.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Produkt został dodany.");
                        isPriceValid = true;
                    } catch (SQLException ex) {
                        System.out.println("Błąd podczas dodawania produktu: " + ex.getMessage());
                        isPriceValid = false;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Nieprawidłowa cena produktu. Proszę wprowadzić ponownie.");
                    isPriceValid = false;
                }
            } else {
                break;
            }
        } while (!isPriceValid);
    }

    // Method for editing an existing product (only for admin user)
    private void edytujProdukt() {
        int selectedRow = tabelaProduktow.getSelectedRow();
        if (selectedRow >= 0) {
            int produktId = (int) tableModel.getValueAt(selectedRow, 0);

            JTextField nazwaField = new JTextField((String) tableModel.getValueAt(selectedRow, 1));
            JTextField cenaField = new JTextField(String.valueOf((double) tableModel.getValueAt(selectedRow, 2)));
            JTextField opisField = new JTextField((String) tableModel.getValueAt(selectedRow, 3));

            Object[] message = {
                    "Nazwa:", nazwaField,
                    "Cena:", cenaField,
                    "Opis:", opisField
            };

            boolean isPriceValid;
            do {
                int option = JOptionPane.showConfirmDialog(null, message, "Edytuj produkt", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        double cenaProduktu = Double.parseDouble(cenaField.getText());

                        try (Connection connection = DriverManager.getConnection(
                                "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                             PreparedStatement statement = connection.prepareStatement(
                                     "UPDATE produkty SET nazwa = ?, cena = ?, opis = ? WHERE id = ?")
                        ) {
                            statement.setString(1, nazwaField.getText());
                            statement.setDouble(2, cenaProduktu);
                            statement.setString(3, opisField.getText());
                            statement.setInt(4, produktId);
                            statement.executeUpdate();
                            JOptionPane.showMessageDialog(this, "Produkt został zaktualizowany.");
                            isPriceValid = true;
                        } catch (SQLException ex) {
                            System.out.println("Błąd podczas edycji produktu: " + ex.getMessage());
                            isPriceValid = false;
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Nieprawidłowa cena produktu. Proszę wprowadzić ponownie.");
                        isPriceValid = false;
                    }
                } else {
                    break;
                }
            } while (!isPriceValid);
        } else {
            JOptionPane.showMessageDialog(this, "Nie wybrano żadnego produktu do edycji.");
        }
    }

    private void usunProdukt() {
        String idProduktuStr = JOptionPane.showInputDialog(this, "Podaj ID produktu do usunięcia:");
        try {
            int idProduktu = Integer.parseInt(idProduktuStr);
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM produkty WHERE id = ?")
            ) {
                statement.setInt(1, idProduktu);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Produkt został usunięty.");
            } catch (SQLException ex) {
                System.out.println("Błąd podczas usuwania produktu: " + ex.getMessage());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Nieprawidłowe ID produktu.");
        }
    }

    private void aktualizujTabele() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                     PreparedStatement statement = connection.prepareStatement(
                             "SELECT * FROM produkty")
                ) {
                    ResultSet resultSet = statement.executeQuery();
                    tableModel.setRowCount(0);
                    while (resultSet.next()) {
                        Vector<Object> row = new Vector<Object>();
                        row.add(resultSet.getInt("id"));
                        row.add(resultSet.getString("nazwa"));
                        row.add(resultSet.getDouble("cena"));
                        row.add(resultSet.getString("opis"));
                        tableModel.addRow(row);
                    }
                } catch (SQLException ex) {
                    System.out.println("Błąd podczas aktualizacji tabeli: " + ex.getMessage());
                }
            }
        }).start();
    }

    private void FajniejszyWyglad() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
