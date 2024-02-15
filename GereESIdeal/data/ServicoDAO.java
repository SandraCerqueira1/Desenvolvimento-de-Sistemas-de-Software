package GereESIdeal.data;
import GereESIdeal.business.Marcacao;
import GereESIdeal.business.PostoDeTrabalho;
import GereESIdeal.business.Servico;

import java.sql.*;
import java.util.*;



/**
 * Versão  de um DAO para Servico
 */



public class ServicoDAO implements Map<String, Servico> {
    // Singleton para garantir apenas uma instância da classe
    private static ServicoDAO singleton = null;

    private ServicoDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            // Tabela Servico
            String sql = "CREATE TABLE IF NOT EXISTS Servico (" +
                    "codServico varchar(10) NOT NULL PRIMARY KEY," +
                    "nome varchar(255) NOT NULL)";
            stm.executeUpdate(sql);
            // System.out.println("tabela serviço criada!!");
            // Tabela TipoServico
            sql = "CREATE TABLE IF NOT EXISTS TipoServico (" +
                    "codServ varchar(10) PRIMARY KEY," +
                    "tipo varchar(255) NOT NULL," +
                    "FOREIGN KEY (codServ) REFERENCES Servico(codServico))";
            stm.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static ServicoDAO getInstance() {
        if (ServicoDAO.singleton == null) {
            ServicoDAO.singleton = new ServicoDAO();
        }
        return ServicoDAO.singleton;
    }

    @Override
    public int size() {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT COUNT(*) FROM Servico")) {

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter o tamanho da tabela de Servico.", e);
        }
        return count;
    }


    @Override
    public boolean isEmpty() {return this.size() == 0;}

    @Override
    public boolean containsKey(Object key) {
        boolean result;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT codServico FROM Servico WHERE codServico='" + key + "'")) {
            result = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao verificar a existência do Servico na tabela.", e);
        }
        return result;
    }

    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof Servico)) {
            return false;
        }

        Servico servico = (Servico) value;

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT codServico FROM Servico WHERE codServico = ?")) {
            stm.setString(1, servico.getCodServico());

            try (ResultSet rs = stm.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao verificar a existência do Servico na tabela.", e);
        }
    }

    @Override
    public Servico get(Object key) {
        Servico servico = new Servico();  // Cria uma instância usando o construtor padrão

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Servico WHERE codServico='" + key + "'")) {

            if (rs.next()) {
                // Define os valores dos campos usando os métodos setter
                servico.setCodServico(rs.getString("codServico"));
                servico.setNome(rs.getString("nome"));

                // Obtém os tipos associados ao serviço usando o novo método
                String tipos = getTiposByCodServico(key.toString());

                // Reconstruir a lista de tipos associados ao serviço
                servico.setTipoMotor(tipos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter Servico da tabela.", e);
        }

        return servico;
    }

    /**
     * Obtém os tipos associados a um serviço, dado o código do serviço.
     *
     * @param codServico Código do serviço
     * @return String contendo os tipos associados ao serviço, separados por vírgula
     * @throws SQLException Em caso de erro na consulta SQL
     */
    private String getTiposByCodServico(String codServico) throws SQLException {
        StringBuilder tiposBuilder = new StringBuilder();
        String sql = "SELECT tipo FROM TipoServico WHERE codServico='" + codServico + "'";
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {

            while (rs.next()) {
                tiposBuilder.append(rs.getString("tipo")).append(", ");
            }
        }

        // Remover a última vírgula e espaço em branco, se houver
        if (tiposBuilder.length() > 0) {
            tiposBuilder.setLength(tiposBuilder.length() - 2);
        }

        return tiposBuilder.toString();
    }

    @Override
    public Servico put(String key, Servico value) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            // Inicia uma transação
            conn.setAutoCommit(false);

            try {
                // Adiciona o serviço na tabela Servico
                insertServico(conn, value);

                // Adiciona os tipos de motor associados ao serviço
                String tipoMotor = value.getTipoMotor();
                if (tipoMotor != null && !tipoMotor.isEmpty()) {
                    insertTipoServico(conn, key, tipoMotor);
                }

                // Comita a transação
                conn.commit();
            } catch (SQLException e) {
                // Em caso de erro, faz rollback
                conn.rollback();
                throw new RuntimeException("Erro ao adicionar serviço na tabela.", e);
            } finally {
                // Restaura o modo de commit automático
                conn.setAutoCommit(true);
            }

            return value;
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao adicionar serviço na tabela.", e);
        }
    }


    private void insertServico(Connection conn, Servico value) throws SQLException {
        String insertServicoSQL = "INSERT INTO Servico(codServico, nome) VALUES (?, ?)";
        try (PreparedStatement pstm = conn.prepareStatement(insertServicoSQL)) {
            pstm.setString(1, value.getCodServico());
            pstm.setString(2, value.getNome());
            pstm.executeUpdate();
        }
    }

    private void insertTipoServico(Connection conn, String codServico, String tipoMotor) throws SQLException {
        String insertTipoServicoSQL = "INSERT INTO tipoServico(codServico, tipo) VALUES (?, ?)";
        try (PreparedStatement pstm = conn.prepareStatement(insertTipoServicoSQL)) {
            pstm.setString(1, codServico);
            pstm.setString(2, tipoMotor);
            pstm.executeUpdate();
        }
    }

    @Override
    public Servico remove(Object key) {
        Servico servicoRemovido = this.get(key);

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             PreparedStatement pstmTipos = conn.prepareStatement("DELETE FROM tipoServico WHERE codServico=?");
             PreparedStatement pstmServico = conn.prepareStatement("DELETE FROM Servico WHERE codServico=?")) {

            // Remover os tipos de serviço associados ao serviço
            pstmTipos.setString(1, key.toString());
            pstmTipos.executeUpdate();

            // Remover o serviço
            pstmServico.setString(1, key.toString());
            pstmServico.executeUpdate();

        } catch (SQLException e) {
            // Erro no banco de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao remover serviço e os tipos associados.", e);
        }

        return servicoRemovido;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Servico> servicos) {
        for (Servico servico : servicos.values()) {
            this.put(servico.getCodServico(), servico);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            // Remover os tipos associados a todos os serviços
            stm.executeUpdate("DELETE FROM TipoServico");

            // Limpar todos os serviços
            stm.executeUpdate("TRUNCATE Servico");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException("Erro durante a limpeza dos serviços: " + e.getMessage());
        }
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT codServico FROM Servico")) {

            while (rs.next()) {
                keys.add(rs.getString("codServico"));
            }

        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter códigos identificadores de serviços.", e);
        }

        return keys;
    }

    @Override
    public Collection<Servico> values() {
        Collection<Servico> res = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT codServico FROM Servico")) {

            while (rs.next()) {
                String codServico = rs.getString("codServico");
                Servico servico = this.get(codServico);
                res.add(servico);
            }

        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter serviços da base de dados.", e);
        }

        return res;
    }
    @Override
    public Set<Entry<String, Servico>> entrySet() {
        throw new UnsupportedOperationException("entrySet() not implemented");
    }

    public String getCodigoByNome(String nomeServico) {
        String codigoServico = null;

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT codServico FROM Servico WHERE nome = ?")) {

            stm.setString(1, nomeServico);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    codigoServico = rs.getString("codServico");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter o código do serviço pelo nome.", e);
        }

        return codigoServico;
    }

    public boolean isServicoDisponivelParaVeiculo(String matricula, String servico) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            String tipoMotorVeiculo = getTipoMotorByMatricula(conn, matricula);
            String codigoServico = getCodigoByNome(servico);
            String tipoServico = getTipoServicoByCodigo(conn, codigoServico);

            if (tipoMotorVeiculo == null) {
                // Se não encontrar o tipo de motor do veículo, retorna false
                System.out.println("Tipo de motor do veículo não encontrado.");
                return false;
            }

            if (tipoServico == null) {
                // Se não encontrar o tipo do serviço, retorna false
                System.out.println("Tipo do serviço não encontrado para o código: " + codigoServico);
                return false;
            }

            // Verifica as condições de disponibilidade do serviço para o tipo de veículo
            switch (tipoServico) {
                case "Universal":
                    return true;
                case "Combustão":
                    return tipoMotorVeiculo.equals("gasolina") ||
                            tipoMotorVeiculo.equals("gasoleoHibrido") ||
                            tipoMotorVeiculo.equals("gasoleo") ||
                            tipoMotorVeiculo.equals("gasolinaHibrido");
                case "Eletrico":
                    return tipoMotorVeiculo.equals("eletrico") ||
                            tipoMotorVeiculo.equals("gasoleoHibrido") ||
                            tipoMotorVeiculo.equals("gasolinaHibrido");
                case "Gasoleo":
                    return tipoMotorVeiculo.equals("gasoleo");
                case "Gasolina":
                    return tipoMotorVeiculo.equals("gasolina");

                default:
                    // Se o tipo de serviço não for reconhecido, retorna false
                    System.out.println("Tipo de serviço não reconhecido: " + servico);
                    return false;
            }
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao verificar a disponibilidade do serviço para o veículo.", e);
        }
    }


    private String getTipoServicoByCodigo(Connection conn, String codigoServico) throws SQLException {
        String tipoServico = null;

        try (PreparedStatement stm = conn.prepareStatement("SELECT tipo FROM TipoServico WHERE codServ = ?")) {
            stm.setString(1, codigoServico);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    tipoServico = rs.getString("tipo");
                }
            }
        }

        return tipoServico;
    }

    private String getTipoMotorByMatricula(Connection conn, String matricula) throws SQLException {
        String tipoMotorVeiculo = null;
        String sql = "SELECT tipo FROM ClienteFichaVeiculo WHERE matricula=?";
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, matricula);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    tipoMotorVeiculo = rs.getString("tipo");
                }
            }
        }
        return tipoMotorVeiculo;
    }

    public static void insertMockData() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD); Statement stm = conn.createStatement()) {
            var rs = stm.executeQuery("SELECT COUNT(*) AS c FROM Servico;");
            rs.next();
            if (rs.getInt("c") == 0) {
                stm.executeUpdate("INSERT INTO Servico(codServico, nome) VALUES ('01', 'Check-up');");
                stm.executeUpdate("INSERT INTO Servico(codServico, nome) VALUES ('02', 'Troca de oleo');");
                stm.executeUpdate("INSERT INTO Servico(codServico, nome) VALUES ('03', 'Troca de filtros');");
                stm.executeUpdate("INSERT INTO Servico(codServico, nome) VALUES ('04', 'Avalição do desempenho das baterias');");
                stm.executeUpdate("INSERT INTO Servico(codServico, nome) VALUES ('05', 'Substituicao das velas de incandescencia');");
                stm.executeUpdate("INSERT INTO Servico(codServico, nome) VALUES ('06', 'Substituicao da valvula do acelerador');");
                stm.executeUpdate("INSERT INTO Servico(codServico, nome) VALUES ('07', 'Troca da bateria de arranque');");
            }
            rs = stm.executeQuery("SELECT COUNT(*) AS c FROM TipoServico;");
            rs.next();
            if (rs.getInt("c") == 0) {
                stm.executeUpdate("INSERT INTO TipoServico(codServ, tipo) VALUES ('01', 'Universal');");
                stm.executeUpdate("INSERT INTO TipoServico(codServ, tipo) VALUES ('02', 'Combustão');");
                stm.executeUpdate("INSERT INTO TipoServico(codServ, tipo) VALUES ('03', 'Eletrico');");
                stm.executeUpdate("INSERT INTO TipoServico(codServ, tipo) VALUES ('04', 'Gasoleo');");
                stm.executeUpdate("INSERT INTO TipoServico(codServ, tipo) VALUES ('05', 'Gasolina');");
                stm.executeUpdate("INSERT INTO TipoServico(codServ, tipo) VALUES ('06', 'Universal');");

            }
        }
    }
}