import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Klient extends JFrame {
    private JTextField poleLogin;
    private JPasswordField poleHaslo;
    private String login, haslo;

    public Klient() {
        Klient self = this;
        setTitle("Panel logowania");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 10, 10);

        JLabel etykietaLogin = new JLabel("Login:");
        etykietaLogin.setFont(new Font("Arial", Font.PLAIN, 14));
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(etykietaLogin, constraints);

        poleLogin = new JTextField(15);
        constraints.gridx = 1;
        panel.add(poleLogin, constraints);

        JLabel etykietaHaslo = new JLabel("Hasło:");
        etykietaHaslo.setFont(new Font("Arial", Font.PLAIN, 14));
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(etykietaHaslo, constraints);

        poleHaslo = new JPasswordField(15);
        constraints.gridx = 1;
        panel.add(poleHaslo, constraints);

        JButton przyciskLogowanie = new JButton("Zaloguj");
        przyciskLogowanie.setFont(new Font("Arial", Font.BOLD, 14));
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(przyciskLogowanie, constraints);

        JButton przyciskRejestracja = new JButton("Zarejestruj się");
        przyciskRejestracja.setFont(new Font("Arial", Font.BOLD, 14));
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.EAST;
        panel.add(przyciskRejestracja, constraints);

        add(panel);

        przyciskLogowanie.addActionListener(e -> {
            login = poleLogin.getText();
            haslo = new String(poleHaslo.getPassword());

            if (sprawdzDaneLogowania(login, haslo)) {
                JOptionPane.showMessageDialog(null, "Zalogowano pomyślnie!\nWitaj, " + login + "!", "Sukces", JOptionPane.INFORMATION_MESSAGE);

                PanelKlienta panelKlienta = new PanelKlienta(login);
                panelKlienta.setVisible(true);
                self.dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Nieprawidłowe dane logowania!", "Błąd logowania", JOptionPane.ERROR_MESSAGE);
            }
        });

        przyciskRejestracja.addActionListener(e -> {
            JPanel panelRejestracji = new JPanel(new GridLayout(3, 2));
            JTextField poleRegLogin = new JTextField(15);
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
                        JOptionPane.showMessageDialog(null, "Rejestracja pomyślna!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Rejestracja nieudana.", "Błąd rejestracji", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Podane hasła nie są identyczne.", "Błąd rejestracji", JOptionPane.ERROR_MESSAGE);
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

            try (ResultSet query = statement.executeQuery()) {
                if (query.next()) {
                    int count = query.getInt(1);
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
