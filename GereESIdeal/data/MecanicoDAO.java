package GereESIdeal.data;

import GereESIdeal.business.Mecanico;

import java.sql.*;
import java.util.*;

/**
 * Versão  de um DAO para Mecanicos
 *
 * Tabelas a criar na BD: ver método getInstance
 *
 */

public class MecanicoDAO implements Map<String, Mecanico>{
    // Singleton para garantir apenas uma instância da classe
    private static MecanicoDAO singleton = null;


    private MecanicoDAO(){
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Criação da tabela de competências
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS competencias (" +
                    "idCompetencia INT AUTO_INCREMENT PRIMARY KEY," +
                    "competencia VARCHAR(45) NOT NULL)");

            // Criação da tabela de mecânicos
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS mecanicos (" +
                    "codMecanico varchar(45) NOT NULL PRIMARY KEY," +
                    "password varchar(45) NOT NULL)");

            // Criação da tabela de relação entre mecânicos e competências
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS mecanico_competencia (" +
                    "codMecanico varchar(45) NOT NULL," +
                    "idCompetencia INT NOT NULL," +
                    "PRIMARY KEY (codMecanico, idCompetencia)," +
                    "FOREIGN KEY (codMecanico) REFERENCES mecanicos (codMecanico)," +
                    "FOREIGN KEY (idCompetencia) REFERENCES competencias (idCompetencia))");
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

    public static MecanicoDAO getInstance() {
        if (MecanicoDAO.singleton == null) {
            MecanicoDAO.singleton = new MecanicoDAO();
        }
        return MecanicoDAO.singleton;
    }

    // Método adicional para construir a instância Singleton se necessário

    public static void buildInstance() {
        if (MecanicoDAO.singleton == null) {
            MecanicoDAO.singleton = new MecanicoDAO();
        }
    }

    //TODO: ver se isto é mesmo necessário ou se mandamos com o caraças

    //@Override
        /* public int hashCode() {
            int result = 17; // Escolha de um número primo inicial
            result = 31 * result + (codMecanico != null ? codMecanico.hashCode() : 0);
            return result;
        }

        @Override
        public int hashCode() {
            int lHashCode = 0;
            if ( lHashCode == 0 ) {
                lHashCode = super.hashCode();
            }
            return lHashCode;
        }

*/
    /**
     * @return número de mecanicos na base de dados
     */
    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM mecanicos")) {
            if(rs.next()) {
                i = rs.getInt(1);
            }
        }
        catch (Exception e) {
            // Erro a criar tabela...
            e.printStackTrace();
            throw new NullPointerException(e.getMessage()); //TODO: ver onde estão definidas estas mensagens de erro
        }
        return i;
    }

    /**
     * Método que verifica se existem mecanicos
     *
     * @return true se existirem 0 mecanicos
     */
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Método que verifica se um codMecanico de mecanico existe na base de dados
     *
     * @param key codMecanico do mecanico
     * @return true se o mecanico existe
     * @throws NullPointerException Em caso de erro
     */


    @Override
    public boolean containsKey(Object key) {

        // Inicializaçãp da variável para armazenar o resultado da verificação
        boolean r;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             // consulta SQL para verificar a existência do mecânico com o código fornecido
             ResultSet rs =
                     stm.executeQuery("SELECT codMecanico FROM mecanicos WHERE codMecanico='"+key.toString()+"'")) {
            r = rs.next();
        } catch (SQLException e) {
            // Database error!
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }


    /**
     * Verifica se um mecânico existe na base de dados
     *
     * @param value Mecânico a ser verificado
     * @return true se o mecânico existe
     * @throws NullPointerException Em caso de erro
     */
    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof Mecanico)) {
            return false; // Se o objeto não é do tipo Mecanico, retorna false
        }

        Mecanico mecanico = (Mecanico) value;

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("SELECT codMecanico FROM mecanicos WHERE codMecanico = ?");
        ) {
            stm.setString(1, mecanico.getCodMecanico());
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
     * Obtém um Mecanico da base de dados dado o seu código
     *
     * @param key codMecanico do mecanico
     * @return o mecanico caso exista (null noutro caso)
     * @throws NullPointerException Em caso de erro
     */

    //TODO: ESTE MÉTODO NAO ESTÁ EXATAMENTE IGUAL AO DOS STRS
    /**
     * Obtém um Mecanico da base de dados dado o seu código
     *
     * @param key codMecanico do mecanico
     * @return o mecanico caso exista (null noutro caso)
     * @throws NullPointerException Em caso de erro
     */
    @Override
    public Mecanico get(Object key) {
        Mecanico mecanico = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            String selectMecanicoSQL = "SELECT * FROM mecanicos WHERE codMecanico=?";
            try (PreparedStatement pstm = conn.prepareStatement(selectMecanicoSQL)) {
                pstm.setString(1, key.toString());
                try (ResultSet rs = pstm.executeQuery()) {
                    if (rs.next()) {
                        // Obtém as informações básicas do mecânico
                        String codMecanico = rs.getString("codMecanico");
                        String password = rs.getString("password");

                        // Obtém as competências associadas ao mecânico
                        List<String> competencias = getCompetenciasDoMecanico(codMecanico);

                        // Cria o objeto Mecanico com as informações obtidas
                        mecanico = new Mecanico(codMecanico, competencias, password);
                    }
                }
            }
        } catch (SQLException e) {
            // Erro na base de dados
            e.printStackTrace();
            throw new NullPointerException("Erro ao obter o mecânico da base de dados.");
        }
        return mecanico;
    }

    /**
     * Obtém as competências associadas a um mecânico.
     *
     * @param codMecanico Código do mecânico.
     * @return Lista de competências do mecânico.
     * @throws SQLException Em caso de erro na execução da consulta SQL.
     */
    public List<String> getCompetenciasDoMecanico(String codMecanico) throws SQLException {
        List<String> competencias = new ArrayList<>();
        String selectCompetenciasSQL = "SELECT competencia FROM competencias " +
                "JOIN mecanico_competencia ON competencias.idCompetencia = mecanico_competencia.idCompetencia " +
                "WHERE mecanico_competencia.codMecanico = ?";
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement pstm = conn.prepareStatement(selectCompetenciasSQL)) {
            pstm.setString(1, codMecanico);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    competencias.add(rs.getString("competencia"));
                }
            }
        }
        return competencias;
    }



    /**
     * Adiciona um Mecanico à base de dados
     *
     * @param key   codMecanico do mecanico
     * @param value Mecanico a adicionar
     * @return o mecânico adicionado (null se não foi possível adicionar)
     * @throws NullPointerException Em caso de erro
     */
    //TODO: VER SE MANTEMOS O PUT MESMO NAO TENDO NOS DE ADICIONAR MECANICOS
    @Override
    public Mecanico put(String key, Mecanico value) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            // Adiciona o mecânico na tabela mecanicos
            String insertMecanicoSQL = "INSERT INTO mecanicos(codMecanico, password) VALUES (?, ?)";
            try (PreparedStatement pstm = conn.prepareStatement(insertMecanicoSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstm.setString(1, value.getCodMecanico());
                pstm.setString(2, value.getPassword());
                pstm.executeUpdate();

                // Recupera a chave gerada automaticamente (se houver)
                try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Se a chave foi gerada automaticamente, atualiza o código do mecânico
                        value.setCodMecanico(generatedKeys.getString(1));
                    }
                }
            }

            // Adiciona as competências na tabela competencias e a relação na tabela mecanico_competencia
            String insertCompetenciaSQL = "INSERT INTO competencias(competencia) VALUES (?)";
            String insertMecanicoCompetenciaSQL = "INSERT INTO mecanico_competencia(codMecanico, idCompetencia) VALUES (?, ?)";
            for (String competencia : value.getCompetencias()) {
                // Adiciona a competência na tabela competencias
                try (PreparedStatement pstm = conn.prepareStatement(insertCompetenciaSQL, Statement.RETURN_GENERATED_KEYS)) {
                    pstm.setString(1, competencia);
                    pstm.executeUpdate();

                    // Recupera a chave gerada automaticamente (se houver)
                    try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int idCompetencia = generatedKeys.getInt(1);

                            // Adiciona a relação entre mecânico e competência na tabela mecanico_competencia
                            try (PreparedStatement pstmRelacao = conn.prepareStatement(insertMecanicoCompetenciaSQL)) {
                                pstmRelacao.setString(1, value.getCodMecanico());
                                pstmRelacao.setInt(2, idCompetencia);
                                pstmRelacao.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Tratar erro no banco de dados
            e.printStackTrace();
            throw new RuntimeException("Erro ao adicionar mecânico na tabela.");
        }
        return value;
    }


    /**
     * Remover um mecanico, dado o seu codMecanico
     *
     * @param key codMecanico do mecanico a remover
     * @return o mecanico removido
     * @throws RuntimeException Em caso de erro
     */
    //TODO: VER SE MANTEMOS O remove MESMO NAO TENDO NOS DE remover MECANICOS
    @Override
    public Mecanico remove(Object key) {
        Mecanico mecanicoRemovido = this.get(key);

        if (mecanicoRemovido != null) {
            try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD); //TODO VER SE ISTO REMOVE AS competencias do mecanico da tabela mecanico competencias
                 Statement stm = conn.createStatement()) {
                // Apagar o mecanico
                stm.executeUpdate("DELETE FROM mecanicos WHERE codMecanico='" + key + "'");
            } catch (SQLException e) {
                // Tratar erro no banco de dados
                e.printStackTrace();
                throw new RuntimeException("Erro ao remover mecanico da tabela.");
            }
        }

        return mecanicoRemovido;
    }


    /**
     * Adiciona um conjunto de mecânicos à base de dados.
     *
     * @param mecanicos Um mapa contendo os mecânicos a serem adicionados, onde a chave é o código do mecânico.
     * @throws NullPointerException Em caso de erro durante a execução do método.
     */
    //TODO: VER SE MANTEMOS O MESMO NAO TENDO NOS DE adicionar MECANICOS
    @Override
    public void putAll(Map<? extends String, ? extends Mecanico> mecanicos) {
        // Itera sobre os mecânicos fornecidos no mapa
        for (Mecanico m : mecanicos.values()) {
            // Chama o método put para adicionar cada mecânico à base de dados
            this.put(m.getCodMecanico(), m);
        }
    }


    /**
     * Apaga todos os mecânicos da base de dados.
     *
     * @throws NullPointerException Em caso de erro durante a execução do método.
     */
    //TODO: VER SE MANTEMOS O MESMO NAO TENDO NOS DE eliminar MECANICO
    //TODO:VER SE ELE RETIRA TODAS AS INFOS DE TODAS AS TABELAS
    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            // Atualiza a referência à turma para nulo em todos os alunos
            stm.executeUpdate("UPDATE mecanicos SET Mecanico=NULL");
            // Elimina todos os registros da tabela de mecânicos
            stm.executeUpdate("TRUNCATE mecanicos");
        } catch (SQLException e) {
            // Database error! Imprime o stack trace, lança uma exceção e imprime a mensagem de erro
            e.printStackTrace();
            throw new NullPointerException("Erro ao limpar a tabela de mecânicos: " + e.getMessage());
        }
    }

    /**
     * Obtém um conjunto de códigos de mecânicos presentes na base de dados.
     *
     * @return Um conjunto de códigos de mecânicos.
     * @throws NullPointerException Em caso de erro durante a execução do método.
     */
    @Override
    public Set<String> keySet() {
        Set<String> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT codMecanico FROM mecanicos")) {
            // ResultSet com os códigos de todos os mecânicos
            while (rs.next()) {
                String codMecanico = rs.getString("codMecanico"); // Obtemos um código de mecânico do ResultSet
                res.add(codMecanico); // Adiciona o código de mecânico ao resultado.
            }
        } catch (Exception e) {
            // Erro no banco de dados! Imprime o stack trace, lança uma exceção e imprime a mensagem de erro
            e.printStackTrace();
            throw new NullPointerException("Erro ao obter o conjunto de códigos de mecânicos: " + e.getMessage());
        }
        return res;
    }

    /**
     * @return Todos os mecânicos da base de dados
     */
    @Override
    public Collection<Mecanico> values() {
        Collection<Mecanico> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT codMecanico FROM mecanicos")) {
            while (rs.next()) {
                String codMecanico = rs.getString("codMecanico"); // Obtemos um código de mecânico do ResultSet
                Mecanico mec = this.get(codMecanico);       // Utilizamos o get para construir os mecânicos um a um
                res.add(mec);                          // Adiciona o mecânico ao resultado.
            }
        } catch (Exception e) {
            // Tratamento de erros de banco de dados
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }



    @Override
    public Set<Entry<String, Mecanico>> entrySet() {
        throw new NullPointerException("public Set<Map.Entry<String,Circuito>> entrySet() not implemented!");
    }

    public static void insertMockData() throws SQLException{
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);Statement stm = conn.createStatement()){
            var rs = stm.executeQuery("SELECT COUNT(*) AS c FROM competencias;");
            rs.next();
            if (rs.getInt("c") ==0){
                stm.executeUpdate("INSERT INTO competencias(idCompetencia,competencia) VALUES ('01','Check-up');");
                stm.executeUpdate("INSERT INTO competencias(idCompetencia,competencia) VALUES ('02','Troca de oleo');");
                stm.executeUpdate("INSERT INTO competencias(idCompetencia,competencia) VALUES ('03','Troca de filtros');");
                stm.executeUpdate("INSERT INTO competencias(idCompetencia,competencia) VALUES ('04','Avalição do desempenho das baterias');");
                stm.executeUpdate("INSERT INTO competencias(idCompetencia,competencia) VALUES ('05','Substituição das velas de incandescência');");
                stm.executeUpdate("INSERT INTO competencias(idCompetencia,competencia) VALUES ('06','Substituição da válvula do acelerador');");
                stm.executeUpdate("INSERT INTO competencias(idCompetencia,competencia) VALUES ('07','Troca da bateria de arranque');");
            }
            rs = stm.executeQuery("SELECT COUNT(*) AS c FROM mecanicos;");
            rs.next();
            if  (rs.getInt("c") ==0){
                stm.executeUpdate("INSERT INTO mecanicos(codMecanico,password) VALUES ('001', '123456');");
                stm.executeUpdate("INSERT INTO mecanicos(codMecanico,password) VALUES ('002', '654321');");
                stm.executeUpdate("INSERT INTO mecanicos(codMecanico,password) VALUES ('003', '214365');");
                stm.executeUpdate("INSERT INTO mecanicos(codMecanico,password) VALUES ('004', '563412');");
                stm.executeUpdate("INSERT INTO mecanicos(codMecanico,password) VALUES ('005', '162534');");
                stm.executeUpdate("INSERT INTO mecanicos(codMecanico,password) VALUES ('006', '615243');");
                stm.executeUpdate("INSERT INTO mecanicos(codMecanico,password) VALUES ('007', '147258');");

            }
            rs = stm.executeQuery("SELECT COUNT(*) AS c FROM mecanico_competencia;");
            rs.next();
            if  (rs.getInt("c") ==0){
                stm.executeUpdate("INSERT INTO mecanico_competencia(codMecanico,idCompetencia) VALUES ('001', '01');");
                stm.executeUpdate("INSERT INTO mecanico_competencia(codMecanico,idCompetencia) VALUES ('002', '02');");
                stm.executeUpdate("INSERT INTO mecanico_competencia(codMecanico,idCompetencia) VALUES ('003', '03');");
                stm.executeUpdate("INSERT INTO mecanico_competencia(codMecanico,idCompetencia) VALUES ('004', '04');");
                stm.executeUpdate("INSERT INTO mecanico_competencia(codMecanico,idCompetencia) VALUES ('005', '05');");
                stm.executeUpdate("INSERT INTO mecanico_competencia(codMecanico,idCompetencia) VALUES ('006', '06');");
                stm.executeUpdate("INSERT INTO mecanico_competencia(codMecanico,idCompetencia) VALUES ('007', '07');");
            }
        }
    }


}
