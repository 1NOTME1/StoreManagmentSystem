import java.sql.*;
/**
 * Klasa abstrakcyjna GenericDAO z funkcjonalnościa dla dostępu do bazy danych z wykorzystaniem wzorca DAO (Data Access Object).
 * Implementacje tej klasy obsługuje operacje CRUD (Create, Read, Update, Delete) dla określonego typu obiektów.
 * @param <T> typ obiektu, z którym ma być powiązane DAO
 */
public abstract class GenericDAO<T> {
    private static final String URL = "jdbc:mysql://localhost:3306/storemanagmentsystemdb";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    /**
     * Tworzy i zwraca połączenie do bazy danych.
     *
     * @return zwraca połączenie do bazy danych
     * @throws SQLException jeśli wystąpi problem z uzyskaniem połączenia
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Metoda abstrakcyjna do dodawania obiektu do bazy danych.
     *
     * @param object  obiekt do dodania
     * @param userId  id użytkownika dokonującego operacji
     */
    public abstract void dodaj(T object, int userId);

    /**
     * Metoda abstrakcyjna do aktualizacji obiektu w bazie danych.
     *
     * @param object  obiekt do aktualizacji
     * @param userId  id użytkownika dokonującego operacji
     */
    public abstract void aktualizuj(T object, int userId);

    /**
     * Metoda abstrakcyjna do usuwania obiektu z bazy danych na podstawie identyfikatora.
     *
     * @param id      id obiektu do usunięcia
     * @param userId  id użytkownika który wykonuje operacji
     */
    public abstract void usun(int id, int userId);

    /**
     * Metoda abstrakcyjna do pobierania obiektu z bazy danych na podstawie identyfikatora.
     *
     * @param id      id obiektu do pobrania
     * @param userId  id użytkownika który wykonuje operacji
     * @return obiekt T, jeżeli obiekt o podanym id istnieje, w przeciwnym wypadku null
     */
    public abstract T get(int id, int userId);
}
