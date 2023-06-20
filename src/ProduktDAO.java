import java.sql.*;

public class ProduktDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/storemanagmentsystemdb";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void dodajProdukt(Produkt produkt, int userId) {
//        if (userId != 1) {
//            System.out.println("Brak uprawnień do dodania produktu.");
//            return;
//        }

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO produkty (nazwa, cena, opis) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, produkt.getNazwa());
            preparedStatement.setDouble(2, produkt.getCena());
            preparedStatement.setString(3, produkt.getOpis());

            int result = preparedStatement.executeUpdate();
            if (result == 1) {
                System.out.println("Produkt został dodany");
            } else {
                System.out.println("Nie udało się dodać produktu");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void aktualizujProdukt(Produkt produkt, int userId) {
        if (userId != 1) {
            System.out.println("Brak uprawnień do aktualizacji produktu.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "UPDATE produkty SET nazwa = ?, cena = ?, opis = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, produkt.getNazwa());
            stmt.setDouble(2, produkt.getCena());
            stmt.setString(3, produkt.getOpis());
            stmt.setInt(4, produkt.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void usunProdukt(int produktId, int userId) {
//        if (userId != 1) {
//            System.out.println("Brak uprawnień do usunięcia produktu.");
//            return;
//        }

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM produkty WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, produktId);

            int result = preparedStatement.executeUpdate();
            if (result == 1) {
                System.out.println("Produkt został usunięty");
            } else {
                System.out.println("Nie udało się usunąć produktu");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Produkt getProdukt(int produktId, int userId) {
//        if (userId != 1) {
//            System.out.println("Brak uprawnień do przeglądania produktu.");
//            return null;
//        }

        Produkt produkt = null;

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT * FROM produkty WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, produktId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                produkt = new Produkt();
                produkt.setId(resultSet.getInt("id"));
                produkt.setNazwa(resultSet.getString("nazwa"));
                produkt.setCena(resultSet.getDouble("cena"));
                produkt.setOpis(resultSet.getString("opis"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produkt;
    }
}
