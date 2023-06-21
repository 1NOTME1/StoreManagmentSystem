import java.sql.*;

public class ProduktDAO extends GenericDAO<Produkt> {
    @Override
    public void dodaj(Produkt produkt, int userId) {
        try (Connection connection = getConnection()) {
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
    @Override
    public void aktualizuj(Produkt produkt, int userId) {
        try (Connection conn = getConnection()) {
            String query = "UPDATE produkty SET nazwa = ?, cena = ?, opis = ? WHERE id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, produkt.getNazwa());
            preparedStatement.setDouble(2, produkt.getCena());
            preparedStatement.setString(3, produkt.getOpis());
            preparedStatement.setInt(4, produkt.getId());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void usun(int produktId, int userId) {
        try (Connection connection = getConnection()) {
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
    @Override
    public Produkt get(int produktId, int userId) {
        Produkt produkt = null;

        try (Connection connection = getConnection()) {
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