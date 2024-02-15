package GereESIdeal.data;
import GereESIdeal.business.Marcacao;
import GereESIdeal.business.PostoDeTrabalho;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;



/**
 * Versão  de um DAO para Postos de trabalho
 *
 *
 */

public class PostodeTrabalhoDAO implements Map<String, PostoDeTrabalho>{
    // Singleton para garantir apenas uma instância da classe
    private static PostodeTrabalhoDAO singleton = null;


    private PostodeTrabalhoDAO(){
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {

            // Tabela de postos de trabalho
            String sql = "CREATE TABLE IF NOT EXISTS PostosDeTrabalho (" +
                    "codPostoTrabalho varchar(10) NOT NULL PRIMARY KEY," +
                    "servicoEspecificoPosto varchar(255) DEFAULT NULL)";
            stm.executeUpdate(sql);

            // Tabela de marcações associadas a postos de trabalho
            sql = "CREATE TABLE IF NOT EXISTS Marcacoes (" +
                    "idMarcacao INT AUTO_INCREMENT PRIMARY KEY," +
                    "horaMarc timestamp NOT NULL," +
                    "matricula varchar(255) NOT NULL," +
                    "codPostoTrabalho varchar(10) NOT NULL," +
                    "estado varchar(30) NOT NULL," +
                    "FOREIGN KEY (codPostoTrabalho) REFERENCES PostosDeTrabalho(codPostoTrabalho))";
            stm.executeUpdate(sql);

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
    public static PostodeTrabalhoDAO getInstance() {
        if (PostodeTrabalhoDAO.singleton == null) {
            PostodeTrabalhoDAO.singleton = new PostodeTrabalhoDAO();
        }
        return PostodeTrabalhoDAO.singleton;
    }

    /**
     * @return número de postos de trabalho na base de dados
     */

    @Override
    public int size() {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM PostosDeTrabalho ")) {

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            // erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter o tamanho da tabela de Postos de Trabalho.", e);
        } catch (Exception e) {
            // Outros erros
            e.printStackTrace();
            throw new RuntimeException("Erro inesperado ao obter o tamanho da tabela de Postos de Trabalho.", e);
        }
        return count;
    }

    /**
     * Método que verifica se existem postosdetrabbalho
     *
     * @return true se existirem 0 postos
     */

    @Override
    public boolean isEmpty() {return this.size() == 0;}



    /**
     * Verifica se um código de posto de trabalho existe na base de dados.
     *
     * @param key Código do posto de trabalho a verificar
     * @return true se o posto de trabalho existe, false caso contrário
     * @throws RuntimeException Em caso de erro durante a execução do método
     */
    @Override
    public boolean containsKey(Object key) {
        boolean result;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs =
                     stm.executeQuery("SELECT codPostoTrabalho FROM PostosDeTrabalho WHERE codPostoTrabalho='" + key + "'")) {
            result = rs.next();
        } catch (SQLException e) {
            // Erro ao executar a consulta
            e.printStackTrace();
            throw new RuntimeException("Erro ao verificar a existência do Posto de Trabalho na tabela.", e);
        }
        return result;
    }



    /**
     * Verifica se um posto de trabalho existe na base de dados
     *
     * @param value Posto de trabalho a ser verificado
     * @return true se o posto de trabalho existe
     * @throws NullPointerException Em caso de erro
     */
    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof PostoDeTrabalho)) {
            return false; // Se o objeto não é do tipo PostoDeTrabalho, retorna false
        }

        PostoDeTrabalho posto = (PostoDeTrabalho) value;

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT codPostoTrabalho FROM PostosDeTrabalho WHERE codPostoTrabalho = ?");
        ) {
            stm.setString(1, posto.getCodPostoTrabalho());
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
     * Obtém um posto de trabalho, dado o seu código
     *
     * @param key Código do posto de trabalho
     * @return O posto de trabalho caso exista (null noutro caso)
     * @throws NullPointerException Em caso de erro - deveriam ser criadas exceções do projeto
     */
    @Override
    public PostoDeTrabalho get(Object key) {
        PostoDeTrabalho posto = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM PostosDeTrabalho WHERE codPostoTrabalho='" + key + "'")) {
            if (rs.next()) {  // O código existe na tabela
                // Obter o serviço específico usando o novo método
                String servicoEspecifico = getServicoEspecificoByCodPostoTrabalho(key.toString());

                // Reconstruir a lista de marcações associadas ao posto de trabalho
                List<Marcacao> marcacoes = getMarcacoesPosto(key.toString(), stm);

                // Reconstruir o posto de trabalho com os dados obtidos da BD
                posto = new PostoDeTrabalho(rs.getString("codPostoTrabalho"), marcacoes,
                        rs.getString("servicoEspecificoPosto"));
            }
        } catch (SQLException e) {
            // Erro na base de dados!
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return posto;
    }

    /**
     * Obtém a lista de marcações associadas a um posto de trabalho
     *
     * @param codPostoTrabalho Código do posto de trabalho
     * @param stm              Statement para executar consultas SQL
     * @return Lista de marcações associadas ao posto de trabalho
     * @throws SQLException Em caso de erro na consulta SQL
     */
    private List<Marcacao> getMarcacoesPosto(String codPostoTrabalho, Statement stm) throws SQLException {
        List<Marcacao> marcacoes = new ArrayList<>();
        String sql = "SELECT * FROM Marcacoes WHERE codPostoTrabalho='" + codPostoTrabalho + "'";
        try (ResultSet rs = stm.executeQuery(sql)) {
            while (rs.next()) {
                Marcacao marcacao = new Marcacao(rs.getString("horaMarc"), rs.getString("matricula"), rs.getString("estado"), rs.getString("codPostoTrabalho"));
                marcacoes.add(marcacao);
            }
        }
        return marcacoes;
    }

    /**
     * Obtém o serviço específico de um posto de trabalho, dado o seu código.
     *
     * @param codPostoTrabalho Código do posto de trabalho
     * @return O serviço específico do posto de trabalho, ou null se não encontrado
     * @throws RuntimeException Em caso de erro durante a consulta no banco de dados
     */
    public String getServicoEspecificoByCodPostoTrabalho(String codPostoTrabalho) {
        String servicoEspecifico = null;

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT servicoEspecificoPosto FROM PostosDeTrabalho WHERE codPostoTrabalho = ?")) {

            stm.setString(1, codPostoTrabalho);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    servicoEspecifico = rs.getString("servicoEspecificoPosto");
                }
            }
        } catch (SQLException e) {
            // Erro na base de dados!
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter o serviço específico do posto de trabalho.", e);
        }

        return servicoEspecifico;
    }

    public List<String> getHorariosMarcados(String codPostoTrabalho) {
        List<String> horariosMarcados = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT horaMarc FROM Marcacoes WHERE codPostoTrabalho=?")) {

            stm.setString(1, codPostoTrabalho);

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    // Formatar a data sem a parte fracionada (.0)
                    String horaMarcada = rs.getTimestamp("horaMarc").toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    horariosMarcados.add(horaMarcada);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter horários marcados.", e);
        }

        return horariosMarcados;
    }



    /**
     * Insere um novo posto de trabalho na base de dados, associando marcações e informações específicas.
     *
     * Esta implementação utiliza uma transação para garantir a atomicidade das operações no banco de dados.
     * Em caso de falha em qualquer parte da inserção, a transação é revertida (rollback), mantendo a consistência dos dados.
     *
     * @param key   O código identificador do posto de trabalho.
     * @param value O objeto PostoDeTrabalho a ser inserido no banco de dados.
     * @return O objeto PostoDeTrabalho inserido, ou null em caso de falha.
     * @throws RuntimeException Em caso de erro durante a inserção no banco de dados.
     */


    @Override
    public PostoDeTrabalho put(String key, PostoDeTrabalho value) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            // Inicia uma transação
            conn.setAutoCommit(false);

            try {
                // Adiciona o posto de trabalho na tabela PostosDeTrabalho
                insertPostoDeTrabalho(conn, value);

                // Adiciona as marcações associadas ao posto de trabalho
                for (Marcacao marcacao : value.getListaServicoPosto()) {
                    int idMarcacao = insertMarcacao(conn, marcacao);
                    insertMarcacaoPostoDeTrabalho(conn, idMarcacao, value.getCodPostoTrabalho());
                }

                // Comita a transação
                conn.commit();
            } catch (SQLException e) {
                // Em caso de erro, faz rollback
                conn.rollback();
                throw new RuntimeException("Erro ao adicionar posto de trabalho na tabela.", e);
            } finally {
                // Restaura o modo de commit automático
                conn.setAutoCommit(true);
            }

            return value;
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao adicionar posto de trabalho na tabela.", e);
        }
    }

    private void insertPostoDeTrabalho(Connection conn, PostoDeTrabalho value) throws SQLException {
        String insertPostoDeTrabalhoSQL = "INSERT INTO PostosDeTrabalho(codPostoTrabalho, servicoEspecificoPosto) VALUES (?, ?)";
        try (PreparedStatement pstm = conn.prepareStatement(insertPostoDeTrabalhoSQL)) {
            pstm.setString(1, value.getCodPostoTrabalho());
            pstm.setString(2, value.getServicoEspecificoPosto());
            pstm.executeUpdate();
        }
    }

    private int insertMarcacao(Connection conn, Marcacao marcacao) throws SQLException {
        String insertMarcacaoSQL = "INSERT INTO Marcacoes(horaMarc, matricula) VALUES (?, ?)";
        try (PreparedStatement pstm = conn.prepareStatement(insertMarcacaoSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setTimestamp(1, Timestamp.valueOf(marcacao.getHoraMarc()));
            pstm.setString(2, marcacao.getMatricula());
            pstm.executeUpdate();

            // Recupera a chave gerada automaticamente (se houver)
            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }


        throw new SQLException("Erro ao obter ID da marcação.");
    }

    private void insertMarcacaoPostoDeTrabalho(Connection conn, int idMarcacao, String codPostoTrabalho) throws SQLException {
        String insertMarcacaoPostoDeTrabalhoSQL = "INSERT INTO MarcacaoPostoDeTrabalho(idMarcacao, codPostoTrabalho) VALUES (?, ?)";
        try (PreparedStatement pstm = conn.prepareStatement(insertMarcacaoPostoDeTrabalhoSQL)) {
            pstm.setInt(1, idMarcacao);
            pstm.setString(2, codPostoTrabalho);
            pstm.executeUpdate();
        }
    }

    /**
     * Remove um posto de trabalho, dado o seu código identificador, incluindo as marcações associadas.
     *
     * @param key Código identificador do posto de trabalho a remover.
     * @return O posto de trabalho removido, ou null se não existir.
     * @throws RuntimeException Em caso de erro durante a remoção na base de dados
     */
    @Override
    public PostoDeTrabalho remove(Object key) {
        PostoDeTrabalho postoRemovido = this.get(key);

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             PreparedStatement pstmMarcacoes = conn.prepareStatement("DELETE FROM Marcacoes WHERE codPostoTrabalho=?");
             PreparedStatement pstmPosto = conn.prepareStatement("DELETE FROM PostosDeTrabalho WHERE codPostoTrabalho=?")) {

            // Remover as marcações associadas ao posto de trabalho
            pstmMarcacoes.setString(1, key.toString());
            pstmMarcacoes.executeUpdate();

            // Remover o posto de trabalho
            pstmPosto.setString(1, key.toString());
            pstmPosto.executeUpdate();

        } catch (SQLException e) {
            // Erro no banco de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao remover posto de trabalho e as suas marcações associadas.", e);
        }

        return postoRemovido;
    }



    /**
     * Adiciona um conjunto de postos de trabalho à base de dados, incluindo as marcações associadas.
     *
     * @param postosDeTrabalho Os postos de trabalho a adicionar.
     * @throws RuntimeException Em caso de erro durante a adição no banco de dados.
     */
    @Override
    public void putAll(Map<? extends String, ? extends PostoDeTrabalho> postosDeTrabalho) {
        for (PostoDeTrabalho posto : postosDeTrabalho.values()) {
            this.put(posto.getCodPostoTrabalho(), posto);
        }
    }

    /**
     * Apagar todos os postos de trabalho, removendo as marcações associadas.
     *
     * @throws NullPointerException Em caso de erro durante a limpeza dos postos de trabalho.
     */
    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {


            // Remover as marcações associadas a todos os postos de trabalho
            stm.executeUpdate("UPDATE Marcacoes SET codPostoTrabalho = NULL");


            // Limpar todos os postos de trabalho
            stm.executeUpdate("TRUNCATE PostosDeTrabalho");

        } catch (SQLException e) {
            // Erro no banco de dados
            e.printStackTrace();
            throw new NullPointerException("Erro durante a limpeza dos postos de trabalho: " + e.getMessage());
        }
    }

    /**
     * Obtém um conjunto de códigos identificadores de postos de trabalho.
     *
     * @return Conjunto de códigos identificadores de postos de trabalho.
     */
    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT codPostoTrabalho FROM PostosDeTrabalho")) {

            while (rs.next()) {
                keys.add(rs.getString("codPostoTrabalho"));
            }

        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter códigos identificadores de postos de trabalho.", e);
        }

        return keys;
    }


    /**
     * Obtém uma coleção de todos os postos de trabalho da base de dados.
     *
     * @return Coleção de todos os postos de trabalho.
     */
    @Override
    public Collection<PostoDeTrabalho> values() {
        Collection<PostoDeTrabalho> res = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT codPostoTrabalho FROM PostosDeTrabalho")) {

            while (rs.next()) {
                String codPostoTrabalho = rs.getString("codPostoTrabalho");
                PostoDeTrabalho posto = this.get(codPostoTrabalho);
                res.add(posto);
            }

        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter postos de trabalho da base de dados.", e);
        }

        return res;
    }


    @Override
    public Set<Entry<String, PostoDeTrabalho>> entrySet() {
        throw new NullPointerException("public Set<Map.Entry<String,Circuito>> entrySet() not implemented!");
    }

    public void imprimirDadosPostosDeTrabalho() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM PostosDeTrabalho")) {

            System.out.println("Dados dos Postos de Trabalho:");

            while (rs.next()) {
                System.out.println("Código do Posto: " + rs.getString("codPostoTrabalho"));
                System.out.println("Serviço Específico: " + rs.getString("servicoEspecificoPosto"));
                // Imprimir outros campos, se necessário
                System.out.println("-------------------------");
            }
        } catch (SQLException e) {
            // Tratar erro no banco de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter e imprimir dados dos postos de trabalho.", e);
        }
    }

    // metodo get das marcacoes de um determinado posto de trabalho
    public List<Marcacao> getMarcacoesPosto(String codPostoTrabalho) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT DATE_FORMAT(horaMarc, '%Y-%m-%d %H:%i:%s') AS horaMarc, matricula, estado FROM Marcacoes WHERE codPostoTrabalho=?")) {

            stm.setString(1, codPostoTrabalho);

            List<Marcacao> marcacoes = new ArrayList<>();
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    String horaMarcada = rs.getString("horaMarc");
                    String matricula = rs.getString("matricula");
                    String estado = rs.getString("estado");

                    Marcacao marcacao = new Marcacao(horaMarcada, matricula, estado, codPostoTrabalho);
                    marcacoes.add(marcacao);
                }
            }
            return marcacoes;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter marcações do posto de trabalho " + codPostoTrabalho, e);
        }
    }

    public int insertMarcacaoCod(String horaServico, String matricula, String servico, List<String> horariosDisponiveis) throws SQLException {
        // Obter o codPostoTrabalho correspondente ao serviço
        String codPostoTrabalho = getCodPostoTrabalhoByServicoEspecifico(servico);
        Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);

        // Criar um objeto Marcacao
        String estado = "pendente";
        Marcacao marcacao = new Marcacao(horaServico, matricula, estado, codPostoTrabalho);

        if (!horariosDisponiveis.contains(horaServico)){
            System.out.println("A hora inserida não é válida. Selecione um dos horários acima listados.");
            return 0;
        }

        // Verificar se já existe uma marcação para a hora e o posto de trabalho especificados
        if (existeMarcacaoParaHoraEPosto(conn, marcacao.getHoraMarc(), codPostoTrabalho)) {
            System.out.println("Não existe disponibilidade a essa hora, selecione um dos horários acima listados.");
            return 0;
        }

        // Verificar se o mesmo veículo tem uma marcação para a mesma hora em outro posto
        if (existeMarcacaoParaVeiculoEOutroPosto(conn, marcacao.getHoraMarc(), marcacao.getMatricula(), codPostoTrabalho)) {
            System.out.println("O veículo com a matricula indicada já possui um serviço agendado para esse horário num outro posto.");
            return 0;
        }

        // Definindo a consulta SQL para inserção de uma marcação
        String insertMarcacaoSQL = "INSERT INTO Marcacoes(horaMarc, matricula, codPostoTrabalho, estado) VALUES (?, ?, ?, ?)";

        // Usando try-with-resources para garantir o fechamento automático do PreparedStatement
        try (PreparedStatement pstm = conn.prepareStatement(insertMarcacaoSQL, Statement.RETURN_GENERATED_KEYS)) {
            // Definindo os valores dos parâmetros na consulta preparada
            pstm.setTimestamp(1, Timestamp.valueOf(marcacao.getHoraMarc()));  // Parâmetro 1: horaMarc
            pstm.setString(2, marcacao.getMatricula());  // Parâmetro 2: matricula
            pstm.setString(3, codPostoTrabalho);  // Parâmetro 3: codPostoTrabalho
            pstm.setString(4, estado);  // Parâmetro 4: estado

            // Executando a inserção
            pstm.executeUpdate();
        }

        return 1;
    }



    /**
     * @return código do posto de trabalho com aquele serviço associado
     */
    public String getCodPostoTrabalhoByServicoEspecifico(String servicoEspecifico) {
        String codPostoTrabalho = null;

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT codPostoTrabalho FROM PostosDeTrabalho WHERE servicoEspecificoPosto = ?")) {

            stm.setString(1, servicoEspecifico);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    codPostoTrabalho = rs.getString("codPostoTrabalho");
                }
            }
        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter codPostoTrabalho pelo serviço específico.", e);
        }

        return codPostoTrabalho;
    }

    public String getCodServicoByCodPostoTrabalho(String codPostoTrabalho) {
        String codServico = null;
        String servico = null;

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT servicoEspecificoPosto FROM PostosDeTrabalho WHERE codPostoTrabalho = ?")) {

            // Definir o parâmetro da consulta preparada
            stm.setString(1, codPostoTrabalho);

            // Executar a consulta e obter o resultado
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    // Se houver um resultado, obter o código do serviço
                    servico = rs.getString("servicoEspecificoPosto");
                    ServicoDAO servicoDAO = ServicoDAO.getInstance();
                    codServico = servicoDAO.getCodigoByNome(servico);
                }
            }

        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter código do serviço por código do posto de trabalho.", e);
        }

        return codServico;
    }



    /**
     * Verifica se já existe uma marcação para a hora e o posto de trabalho especificados.
     */
    private boolean existeMarcacaoParaHoraEPosto(Connection conn, LocalDateTime horaMarc, String codPostoTrabalho) throws SQLException {
        String verificaMarcacaoSQL = "SELECT COUNT(*) FROM Marcacoes WHERE horaMarc = ? AND codPostoTrabalho = ?";

        try (PreparedStatement pstm = conn.prepareStatement(verificaMarcacaoSQL)) {
            pstm.setTimestamp(1, Timestamp.valueOf(horaMarc));
            pstm.setString(2, codPostoTrabalho);

            try (ResultSet rs = pstm.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Verifica se o mesmo veículo tem uma marcação para a mesma hora em outro posto.
     *
     * @return true se já existe uma marcação para a mesma hora em outro posto, false caso contrário
     */
    private boolean existeMarcacaoParaVeiculoEOutroPosto(Connection conn, LocalDateTime horaMarc, String matricula, String codPostoTrabalho) throws SQLException {
        String verificaMarcacaoSQL = "SELECT COUNT(*) FROM Marcacoes WHERE horaMarc = ? AND matricula = ? AND codPostoTrabalho != ?";

        try (PreparedStatement pstm = conn.prepareStatement(verificaMarcacaoSQL)) {
            pstm.setTimestamp(1, Timestamp.valueOf(horaMarc));
            pstm.setString(2, matricula);
            pstm.setString(3, codPostoTrabalho);

            try (ResultSet rs = pstm.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void removerMarcacao(LocalDateTime horaMarc, String matricula, String codPostoTrabalho) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("DELETE FROM Marcacoes WHERE horaMarc = ? AND matricula = ? AND codPostoTrabalho = ?")) {

            // Definir os parâmetros da consulta preparada
            stm.setTimestamp(1, Timestamp.valueOf(horaMarc));
            stm.setString(2, matricula);
            stm.setString(3, codPostoTrabalho);

            // Executar a remoção na tabela Marcacoes
            int rowsAffected = stm.executeUpdate();

        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao remover marcação.", e);
        }
    }

    public void imprimirMarcacoesPorMatricula(String matricula) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT * FROM Marcacoes WHERE matricula = ?")) {

            // Definir o parâmetro da consulta preparada
            stm.setString(1, matricula);

            // Executar a consulta e obter o resultado
            try (ResultSet rs = stm.executeQuery()) {
                System.out.println("Marcacoes para a Matricula " + matricula + ":");

                while (rs.next()) {
                    Timestamp horaMarc = rs.getTimestamp("horaMarc");
                    String codPostoTrabalho = rs.getString("codPostoTrabalho");
                    String estado = rs.getString("estado");

                    System.out.println("Hora da Marcacao: " + horaMarc);
                    System.out.println("Codigo do Posto de Trabalho: " + codPostoTrabalho);
                    System.out.println("Estado: " + estado);
                    System.out.println("-------------------------");
                }
            }

        } catch (SQLException e) {
            // Tratar erro na base de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao obter e imprimir marcacoes por matricula.", e);
        }
    }

    public boolean verificarMarcacoesVeiculo(String matricula) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM Marcacoes WHERE matricula = ? AND estado = 'pendente';";

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, matricula);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    return count > 0;
                }
            }
        }

        return false;
    }





    public static void insertMockData() throws SQLException{
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);Statement stm = conn.createStatement()){
            var rs = stm.executeQuery("SELECT COUNT(*) AS c FROM PostosDeTrabalho;");
            rs.next();
            if (rs.getInt("c") == 0) {
                stm.executeUpdate("INSERT INTO PostosDeTrabalho(codPostoTrabalho, servicoEspecificoPosto) VALUES ('01','Check-up');");
                stm.executeUpdate("INSERT INTO PostosDeTrabalho(codPostoTrabalho, servicoEspecificoPosto) VALUES ('02','Troca de oleo');");
                stm.executeUpdate("INSERT INTO PostosDeTrabalho(codPostoTrabalho, servicoEspecificoPosto) VALUES ('03','Troca de filtros');");
                stm.executeUpdate("INSERT INTO PostosDeTrabalho(codPostoTrabalho, servicoEspecificoPosto) VALUES ('04','Avalição do desempenho das baterias');");
                stm.executeUpdate("INSERT INTO PostosDeTrabalho(codPostoTrabalho, servicoEspecificoPosto) VALUES ('05','Substituição das velas de incandescência');");
                stm.executeUpdate("INSERT INTO PostosDeTrabalho(codPostoTrabalho, servicoEspecificoPosto) VALUES ('06','Substituição da válvula do acelerador');");
                stm.executeUpdate("INSERT INTO PostosDeTrabalho(codPostoTrabalho, servicoEspecificoPosto) VALUES ('07','Troca da bateria de arranque');");
            }
            rs = stm.executeQuery("SELECT COUNT(*) AS c FROM Marcacoes;");
            rs.next();
            if  (rs.getInt("c") == 0){
                stm.executeUpdate("INSERT INTO Marcacoes(idMarcacao, horaMarc, matricula, codPostoTrabalho, estado) VALUES ('001', '2023-01-01 14:00:00', '01AA01', '03', 'pendente');");
                stm.executeUpdate("INSERT INTO Marcacoes(idMarcacao, horaMarc, matricula, codPostoTrabalho, estado) VALUES ('002', '2023-01-01 15:00:00', '01AA01', '01', 'pendente');");
                stm.executeUpdate("INSERT INTO Marcacoes(idMarcacao, horaMarc, matricula, codPostoTrabalho, estado) VALUES ('003', '2023-01-01 14:00:00', '02BB02', '02', 'pendente');");
                stm.executeUpdate("INSERT INTO Marcacoes(idMarcacao, horaMarc, matricula, codPostoTrabalho, estado) VALUES ('004', '2023-01-01 15:00:00', '03CC03', '03', 'pendente');");
                stm.executeUpdate("INSERT INTO Marcacoes(idMarcacao, horaMarc, matricula, codPostoTrabalho, estado) VALUES ('005', '2023-01-01 15:00:00', '04DD04', '04', 'pendente');");
                stm.executeUpdate("INSERT INTO Marcacoes(idMarcacao, horaMarc, matricula, codPostoTrabalho, estado) VALUES ('006', '2023-01-01 16:00:00', '05EE05', '05', 'pendente');");
                stm.executeUpdate("INSERT INTO Marcacoes(idMarcacao, horaMarc, matricula, codPostoTrabalho, estado) VALUES ('007', '2023-01-01 17:00:00', '09II09', '03', 'pendente');");
                stm.executeUpdate("INSERT INTO Marcacoes(idMarcacao, horaMarc, matricula, codPostoTrabalho, estado) VALUES ('008', '2023-01-01 18:00:00', '03CC03', '04', 'pendente');");
                stm.executeUpdate("INSERT INTO Marcacoes(idMarcacao, horaMarc, matricula, codPostoTrabalho, estado) VALUES ('009', '2023-01-01 18:00:00', '09II09', '05', 'pendente');");

            }
        }
    }

}