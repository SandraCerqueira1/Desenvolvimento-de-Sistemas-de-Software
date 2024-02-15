package GereESIdeal.data;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe que representa a configuração da base de dados.
 *
 */

public class DAOconfig {

    static final String USERNAME = "root";                       // Actualizar
    static final String PASSWORD = "123456";                       // Actualizar
    private static final String DATABASE = "oficina";          // Actualizar
    private static final String DRIVER = "jdbc:mariadb";        // Usar para MariaDB
    //private static final String DRIVER = "jdbc:mysql";        // Usar para MySQL
    static final String URL = DRIVER+"://localhost:3306/"+DATABASE;

    public static void createDatabase() {
        try (Connection connection = DriverManager.getConnection(DRIVER+"://localhost:3306/", USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {

            // Criação da base de dados se não existir
            String createDatabaseQuery = "CREATE DATABASE IF NOT EXISTS " + DATABASE;
            statement.executeUpdate(createDatabaseQuery);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Novo método para obter o URL
    public static String getURL() {
        return URL;
    }
    public static String getUsername() {
        return USERNAME;
    }
    // Método para obter a senha
    public static String getPassword() {
        return PASSWORD;
    }

}

