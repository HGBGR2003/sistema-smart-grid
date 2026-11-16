/* ============================================================================
 * VERSÃO PROVISÓRIA (STUB PARA COMPILAÇÃO E TESTES DO DAO)
 * Substituir este arquivo quando o Membro 3 enviar a versão oficial.
 * ============================================================================ */
package br.edu.ifgoiano.smartgrid.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gerenciador de conexões via JDBC para o banco H2 em memória.
 * 
 * @version 0.1-PROVISORIA (Aguardando código do Membro 3)
 */
public class ConnectionFactory {
    private static final String URL = "jdbc:h2:mem:smartgrid;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}