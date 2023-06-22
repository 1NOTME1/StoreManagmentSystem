import java.sql.*;
/**
 * Klasa ProduktDAO dostarcza metod CRUD (Create, Read, Update, Delete) dla obiektów klasy Produkt z wykorzystaniem bazy danych.
 */
public class ProduktDAO extends GenericDAO<Produkt> {
    /**
     * Metoda do dodawania nowego produktu do bazy danych.
     *
     * @param produkt obiekt Produkt do dodania
     * @param userId  id użytkownika dodającego produkt
     */
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
                System.out.println("Nie udało się dodać produktu!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda do aktualizacji istniejącego produktu w bazie danych.
     *
     * @param produkt obiekt Produkt do aktualizacji
     * @param userId  id użytkownika aktualizującego produkt
     */
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

    /**
     * Metoda do usuwania istniejącego produktu z bazy danych.
     *
     * @param produktId id produktu do usunięcia
     * @param userId    id użytkownika usuwającego produkt
     */
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
    /**
     * Metoda do pobierania produktu z bazy danych na podstawie id.
     *
     * @param produktId id produktu do pobrania
     * @param userId id użytkownika pobierającego produkt
     * @return zwraca obiekt Produkt, jeżeli produkt o podanym id istnieje, w przeciwnym wypadku null
     */
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