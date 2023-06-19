import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Klient extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private JTextField regLoginField;
    private JPasswordField regPasswordField;
    private String login;
    private String haslo;

    public Klient() {
        Klient self = this;
        setTitle("Logowanie i Rejestracja");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        JLabel loginLabel = new JLabel("Login:");
        loginField = new JTextField();
        JLabel passwordLabel = new JLabel("Hasło:");
        passwordField = new JPasswordField();
        JButton loginButton = new JButton("Zaloguj");
        JButton regButton = new JButton("Zarejestruj się");

        panel.add(loginLabel);
        panel.add(loginField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(regButton);

        add(panel);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login = loginField.getText();
                haslo = new String(passwordField.getPassword());

                if (sprawdzDaneLogowania(login, haslo)) {
                    JOptionPane.showMessageDialog(null, "Zalogowano pomyślnie!");

                    // Tworzymy i wyświetlamy główne okno
                    PanelKlienta panelKlienta = new PanelKlienta(login); // Przekazanie loginu do panelu klienta
                    panelKlienta.setVisible(true);

                    // Zamykamy okno logowania
                    self.dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Nieprawidłowe dane logowania.");
                }
            }
        });

        regButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String regLogin = JOptionPane.showInputDialog("Podaj nowy login");
                String regHaslo = JOptionPane.showInputDialog("Podaj nowe hasło");

                if (rejestruj(regLogin, regHaslo)) {
                    JOptionPane.showMessageDialog(null, "Rejestracja pomyślna!");
                } else {
                    JOptionPane.showMessageDialog(null, "Rejestracja nieudana.");
                }
            }
        });
    }

    private boolean sprawdzDaneLogowania(String login, String haslo) {
        try (
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT COUNT(*) FROM uzytkownicy WHERE login = ? AND haslo = ?")
        ) {
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
        // Sprawdź czy login i hasło nie są puste
        if (login == null || login.isEmpty() || haslo == null || haslo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Login i hasło nie mogą być puste.");
            return false;
        }

        // Sprawdź czy taki użytkownik już istnieje
        try (
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                PreparedStatement checkStatement = connection.prepareStatement(
                        "SELECT COUNT(*) FROM uzytkownicy WHERE login = ?")
        ) {
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

        // Dodaj nowego użytkownika
        try (
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/storemanagmentsystemdb", "root", "root");
                PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO uzytkownicy (login, haslo) VALUES (?, ?)")
        ) {
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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Klient klient = new Klient();
                klient.setVisible(true);
            }
        });
    }
}
