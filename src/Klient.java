import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Klient extends JFrame {
    private JTextField poleLogin;
    private JPasswordField poleHaslo;
    private JPasswordField polePowtorzHaslo;
    private String login;
    private String haslo;

    public Klient() {
        Klient tenObiekt = this;
        setTitle("Logowanie i Rejestracja");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 10, 10);

        JLabel etykietaLogin = new JLabel("Login:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(etykietaLogin, constraints);

        poleLogin = new JTextField(15);
        constraints.gridx = 1;
        panel.add(poleLogin, constraints);

        JLabel etykietaHaslo = new JLabel("Hasło:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(etykietaHaslo, constraints);

        poleHaslo = new JPasswordField(15);
        constraints.gridx = 1;
        panel.add(poleHaslo, constraints);

        JButton przyciskLogowanie = new JButton("Zaloguj");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(przyciskLogowanie, constraints);

        JButton przyciskRejestracja = new JButton("Zarejestruj się");
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.EAST;
        panel.add(przyciskRejestracja, constraints);

        add(panel);

        przyciskLogowanie.addActionListener(e -> {
            login = poleLogin.getText();
            haslo = new String(poleHaslo.getPassword());

            if (sprawdzDaneLogowania(login, haslo)) {
                JOptionPane.showMessageDialog(null, "Zalogowano pomyślnie!");

                // Tworzymy i wyświetlamy główne okno
                PanelKlienta panelKlienta = new PanelKlienta(login); // Przekazanie loginu do panelu klienta
                panelKlienta.setVisible(true);

                // Zamykamy okno logowania
                tenObiekt.dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Nieprawidłowe dane logowania.");
            }
        });

        przyciskRejestracja.addActionListener(e -> {
            JPanel panelRejestracji = new JPanel(new GridLayout(3, 2));
            JTextField poleRegLogin = new JTextField(15); // Ustalamy długość pola login
            JPasswordField poleRegHaslo = new JPasswordField();
            JPasswordField poleRegPowtorzHaslo = new JPasswordField();
            panelRejestracji.add(new JLabel("Podaj nowy login:"));
            panelRejestracji.add(poleRegLogin);
            panelRejestracji.add(new JLabel("Podaj nowe hasło:"));
            panelRejestracji.add(poleRegHaslo);
            panelRejestracji.add(new JLabel("Powtórz nowe hasło:"));
            panelRejestracji.add(poleRegPowtorzHaslo);
            int wynik = JOptionPane.showConfirmDialog(null, panelRejestracji, "Rejestracja", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (wynik == JOptionPane.OK_OPTION) {
                String regLogin = poleRegLogin.getText();
                String regHaslo = new String(poleRegHaslo.getPassword());
                String regPowtorzHaslo = new String(poleRegPowtorzHaslo.getPassword());

                if (regHaslo.equals(regPowtorzHaslo)) {
                    if (rejestruj(regLogin, regHaslo)) {
                        JOptionPane.showMessageDialog(null, "Rejestracja pomyślna!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Rejestracja nieudana.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Podane hasła nie są identyczne.");
                }
            }
        });
    }

    private boolean sprawdzDaneLogowania(String login, String haslo) {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM uzytkownicy WHERE login = ? AND haslo = ?")) {
            statement.setString(1, login);
            statement.setString(2, haslo);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException ex) {
            System.out.println("Błąd podczas łączenia z bazą danych: " + ex.getMessage());
        }

        return false;
    }

    private boolean rejestruj(String login, String haslo) {
        if (login == null || login.isEmpty() || haslo == null || haslo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Login i hasło nie mogą być puste.");
            return false;
        }

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
             PreparedStatement checkStatement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM uzytkownicy WHERE login = ?")) {
            checkStatement.setString(1, login);

            try (ResultSet resultSet = checkStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    if (count > 0) {
                        JOptionPane.showMessageDialog(null, "Taki użytkownik już istnieje.");
                        return false;
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("Błąd podczas łączenia z bazą danych: " + ex.getMessage());
            return false;
        }

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
             PreparedStatement insertStatement = connection.prepareStatement(
                     "INSERT INTO uzytkownicy (login, haslo) VALUES (?, ?)")) {
            insertStatement.setString(1, login);
            insertStatement.setString(2, haslo);
            insertStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.out.println("Błąd podczas łączenia z bazą danych: " + ex.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Klient klient = new Klient();
            klient.setVisible(true);
        });
    }
}
