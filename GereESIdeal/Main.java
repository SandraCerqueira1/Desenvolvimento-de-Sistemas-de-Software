package GereESIdeal;
import GereESIdeal.business.Mecanico;
import GereESIdeal.data.PostodeTrabalhoDAO;
import GereESIdeal.data.*;
import GereESIdeal.ui.TextUI;
import GereESIdeal.business.MecanicoFacade;
import GereESIdeal.data.DAOconfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static GereESIdeal.data.DAOconfig.createDatabase;

public class Main {
    public static void main(String[] args) {

        DAOconfig.createDatabase();


        String fullURL = DAOconfig.getURL();

        // Chamada do método de povoamento
        try (Connection connection = DriverManager.getConnection(fullURL, DAOconfig.getUsername(), DAOconfig.getPassword());
             Statement statement = connection.createStatement()) {
            try {
                PostodeTrabalhoDAO.getInstance();
                // System.out.println("tabela criada");
                PostodeTrabalhoDAO.insertMockData();
                // System.out.println("Dados inseridos nas tabelas do postodetrabalho");

                MecanicoDAO.getInstance();
                // System.out.println("tabelas do mecanico criadas");
                MecanicoDAO.insertMockData();
                // System.out.println("dados inseridos nas tabelaws do mecanicoDAO");


                ServicoDAO.getInstance();
                // System.out.println("tabelas do servicoDAO criadas");
                ServicoDAO.insertMockData();
                // System.out.println("dados inseridos nas tabelas do servicoDAO");


                ClienteDAO.getInstance();
                // System.out.println("tabelas do ClienteDAO criadas");
                ClienteDAO.insertMockData();
                // System.out.println("dados inseridos nas tabelas do clienteDAO");

                TextUI textUI = new TextUI();
                textUI.run();

            } catch (SQLException e) {
                e.printStackTrace();
                // Trate a exceção conforme necessário
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
