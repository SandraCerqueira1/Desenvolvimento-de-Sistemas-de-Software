package GereESIdeal.data;

import GereESIdeal.business.*;
import GereESIdeal.business.Motores.*;
import GereESIdeal.business.Motores.Hibrido;
import GereESIdeal.data.ServicoDAO;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Versão  de um DAO para Clientes
 *
 *
 */

public class ClienteDAO implements Map<String, Cliente> {

    // Singleton para garantir apenas uma instância da classe
    private static ClienteDAO singleton = null;

    private ClienteDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Criação da tabela Clientes
            String sql = "CREATE TABLE IF NOT EXISTS clientes (" +
                    "codCliente varchar(10) NOT NULL PRIMARY KEY," +
                    "nome varchar(100) DEFAULT NULL," +
                    "contactoTelefonico varchar(20) DEFAULT NULL," +
                    "email varchar(50) DEFAULT NULL)";
            stm.executeUpdate(sql);
            // System.out.println("tabela 1 clientes criada!!");

            sql = "CREATE TABLE IF NOT EXISTS ClienteFichaVeiculo (" +
                    "codCliente varchar(10) NOT NULL," +
                    "matricula varchar(10) NOT NULL," +
                    "tipo varchar(30) NOT NULL," +
                    "PRIMARY KEY (codCliente, matricula)," +
                    "FOREIGN KEY (codCliente) REFERENCES Clientes(codCliente))";
            stm.executeUpdate(sql);

            // System.out.println("tabela clientefichaveiculo criada!!");
            sql = "CREATE TABLE IF NOT EXISTS FichaVeiculoServico (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "codCliente varchar(10) NOT NULL," +
                    "matricula varchar(10) NOT NULL," +
                    "codServico varchar(10) NOT NULL," +
                    "FOREIGN KEY (codServico) REFERENCES Servico(codServico)," +
                    "FOREIGN KEY (codCliente, matricula) REFERENCES ClienteFichaVeiculo(codCliente, matricula))";
            stm.executeUpdate(sql);

            // System.out.println("tabela historicoservicos criada!!");
            sql = "CREATE TABLE IF NOT EXISTS HistoricoServicos (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "codCliente varchar(10) NOT NULL," +
                    "matricula varchar(10) NOT NULL," +
                    "codServico varchar(10) NOT NULL," +
                    "horaMarc timestamp NOT NULL," +
                    "conclusao varchar(10) NOT NULL," +
                    "comentario varchar(500) DEFAULT NULL," +
                    "FOREIGN KEY (codServico) REFERENCES Servico(codServico)," +
                    "FOREIGN KEY (codCliente, matricula) REFERENCES ClienteFichaVeiculo(codCliente, matricula))";
            stm.executeUpdate(sql);
            // System.out.println("tabela fichaveiculoservicocriada!!");
        } catch (SQLException e) {
            // Erro a criar tabela...
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }

    }
    /**
     * Implementação do padrão Singleton
     *
     * @return devolve a instância única desta classe
     */

    public static ClienteDAO getInstance() {
        if (ClienteDAO.singleton == null) {
            ClienteDAO.singleton = new ClienteDAO();
        }
        return ClienteDAO.singleton;
    }

    // Método adicional para construir a instância Singleton se necessário

    public static void buildInstance() {
        if (ClienteDAO.singleton == null) {
            ClienteDAO.singleton = new ClienteDAO();
        }
    }


    /**
     * @return número de clientes na base de dados
     */
    @Override
    public int size() {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM clientes")) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter o tamanho da tabela de clientes.", e);
        }
        return count;
    }


    /**
     * Método que verifica se existem clientes
     *
     * @return true se existirem 0 clientes
     */
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }



    /**
     * Verifica se um código de cliente existe na base de dados.
     *
     * @param key Código do cliente a verificar
     * @return true se o cliente existe, false caso contrário
     * @throws RuntimeException Em caso de erro durante a execução do método
     */
    @Override
    public boolean containsKey(Object key) {
        boolean result;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT codCliente FROM clientes WHERE codCliente='" + key.toString() + "'")) {
            result = rs.next();
        } catch (SQLException e) {
            // Erro ao executar a consulta
            e.printStackTrace();
            throw new RuntimeException("Erro ao verificar a existência do cliente na tabela.", e);
        }
        return result;
    }

    /**
     * Verifica se um cliente existe na base de dados.
     *
     * @param value Cliente a ser verificado
     * @return true se o cliente existe
     * @throws NullPointerException Em caso de erro
     */
    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof Cliente)) {
            return false; // Se o objeto não é do tipo Cliente, retorna false
        }

        Cliente cliente = (Cliente) value;

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT codCliente FROM clientes WHERE codCliente = ?");
        ) {
            stm.setString(1, cliente.getCodCliente());
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            // Database error!
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    /**
     * Obtém um cliente da base de dados pelo código do cliente.
     *
     * @param key Código do cliente a ser obtido
     * @return Cliente correspondente ao código, ou null se não existir
     * @throws RuntimeException Em caso de erro durante a execução do método
     */

    @Override
    public Cliente get(Object key) {
        Cliente cliente = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT * FROM clientes WHERE codCliente = ?");
        ) {
            stm.setString(1, key.toString());
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    // Obter os dados básicos do cliente
                    String codCliente = rs.getString("codCliente");
                    String nome = rs.getString("nome");
                    String contactoTelefonico = rs.getString("contactoTelefonico");
                    String email = rs.getString("email");

                    // Obter a lista de fichas de veículo associadas ao cliente
                    List<FichaVeiculo> fichaVeiculos = getFichasVeiculo(codCliente);

                    // Construir o objeto Cliente
                    cliente = new Cliente(codCliente, nome, fichaVeiculos, contactoTelefonico, email);
                }
            }
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter cliente da base de dados.", e);
        }
        return cliente;
    }
    /**
     * Obtém a lista de fichas de veículo associadas a um cliente pelo código do cliente.
     *
     * @param codCliente Código do cliente
     * @return Lista de fichas de veículo associadas ao cliente
     * @throws SQLException Em caso de erro durante a execução do método
     */
    private List<FichaVeiculo> getFichasVeiculo(String codCliente) throws SQLException {
        List<FichaVeiculo> fichaVeiculos = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT * FROM ClienteFichaVeiculo WHERE codCliente = ?");
        ) {
            stm.setString(1, codCliente);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    // Obtém a matrícula e tipo da FichaVeiculo
                    String matricula = rs.getString("matricula");
                    String tipoMotorStr= rs.getString("tipo");

                    // Obtém a lista de serviços associados a esta ficha de veículo
                    List<Servico> listaServicos = getServicosFichaVeiculo(matricula);

                    // Converte a string do tipo de motor para o objeto correspondente
                    TipoMotor tipoMotorObj;
                    switch (tipoMotorStr) {
                        case "Gasolina":
                            tipoMotorObj = new Gasolina();
                            break;

                        case "Eletrico":
                            tipoMotorObj = new EletricoConcreta();
                            break;

                        case "Gasolelo":
                            tipoMotorObj = new Gasoleo();
                            break;

                        case "Hibrido":
                            tipoMotorObj = new Hibrido();
                            break;

                        default:
                            tipoMotorObj = null; // Ou lance uma exceção, dependendo do comportamento desejado para valores desconhecidos
                    }

                    // Construir objeto FichaVeiculo e adicioná-lo à lista
                    FichaVeiculo fichaVeiculo = new FichaVeiculo(matricula, listaServicos, tipoMotorObj);
                    fichaVeiculos.add(fichaVeiculo);
                }
            }
        }
        return fichaVeiculos;
    }

    /**
     * Obtém a lista de serviços associados a uma ficha de veículo pelo código da matrícula.
     *
     * @param matricula Código da matrícula da ficha de veículo
     * @return Lista de serviços associados à ficha de veículo
     * @throws SQLException Em caso de erro durante a execução do método
     */
    private List<Servico> getServicosFichaVeiculo(String matricula) throws SQLException {
        List<Servico> servicos = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT * FROM FichaVeiculoServico WHERE matricula = ?");
        ) {
            stm.setString(1, matricula);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    // Obtém o ID do serviço associado à ficha de veículo
                    int codServico = rs.getInt("codServico");

                    // Obtém o objeto Servico usando o método getServicoById
                    Servico servico = getServicoById(codServico);

                    // Adiciona o serviço à lista
                    servicos.add(servico);
                }
            }
        }
        return servicos;
    }

    /**
     * Obtém um objeto Servico pelo ID.
     *
     * @param codServico ID do serviço a ser obtido
     * @return Objeto Servico correspondente ao ID, ou null se não encontrado
     * @throws SQLException Em caso de erro durante a execução do método
     */
    public Servico getServicoById(int codServico) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT * FROM FichaVeiculoServico WHERE codServico = ?");
        ) {
            stm.setInt(1, codServico);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    // Construa o objeto Servico e retorne
                    return new Servico(
                            rs.getString("codServico"),
                            rs.getString("nome"),
                            rs.getString("tipoMotor")
                    );
                }
            }
        }
        return null; // Retorna null se o serviço não for encontrado
    }

    /**
     * Obtém a ficha de veículo associada a uma matrícula.
     *
     * @param matricula Número da matrícula do veículo
     * @return FichaVeiculo associada à matrícula, ou null se não encontrada
     */
    public FichaVeiculo getFichaVeiculoByMatricula(String matricula) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT * FROM ClienteFichaVeiculo WHERE matricula = ?");
        ) {
            stm.setString(1, matricula);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    String tipoMotorStr = rs.getString("tipo");

                    // Obtém a lista de serviços associados a esta ficha de veículo
                    List<Servico> listaServicos = getServicosFichaVeiculo(matricula);

                    // Converte a string do tipo de motor para o objeto correspondente
                    TipoMotor tipoMotorObj;
                    switch (tipoMotorStr) {
                        case "Gasolina":
                            tipoMotorObj = new Gasolina();
                            break;

                        case "Eletrico":
                            tipoMotorObj = new EletricoConcreta();
                            break;

                        case "Gasolelo":
                            tipoMotorObj = new Gasoleo();
                            break;

                        case "Hibrido":
                            tipoMotorObj = new Hibrido();
                            break;

                        default:
                            tipoMotorObj = null; // Ou lance uma exceção, dependendo do comportamento desejado para valores desconhecidos
                    }

                    // Construir objeto FichaVeiculo e retornar
                    return new FichaVeiculo(matricula, listaServicos, tipoMotorObj);
                }
            }
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter ficha de veículo da base de dados.", e);
        }
        return null;
    }


    public Cliente put(String key, Cliente value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key and value cannot be null");
        }

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            conn.setAutoCommit(false);  // Desativar o modo de confirmação automática para transações

            // Verificar se o cliente já existe no banco de dados

            // Inserir novo cliente
            insertCliente(conn, key, value);


            conn.commit();  // Confirmar a transação
            conn.setAutoCommit(true);  // Restaurar o modo de confirmação automática

        } catch (SQLException e) {
            // Lidar com erros de SQL
            e.printStackTrace();
            throw new RuntimeException("Erro ao executar transação de inserção/atualização do cliente", e);
        }

        return value;
    }

    private void insertCliente(Connection conn, String key, Cliente value) throws SQLException {
        // Inserir dados básicos do cliente
        try (PreparedStatement stmClientes = conn.prepareStatement("INSERT INTO clientes VALUES (?, ?, ?, ?)")) {
            stmClientes.setString(1, value.getCodCliente());
            stmClientes.setString(2, value.getNome());
            stmClientes.setString(3, value.getContactoTelefonico());
            stmClientes.setString(4, value.getEmail());
            stmClientes.executeUpdate();
        }

        // Inserir fichas de veículo associadas ao cliente
        for (FichaVeiculo fichaVeiculo : value.getFichaVeiculos()) {
            insertFichaVeiculo(conn, key, fichaVeiculo);
        }
    }

    private void insertFichaVeiculo(Connection conn, String codCliente, FichaVeiculo fichaVeiculo) throws SQLException {
        // Inserir dados básicos da ficha de veículo
        try (PreparedStatement stmFichaVeiculo = conn.prepareStatement("INSERT INTO ClienteFichaVeiculo VALUES (?, ?, ?)")) {
            stmFichaVeiculo.setString(1, codCliente);
            stmFichaVeiculo.setString(2, fichaVeiculo.getMatricula());
            stmFichaVeiculo.setString(3, fichaVeiculo.getTipoMotor().toString());
            stmFichaVeiculo.executeUpdate();
        }

        // Inserir serviços associados à ficha de veículo
        for (Servico servico : fichaVeiculo.getServicos()) {
            insertServico(conn, fichaVeiculo.getMatricula(), servico);
        }
    }

    private void insertServico(Connection conn, String matricula, Servico servico) throws SQLException {
        // Inserir dados básicos do serviço
        try (PreparedStatement stmServico = conn.prepareStatement("INSERT INTO FichaVeiculoServico (matricula, codServico) VALUES (?, ?)")) {
            stmServico.setString(1, matricula);
            // Obtenha o ID do serviço a partir do banco de dados ou outra lógica (depende da implementação de getServicoId)
            int codServico = getServicoId(conn, servico);
            stmServico.setInt(2, codServico);
            stmServico.executeUpdate();
        }
    }

    private int getServicoId(Connection conn, Servico servico) throws SQLException {
        // Implemente a lógica para obter o ID do serviço a partir do banco de dados ou outra fonte
        // Aqui, você pode usar uma consulta para obter o ID com base nos detalhes do serviço
        // Certifique-se de ajustar isso conforme necessário para a sua implementação específica.
        // Este método é um exemplo e pode precisar de adaptações.
        try (PreparedStatement stmGetId = conn.prepareStatement("SELECT codServico FROM Servico WHERE nome = ? AND tipoMotor = ?")) {
            stmGetId.setString(1, servico.getNome());
            stmGetId.setString(2, servico.getTipoMotor().toString());
            try (ResultSet rs = stmGetId.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("codServico");
                }
            }
        }

        // Se não encontrar o serviço, você pode optar por lançar uma exceção ou tomar outra ação adequada.
        throw new SQLException("Não foi possível encontrar o ID do serviço para inserção");
    }

    @Override
    public Cliente remove(Object key) {
        throw new UnsupportedOperationException("Remoção de clientes não é suportada nesta aplicação");
    }
    /**
     * Adicionar um conjunto de clientes à base de dados
     *
     * @param clientes os clientes a adicionar
     * @throws NullPointerException Em caso de erro - deveriam ser criadas exepções do projecto
     */
    @Override
    public void putAll(Map<? extends String, ? extends Cliente> clientes) {
        for(Cliente t : clientes.values()) {
            this.put(t.getCodCliente(), t);
        }
    }

    /**
     * Apagar todos os clientes e dados relacionados do banco de dados.
     */
    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Apagar dados relacionados (por exemplo, FichaVeiculoServico)
            stm.executeUpdate("DELETE FROM FichaVeiculoServico");
            stm.executeUpdate("DELETE FROM ClienteFichaVeiculo");

            // Apagar todos os clientes
            stm.executeUpdate("DELETE FROM clientes");
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao limpar a base de dados de clientes.", e);
        }
    }


    /**
     * Retorna um conjunto de códigos de clientes presentes no banco de dados.
     *
     * @return Conjunto de códigos de clientes
     */
    @Override
    public Set<String> keySet() {
        Set<String> keySet = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT codCliente FROM clientes");
             ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                keySet.add(rs.getString("codCliente"));
            }
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter o conjunto de chaves do banco de dados.", e);
        }
        return keySet;
    }


    /**
     * Retorna uma coleção de todos os clientes da base de dados.
     *
     * @return Coleção de clientes
     */
    @Override
    public Collection<Cliente> values() {
        Collection<Cliente> clientes = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT codCliente FROM clientes");
             ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                String codCliente = rs.getString("codCliente");
                Cliente cliente = this.get(codCliente);
                clientes.add(cliente);
            }
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter a coleção de valores do banco de dados.", e);
        }
        return clientes;
    }



    @Override
    public Set<Entry<String, Cliente>> entrySet() {
        throw new NullPointerException("public Set<Map.Entry<String,Cliente>> entrySet() not implemented!");
    }

    /**
     * Adiciona um serviço à ficha de veículo associada a uma matrícula.
     *
     * @param matricula Número da matrícula do veículo
     * @param servico Serviço a ser adicionado à ficha de veículo
     * @return FichaVeiculo atualizada
     */
    public FichaVeiculo addServicoToVeiculo(String matricula, Servico servico) {
        FichaVeiculo fichaVeiculo = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            conn.setAutoCommit(false);  // Desativar o modo de confirmação automática para transações

            // Obter o código do serviço
            int codServico = getServicoId(conn, servico);

            // Inserir o serviço na tabela FichaVeiculoServico
            try (PreparedStatement stm = conn.prepareStatement("INSERT INTO FichaVeiculoServico (matricula, codServico) VALUES (?, ?)")) {
                stm.setString(1, matricula);
                stm.setInt(2, codServico);
                stm.executeUpdate();
            }

            // Obtém a ficha de veículo atualizada
            fichaVeiculo = getFichaVeiculoByMatricula(matricula);

            conn.commit();  // Confirmar a transação
            conn.setAutoCommit(true);  // Restaurar o modo de confirmação automática

        } catch (SQLException e) {
            // Lidar com erros de SQL
            e.printStackTrace();
            throw new RuntimeException("Erro ao adicionar serviço à ficha de veículo.", e);
        }
        return fichaVeiculo;
    }

    public void updateFichaVeiculoInDatabase(String matricula, FichaVeiculo fichaVeiculo) {
        try {
            try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                 PreparedStatement stmDelete = conn.prepareStatement("DELETE FROM FichaVeiculoServico WHERE matricula = ? AND codServico = ?")) {

                // Remover serviços associados à ficha de veículo no banco de dados
                for (Servico servico : fichaVeiculo.getServicos()) {
                    stmDelete.setString(1, matricula);
                    stmDelete.setInt(2, Integer.parseInt(servico.getCodServico()));
                    stmDelete.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao atualizar a ficha de veículo no banco de dados.", e);
        }
    }

    public boolean checkMatricula(String matricula) {
        boolean matriculaExists = false;

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT COUNT(*) FROM ClienteFichaVeiculo WHERE matricula = ?");
        ) {
            stm.setString(1, matricula);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    int rowCount = rs.getInt(1);
                    matriculaExists = rowCount > 0;
                }
            }
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao verificar a existência da matrícula na tabela ClienteFichaVeiculo.", e);
        }

        return matriculaExists;
    }

    public void inserirServicoFichaVeiculo(String matricula, String codServico) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            conn.setAutoCommit(false);  // Desativar o modo de confirmação automática para transações

            // Obter o código do cliente com base na matrícula
            String codCliente = getCodigoCliente(conn, matricula);

            // Inserir o serviço na tabela FichaVeiculoServico
            try (PreparedStatement stm = conn.prepareStatement("INSERT INTO FichaVeiculoServico (codCliente, matricula, codServico) VALUES (?, ?, ?)")) {
                stm.setString(1, codCliente);
                stm.setString(2, matricula);
                stm.setString(3, codServico);
                stm.executeUpdate();
            }

            conn.commit();  // Confirmar a transação
            conn.setAutoCommit(true);  // Restaurar o modo de confirmação automática

        } catch (SQLException e) {
            // Lidar com erros de SQL
            e.printStackTrace();
            throw new RuntimeException("Erro ao adicionar serviço à ficha de veículo.", e);
        }
    }

    private String getCodigoCliente(Connection conn, String matricula) throws SQLException {
        try (PreparedStatement stm = conn.prepareStatement("SELECT codCliente FROM ClienteFichaVeiculo WHERE matricula = ?")) {
            stm.setString(1, matricula);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("codCliente");
                }
            }
        }

        // Se não encontrar o cliente, você pode optar por lançar uma exceção ou tomar outra ação adequada.
        throw new SQLException("Não foi possível encontrar o código do cliente para a matrícula fornecida");
    }

    public void inserirHistoricoServico(String matricula, String codPostoTrabalho, LocalDateTime horaMarc, String conclusao, String comentario) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("INSERT INTO HistoricoServicos(codCliente, matricula, codServico, horaMarc, conclusao, comentario) VALUES (?, ?, ?, ?, ?, ?)")) {

            // Obter o código do cliente com base na matrícula
            String codCliente = getCodigoCliente(conn, matricula);

            // Obter o código do serviço com base no código do posto de trabalho
            PostodeTrabalhoDAO postodeTrabalhoDAO = PostodeTrabalhoDAO.getInstance();
            String codServico = postodeTrabalhoDAO.getCodServicoByCodPostoTrabalho(codPostoTrabalho);

            // Definir os parâmetros da consulta preparada
            stm.setString(1, codCliente);
            stm.setString(2, matricula);
            stm.setString(3, codServico);
            stm.setTimestamp(4, Timestamp.valueOf(horaMarc));
            stm.setString(5, conclusao);
            if (comentario == null) {
                stm.setNull(6, Types.VARCHAR);  // ou Types.NULL, dependendo do banco de dados
            } else {
                stm.setString(6, comentario);
            }

            // Executar a inserção na tabela HistoricoServicos
            stm.executeUpdate();

        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao inserir histórico de serviços.", e);
        }
    }

    public void removerFichaVeiculoServico(String matricula, String codPostoTrabalho) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("DELETE FROM FichaVeiculoServico WHERE matricula = ? AND codServico = ?")) {

            // Obter o código do serviço com base no código do posto de trabalho
            PostodeTrabalhoDAO postodeTrabalhoDAO = PostodeTrabalhoDAO.getInstance();
            String codServico = postodeTrabalhoDAO.getCodServicoByCodPostoTrabalho(codPostoTrabalho);

            // Definir os parâmetros da consulta preparada
            stm.setString(1, matricula);
            stm.setString(2, codServico);

            // Executar a remoção na tabela FichaVeiculoServicos
            int rowsAffected = stm.executeUpdate();

        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao remover linha da tabela FichaVeiculoServicos.", e);
        }
    }

    public void imprimirHistoricoServicosPorMatricula(String matricula) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT * FROM HistoricoServicos WHERE matricula = ?");
        ) {
            stm.setString(1, matricula);
            try (ResultSet rs = stm.executeQuery()) {
                System.out.println("\n\nServiços já realizados da Matricula " + matricula + ":");
                while (rs.next()) {
                    // Obter os detalhes do histórico de serviços
                    String codCliente = rs.getString("codCliente");
                    String codServico = rs.getString("codServico");
                    LocalDateTime horaMarc = rs.getTimestamp("horaMarc").toLocalDateTime();
                    String conclusao = rs.getString("conclusao");
                    String comentario = rs.getString("comentario");

                    // Imprimir os detalhes do histórico de serviços
                    System.out.println("Código do Cliente: " + codCliente);
                    System.out.println("Código do Serviço: " + codServico);
                    System.out.println("Hora Marcada: " + horaMarc);
                    System.out.println("Conclusão: " + conclusao);
                    System.out.println("Comentário: " + (comentario != null ? comentario : "N/A"));
                    System.out.println("------------------------");
                }
            }
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao imprimir histórico de serviços por matrícula.", e);
        }
    }

    public String getNomeClienteByMatricula(String matricula) {
        String nomeCliente = null;

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement(
                     "SELECT c.nome " +
                             "FROM clientes c " +
                             "JOIN ClienteFichaVeiculo cfv ON c.codCliente = cfv.codCliente " +
                             "WHERE cfv.matricula = ?"
             )
        ) {
            stm.setString(1, matricula);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    nomeCliente = rs.getString("nome");
                }
            }
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter o nome do cliente pela matrícula.", e);
        }

        return nomeCliente;
    }



    public static void insertMockData() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD); Statement stm = conn.createStatement()) {
            var rs = stm.executeQuery("SELECT COUNT(*) AS c FROM clientes;");
            rs.next();
            if (rs.getInt("c") == 0) {
                stm.executeUpdate("INSERT INTO clientes(codCliente, nome, contactoTelefonico, email) VALUES ('001','Antonio Macedo', '917456238', 'macadoa@gmail.com');");
                stm.executeUpdate("INSERT INTO clientes(codCliente, nome, contactoTelefonico, email) VALUES ('002','Maria Joana', '923654732', 'joanam@gmail.com');");
                stm.executeUpdate("INSERT INTO clientes(codCliente, nome, contactoTelefonico, email) VALUES ('003','Joao Neves', '969030796', 'nevesj@gmail.com');");
                stm.executeUpdate("INSERT INTO clientes(codCliente, nome, contactoTelefonico, email) VALUES ('004','Paulo Silva', '934632743', 'silvap@gmail.com');");
                stm.executeUpdate("INSERT INTO clientes(codCliente, nome, contactoTelefonico, email) VALUES ('005','Joana Castro', '937564349', 'castroj@gmail.com');");
                stm.executeUpdate("INSERT INTO clientes(codCliente, nome, contactoTelefonico, email) VALUES ('006','Teresa Oliveira', '965823822', 'oliveriat@gmail.com');");
            }
            rs = stm.executeQuery("SELECT COUNT(*) AS c FROM ClienteFichaVeiculo;");
            rs.next();
            if (rs.getInt("c") == 0) {
                stm.executeUpdate("INSERT INTO ClienteFichaVeiculo(codCliente, matricula, tipo) VALUES ('001', '01AA01', 'eletrico');");
                stm.executeUpdate("INSERT INTO ClienteFichaVeiculo(codCliente, matricula, tipo) VALUES ('002', '02BB02', 'gasolina');");
                stm.executeUpdate("INSERT INTO ClienteFichaVeiculo(codCliente, matricula, tipo) VALUES ('003', '03CC03', 'gasoleoHibrido');");
                stm.executeUpdate("INSERT INTO ClienteFichaVeiculo(codCliente, matricula, tipo) VALUES ('004', '04DD04', 'gasoleo');");
                stm.executeUpdate("INSERT INTO ClienteFichaVeiculo(codCliente, matricula, tipo) VALUES ('005', '05EE05', 'gasolina');");
                stm.executeUpdate("INSERT INTO ClienteFichaVeiculo(codCliente, matricula, tipo) VALUES ('006', '09II09', 'gasolinaHibrido');");

            }
            rs = stm.executeQuery("SELECT COUNT(*) AS c FROM FichaVeiculoServico;");
            rs.next();
            if (rs.getInt("c") == 0) {
                stm.executeUpdate("INSERT INTO FichaVeiculoServico(codCliente, matricula, codServico) VALUES ('001', '01AA01', '03');");
                stm.executeUpdate("INSERT INTO FichaVeiculoServico(codCliente, matricula, codServico) VALUES ('001', '01AA01', '01');");
                stm.executeUpdate("INSERT INTO FichaVeiculoServico(codCliente, matricula, codServico) VALUES ('002', '02BB02', '02');");
                stm.executeUpdate("INSERT INTO FichaVeiculoServico(codCliente, matricula, codServico) VALUES ('003', '03CC03', '03');");
                stm.executeUpdate("INSERT INTO FichaVeiculoServico(codCliente, matricula, codServico) VALUES ('004', '04DD04', '04');");
                stm.executeUpdate("INSERT INTO FichaVeiculoServico(codCliente, matricula, codServico) VALUES ('005', '05EE05', '05');");
                stm.executeUpdate("INSERT INTO FichaVeiculoServico(codCliente, matricula, codServico) VALUES ('006', '09II09', '03');");
                stm.executeUpdate("INSERT INTO FichaVeiculoServico(codCliente, matricula, codServico) VALUES ('003', '03CC03', '04');");
                stm.executeUpdate("INSERT INTO FichaVeiculoServico(codCliente, matricula, codServico) VALUES ('006', '09II09', '05');");

            }
        }
    }
}