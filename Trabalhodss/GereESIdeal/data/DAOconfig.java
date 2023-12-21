package GereESIdeal.data;



public class DAOconfig {
    static final String USERNAME = "root";                       // Actualizar
    static final String PASSWORD = "123456";                       // Actualizar
    private static final String DATABASE = "’oficina’";          // Actualizar
    private static final String DRIVER = "jdbc:mariadb";        // Usar para MariaDB
    //private static final String DRIVER = "jdbc:mysql";        // Usar para MySQL
    static final String URL = DRIVER+"://localhost:3307/"+DATABASE;
}
