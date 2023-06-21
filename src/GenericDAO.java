import java.sql.*;

public abstract class GenericDAO<T> {
    private static final String URL = "jdbc:mysql://localhost:3306/storemanagmentsystemdb";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    public abstract void dodaj(T object, int userId);
    public abstract void aktualizuj(T object, int userId);
    public abstract void usun(int id, int userId);
    public abstract T get(int id, int userId);
}
