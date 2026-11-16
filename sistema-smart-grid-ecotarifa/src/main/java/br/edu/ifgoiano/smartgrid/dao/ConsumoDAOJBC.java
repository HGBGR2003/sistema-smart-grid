package br.edu.ifgoiano.smartgrid.dao;

import br.edu.ifgoiano.smartgrid.model.BandeiraTarifaria;
import br.edu.ifgoiano.smartgrid.model.MedidorIndustrial;

import java.sql.Connection;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ConsumoDAOJBC {
    public void criarTabelas() {
        String sqlMedidor = "CREATE TABLE IF NOT EXISTS medidor ("
                + "mac_address VARCHAR(17) PRIMARY KEY, "
                + "endereco VARCHAR(150) NOT NULL, "
                + "tensao_atual DOUBLE NOT NULL, "
                + "status_operacional VARCHAR(20) NOT NULL"
                + ")";

        String sqlIndustrial = "CREATE TABLE IF NOT EXISTS medidor_industrial ("
                + "mac_address VARCHAR(17) PRIMARY KEY, "
                + "consumo_kwh DOUBLE NOT NULL, "
                + "fator_demanda DOUBLE NOT NULL, "
                + "bandeira_tarifaria VARCHAR(20) NOT NULL, "
                + "FOREIGN KEY (mac_address) REFERENCES medidor(mac_address)"
                + ")";

        try (Connection conn = ConnectionFactory.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sqlMedidor);
            stmt.execute(sqlIndustrial);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabelas no banco H2: " + e.getMessage(), e);
        }

    }

    public void salvar(MedidorIndustrial medidor) {
        String slqPapai = "INSERT INTO medidor (mac_address, endereco, tensao_atual, status_operacional) VALUES (?, ?, ?, ?)";

        String sqlFilha = "INSERT INTO medidor_industrial (mac_address, consumo_kwh, fator_demanda, bandeira_tarifaria) VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtPapai = conn.prepareStatement(slqPapai)) {
                stmtPapai.setString(1, medidor.getMacAddress());

                stmtPapai.setString(2, medidor.getEndereco());

                stmtPapai.setDouble(3, medidor.getTensaoAtual());

                stmtPapai.setString(4, medidor.getStatusOperacional());

                stmtPapai.executeUpdate();

            }

            try (PreparedStatement stmtFilha = conn.prepareStatement(sqlFilha)) {
                stmtFilha.setString(1, medidor.getMacAddress());

                stmtFilha.setDouble(2, medidor.getConsumoKwh());

                stmtFilha.setDouble(3, medidor.getFatorDemandaContratada());

                stmtFilha.setString(4, medidor.getBandeira().name());

                stmtFilha.executeUpdate();

            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollback) {
                    rollback.printStackTrace();
                }
            }
            throw new RuntimeException("Erro ao salvar medidor industrial: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeExption) {
                    closeExption.printStackTrace();
                }
            }
        }
    }

    public List<MedidorIndustrial> buscarTodos() {
        List<MedidorIndustrial> medidores = new ArrayList<>();

        String sql = "SELECT m.mac_address, m.endereco, mi.consumo_kwh, mi.fator_demanda, mi.bandeira_tarifaria "
                + "FROM medidor m "
                + "INNER JOIN medidor_industrial mi ON m.mac_address = mi.mac_address";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                MedidorIndustrial medidor = new MedidorIndustrial(
                        rs.getString("mac_address"),
                        rs.getString("endereco"),
                        rs.getDouble("consumo_kwh"),
                        rs.getDouble("fator_demanda"),
                        BandeiraTarifaria.valueOf(rs.getString("bandeira_tarifaria")));
                medidores.add(medidor);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar medidores industriais: " + e.getMessage(), e);
        }

        return medidores;
    }

    public MedidorIndustrial buscarPorMac(String macAddress) {
        String sql = "SELECT m.mac_address, m.endereco, mi.consumo_kwh, mi.fator_demanda, mi.bandeira_tarifaria "
                + "FROM medidor m "
                + "INNER JOIN medidor_industrial mi ON m.mac_address = mi.mac_address "
                + "WHERE m.mac_address = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, macAddress);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MedidorIndustrial(
                            rs.getString("mac_address"),
                            rs.getString("endereco"),
                            rs.getDouble("consumo_kwh"),
                            rs.getDouble("fator_demanda"),
                            BandeiraTarifaria.valueOf(rs.getString("bandeira_tarifaria")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar medidor pelo MAC " + macAddress + ": " + e.getMessage(), e);
        }

        return null;
    }

    public void atualizarConsumo(String macAddress, double novoConsumo, BandeiraTarifaria novoBandeiraTarifaria) {
        String sql = "UPDATE medidor_industrial SET consumo_kwh = ?, bandeira_tarifaria = ? WHERE mac_address = ?";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, novoConsumo);
            stmt.setString(2, novoBandeiraTarifaria.name());
            stmt.setString(3, macAddress);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar consumo do MAC " + macAddress + ": " + e.getMessage(), e);

        }
    }
}
