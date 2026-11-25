package br.edu.ifgoiano.smartgrid.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica responsável por centralizar a criação de conexões JDBC com o banco
 * de dados relacional <b>H2 em memória</b>.
 * <p>
 * O banco permanece ativo durante toda a execução da JVM graças ao parâmetro
 * {@code DB_CLOSE_DELAY=-1}, garantindo que as tabelas e registros não sejam
 * perdidos entre conexões sucessivas do {@link ConsumoDAOJBC}.
 *
 * <pre>
 * try (Connection conn = ConnectionFactory.getConnection()) {
 *     // operações JDBC
 * }
 * </pre>
 *
 * @author Vinícius Soares de Melo
 * @version 1.0
 */
public final class ConnectionFactory {

    /** Nome padrão do banco H2 em memória utilizado pela aplicação. */
    public static final String BANCO_PADRAO = "smartgrid";

    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private ConnectionFactory() {
    }

    /**
     * Abre uma conexão JDBC com o banco H2 padrão ({@value #BANCO_PADRAO}).
     *
     * @return conexão ativa com o banco H2 em memória
     * @throws SQLException se ocorrer erro ao estabelecer a conexão JDBC
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(BANCO_PADRAO);
    }

    /**
     * Abre uma conexão JDBC com um banco H2 em memória cujo nome seja
     * informado pelo chamador (útil para testes isolados).
     *
     * @param nomeBanco nome do banco contendo apenas letras, números e underline
     * @return conexão ativa com o banco H2 em memória
     * @throws IllegalArgumentException se o nome do banco for nulo ou inválido
     * @throws SQLException             se ocorrer erro ao estabelecer a conexão JDBC
     */
    public static Connection getConnection(String nomeBanco) throws SQLException {
        if (nomeBanco == null || !nomeBanco.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Nome de banco inválido: " + nomeBanco);
        }
        String url = "jdbc:h2:mem:" + nomeBanco + ";DB_CLOSE_DELAY=-1";
        return DriverManager.getConnection(url, USER, PASSWORD);
    }
}
