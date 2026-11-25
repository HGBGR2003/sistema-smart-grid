/* ============================================================================
 * VERSÃO PROVISÓRIA - MEMBRO 6 (Jeanderson) - Tech Lead / Integrador
 * Substituir ou atualizar quando o Membro 6 enviar a versão oficial.
 * ============================================================================ */
package br.edu.ifgoiano.smartgrid.view;

import br.edu.ifgoiano.smartgrid.controller.TarifacaoService;
import br.edu.ifgoiano.smartgrid.exception.RegraTarifacaoException;
import br.edu.ifgoiano.smartgrid.model.BandeiraTarifaria;
import br.edu.ifgoiano.smartgrid.model.CalculadoraTarifaria;
import br.edu.ifgoiano.smartgrid.model.MedidorIndustrial;
import br.edu.ifgoiano.smartgrid.model.MedidorInteligente;
import br.edu.ifgoiano.smartgrid.model.MedidorResidencial;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface gráfica principal (Java Swing) do <b>Sistema Smart Grid &amp; EcoTarifa</b>.
 * <p>
 * Integra todas as camadas do projeto: modelos de domínio (Membro 1),
 * polimorfismo e exceções tarifárias (Membro 2), banco H2 em memória (Membro 3),
 * persistência JDBC (Membro 4) e análises com Streams API (Membro 5).
 *
 * @author Jeanderson Honorato de Souza
 * @version 0.9-PROVISORIA
 */
public class MainView extends JFrame {

    private final TarifacaoService service;
    private final List<MedidorResidencial> residenciais = new ArrayList<>();

    private DefaultTableModel modeloTabelaIndustrial;
    private JTable tabelaIndustrial;

    private DefaultTableModel modeloTabelaResidencial;
    private JTable tabelaResidencial;

    private JTextArea areaRelatorio;

    /**
     * Constrói e inicializa a janela principal do sistema.
     */
    public MainView() {
        super("⚡ Sistema Smart Grid & EcoTarifa — IF Goiano");
        this.service = new TarifacaoService();
        this.service.inicializarBanco();
        carregarDadosIniciais();

        configurarJanela();
        atualizarTabelaIndustrial();
        atualizarTabelaResidencial();
        atualizarRelatorio();
    }

    private void carregarDadosIniciais() {
        if (service.listarMedidoresIndustriais().isEmpty()) {
            service.salvarMedidorIndustrial(new MedidorIndustrial(
                    "AA:BB:CC:01:01:01", "Distrito Industrial - Lote 12", 4200.0, 1.35, BandeiraTarifaria.VERMELHA));
            service.salvarMedidorIndustrial(new MedidorIndustrial(
                    "AA:BB:CC:01:02:02", "Av. Goiás Norte - Galpão 4", 1850.0, 1.15, BandeiraTarifaria.AMARELA));
            service.salvarMedidorIndustrial(new MedidorIndustrial(
                    "AA:BB:CC:01:03:03", "Rod. GO-060 Km 8 - Indústria Alpha", 980.0, 1.05, BandeiraTarifaria.VERDE));
        }

        residenciais.add(new MedidorResidencial(
                "DD:EE:FF:02:01:01", "Rua das Flores, 120 - Centro", 310.0, 180.0, BandeiraTarifaria.AMARELA));
        residenciais.add(new MedidorResidencial(
                "DD:EE:FF:02:02:02", "Av. Brasil, 450 - Setor Sul", 240.0, 320.0, BandeiraTarifaria.VERDE));
        residenciais.add(new MedidorResidencial(
                "DD:EE:FF:02:03:03", "Rua 7, Qd. 14 - Vila Nova", 410.0, 0.0, BandeiraTarifaria.VERMELHA));
    }

    private void configurarJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 640);
        setMinimumSize(new Dimension(880, 540));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(new Color(18, 52, 86));
        cabecalho.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titulo = new JLabel("Sistema Smart Grid & EcoTarifa");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel subtitulo = new JLabel("Monitoramento de Medidores Inteligentes, Net Metering e Banco H2 em Memória");
        subtitulo.setForeground(new Color(200, 220, 240));
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel textosTopo = new JPanel(new GridLayout(2, 1));
        textosTopo.setOpaque(false);
        textosTopo.add(titulo);
        textosTopo.add(subtitulo);
        cabecalho.add(textosTopo, BorderLayout.WEST);

        add(cabecalho, BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font("SansSerif", Font.BOLD, 13));
        abas.addTab("🏭 Medidores Industriais (H2 / JDBC)", criarAbaIndustrial());
        abas.addTab("🏠 Medidores Residenciais & Solar", criarAbaResidencial());
        abas.addTab("📊 Relatório & Streams API", criarAbaRelatorio());

        add(abas, BorderLayout.CENTER);
    }

    private JPanel criarAbaIndustrial() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"MAC Address", "Endereço", "Consumo (kWh)", "Fator Demanda", "Bandeira", "Status", "Fatura"};
        modeloTabelaIndustrial = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaIndustrial = new JTable(modeloTabelaIndustrial);
        tabelaIndustrial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaIndustrial.setRowHeight(24);
        painel.add(new JScrollPane(tabelaIndustrial), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(8, 8));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        form.setBorder(BorderFactory.createTitledBorder("Cadastrar Medidor Industrial (persiste no H2)"));

        JTextField txtMac = new JTextField(13);
        JTextField txtEnd = new JTextField(16);
        JTextField txtKwh = new JTextField(6);
        JTextField txtFator = new JTextField("1.20", 4);
        JComboBox<BandeiraTarifaria> cbBandeira = new JComboBox<>(BandeiraTarifaria.values());
        JButton btnCadastrar = new JButton("Salvar no H2");

        form.add(new JLabel("MAC:"));
        form.add(txtMac);
        form.add(new JLabel("Endereço:"));
        form.add(txtEnd);
        form.add(new JLabel("kWh:"));
        form.add(txtKwh);
        form.add(new JLabel("Fator:"));
        form.add(txtFator);
        form.add(new JLabel("Bandeira:"));
        form.add(cbBandeira);
        form.add(btnCadastrar);

        btnCadastrar.addActionListener(e -> {
            try {
                String mac = txtMac.getText().trim();
                String end = txtEnd.getText().trim();
                double kwh = Double.parseDouble(txtKwh.getText().trim().replace(",", "."));
                double fator = Double.parseDouble(txtFator.getText().trim().replace(",", "."));
                BandeiraTarifaria bandeira = (BandeiraTarifaria) cbBandeira.getSelectedItem();

                if (end.isEmpty()) {
                    throw new IllegalArgumentException("Informe o endereço.");
                }
                MedidorIndustrial novo = new MedidorIndustrial(mac, end, kwh, fator, bandeira);
                novo.calcularValorFatura(); // valida regras antes de salvar
                service.salvarMedidorIndustrial(novo);

                txtMac.setText("");
                txtEnd.setText("");
                txtKwh.setText("");
                atualizarTabelaIndustrial();
                atualizarRelatorio();
                JOptionPane.showMessageDialog(this, "Medidor industrial salvo no banco H2 com sucesso!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Informe números válidos para consumo (kWh) e fator de demanda.",
                        "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnAtualizar = new JButton("Atualizar Consumo / Bandeira");
        JButton btnComparar = new JButton("Simular Bandeiras");

        btnAtualizar.addActionListener(e -> atualizarSelecionadoIndustrial());
        btnComparar.addActionListener(e -> simularBandeirasSelecionado());

        acoes.add(btnAtualizar);
        acoes.add(btnComparar);

        rodape.add(form, BorderLayout.CENTER);
        rodape.add(acoes, BorderLayout.SOUTH);
        painel.add(rodape, BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarAbaResidencial() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {
                "MAC Address", "Endereço", "Consumo (kWh)", "Solar (kWh)",
                "Saldo (kWh)", "Créditos (kWh)", "Perfil", "Bandeira", "Disjuntor", "Status", "Fatura"
        };
        modeloTabelaResidencial = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaResidencial = new JTable(modeloTabelaResidencial);
        tabelaResidencial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaResidencial.setRowHeight(24);
        painel.add(new JScrollPane(tabelaResidencial), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(8, 8));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        form.setBorder(BorderFactory.createTitledBorder("Cadastrar Medidor Residencial (suporte a Prossumidor / Solar)"));

        JTextField txtMac = new JTextField(12);
        JTextField txtEnd = new JTextField(14);
        JTextField txtKwh = new JTextField(5);
        JTextField txtSolar = new JTextField("0", 5);
        JComboBox<BandeiraTarifaria> cbBandeira = new JComboBox<>(BandeiraTarifaria.values());
        JButton btnAdicionar = new JButton("Adicionar");

        form.add(new JLabel("MAC:"));
        form.add(txtMac);
        form.add(new JLabel("Endereço:"));
        form.add(txtEnd);
        form.add(new JLabel("kWh:"));
        form.add(txtKwh);
        form.add(new JLabel("Solar (kWh):"));
        form.add(txtSolar);
        form.add(new JLabel("Bandeira:"));
        form.add(cbBandeira);
        form.add(btnAdicionar);

        btnAdicionar.addActionListener(e -> {
            try {
                String mac = txtMac.getText().trim();
                String end = txtEnd.getText().trim();
                double kwh = Double.parseDouble(txtKwh.getText().trim().replace(",", "."));
                double solar = Double.parseDouble(txtSolar.getText().trim().replace(",", "."));
                BandeiraTarifaria bandeira = (BandeiraTarifaria) cbBandeira.getSelectedItem();

                if (end.isEmpty()) {
                    throw new IllegalArgumentException("Informe o endereço.");
                }
                MedidorResidencial novo = new MedidorResidencial(mac, end, kwh, solar, bandeira);
                novo.calcularValorFatura(); // valida regras
                residenciais.add(novo);

                txtMac.setText("");
                txtEnd.setText("");
                txtKwh.setText("");
                txtSolar.setText("0");
                atualizarTabelaResidencial();
                atualizarRelatorio();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Informe números válidos para consumo e injeção solar.",
                        "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnDisjuntor = new JButton("Alternar Disjuntor Virtual (Ligar/Desligar)");
        btnDisjuntor.addActionListener(e -> {
            int row = tabelaResidencial.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Selecione um medidor residencial na tabela.");
                return;
            }
            MedidorResidencial m = residenciais.get(row);
            if (m.isAtivo()) {
                m.desligarDisjuntorVirtual();
            } else {
                m.religarDisjuntorVirtual();
            }
            atualizarTabelaResidencial();
            atualizarRelatorio();
        });
        acoes.add(btnDisjuntor);

        rodape.add(form, BorderLayout.CENTER);
        rodape.add(acoes, BorderLayout.SOUTH);
        painel.add(rodape, BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarAbaRelatorio() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        areaRelatorio = new JTextArea();
        areaRelatorio.setEditable(false);
        areaRelatorio.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaRelatorio.setMargin(new java.awt.Insets(10, 12, 10, 12));
        painel.add(new JScrollPane(areaRelatorio), BorderLayout.CENTER);

        JButton btnAtualizar = new JButton("Atualizar Relatório Consolidado");
        btnAtualizar.addActionListener(e -> atualizarRelatorio());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barra.add(btnAtualizar);
        painel.add(barra, BorderLayout.SOUTH);

        return painel;
    }

    private void atualizarTabelaIndustrial() {
        modeloTabelaIndustrial.setRowCount(0);
        for (MedidorIndustrial m : service.listarMedidoresIndustriais()) {
            String fatura;
            try {
                fatura = m.getFaturaFormatada();
            } catch (RegraTarifacaoException ex) {
                fatura = "Bloqueada";
            }
            modeloTabelaIndustrial.addRow(new Object[]{
                    m.getMacAddress(),
                    m.getEndereco(),
                    String.format("%.2f", m.getConsumoKwh()),
                    String.format("%.2f", m.getFatorDemandaContratada()),
                    m.getBandeira(),
                    m.getStatusOperacional(),
                    fatura
            });
        }
    }

    private void atualizarTabelaResidencial() {
        modeloTabelaResidencial.setRowCount(0);
        for (MedidorResidencial m : residenciais) {
            String fatura;
            try {
                fatura = m.getFaturaFormatada();
            } catch (RegraTarifacaoException ex) {
                fatura = "Bloqueada (Disjuntor)";
            }
            modeloTabelaResidencial.addRow(new Object[]{
                    m.getMacAddress(),
                    m.getEndereco(),
                    String.format("%.1f", m.getConsumoKwh()),
                    String.format("%.1f", m.getInjecaoSolarKwh()),
                    String.format("%.1f", m.getSaldoLiquidoKwh()),
                    String.format("%.1f", m.getCreditosKwh()),
                    m.isProssumidor() ? "Prossumidor" : "Comum",
                    m.getBandeira(),
                    m.getStatusOperacional(),
                    m.getStatusFatura(),
                    fatura
            });
        }
    }

    private void atualizarSelecionadoIndustrial() {
        int row = tabelaIndustrial.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um medidor industrial na tabela.");
            return;
        }
        String mac = (String) modeloTabelaIndustrial.getValueAt(row, 0);
        String entrada = JOptionPane.showInputDialog(this, "Novo consumo (kWh) para " + mac + ":");
        if (entrada == null) {
            return;
        }
        BandeiraTarifaria novaBandeira = (BandeiraTarifaria) JOptionPane.showInputDialog(
                this,
                "Escolha a nova bandeira tarifária:",
                "Atualizar Bandeira",
                JOptionPane.QUESTION_MESSAGE,
                null,
                BandeiraTarifaria.values(),
                BandeiraTarifaria.VERDE);
        if (novaBandeira == null) {
            return;
        }
        try {
            double novoConsumo = Double.parseDouble(entrada.trim().replace(",", "."));
            if (novoConsumo < 0) {
                throw new RegraTarifacaoException("O consumo não pode ser negativo!");
            }
            service.atualizarConsumoIndustrial(mac, novoConsumo, novaBandeira);
            atualizarTabelaIndustrial();
            atualizarRelatorio();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simularBandeirasSelecionado() {
        int row = tabelaIndustrial.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um medidor industrial na tabela para simular.");
            return;
        }
        String mac = (String) modeloTabelaIndustrial.getValueAt(row, 0);
        Optional<MedidorIndustrial> opt = service.buscarIndustrialPorMac(mac);
        if (opt.isEmpty()) {
            return;
        }
        try {
            MedidorIndustrial m = opt.get();
            Map<BandeiraTarifaria, BigDecimal> comp = service.compararBandeiras(m);
            StringBuilder sb = new StringBuilder();
            sb.append("Simulação de Bandeiras — ").append(m.getMacAddress()).append("\n");
            sb.append("Consumo: ").append(String.format("%.2f kWh", m.getConsumoKwh()))
              .append(" | Fator Demanda: ").append(String.format("%.2f", m.getFatorDemandaContratada())).append("\n\n");
            for (Map.Entry<BandeiraTarifaria, BigDecimal> entry : comp.entrySet()) {
                BandeiraTarifaria b = entry.getKey();
                sb.append(String.format("%-9s (+%2d%%): R$ %10.2f — %s%n",
                        b.name(), b.getPercentualAcrescimo(), entry.getValue(), b.getRecomendacao()));
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Comparativo EcoTarifa", JOptionPane.INFORMATION_MESSAGE);
        } catch (RegraTarifacaoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regra Violada", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void atualizarRelatorio() {
        List<MedidorIndustrial> industriais = service.listarMedidoresIndustriais();
        List<MedidorInteligente> todos = new ArrayList<>();
        todos.addAll(industriais);
        todos.addAll(residenciais);

        List<MedidorInteligente> ativos = service.filtrarAtivos(todos);
        List<MedidorResidencial> prossumidores = service.filtrarProssumidores(residenciais);
        double totalIndustrialKwh = service.somarConsumoIndustrialKwh(industriais);
        double totalCreditosSolar = service.somarCreditosSolaresKwh(residenciais);
        Optional<MedidorIndustrial> maiorIndustrial = service.buscarMaiorConsumidorIndustrial(industriais);
        Map<BandeiraTarifaria, Double> porBandeira = service.agruparConsumoIndustrialPorBandeira(industriais);

        List<CalculadoraTarifaria> faturaveis = new ArrayList<>();
        for (MedidorInteligente m : ativos) {
            if (m instanceof CalculadoraTarifaria calc) {
                faturaveis.add(calc);
            }
        }

        String totalFaturas;
        try {
            BigDecimal soma = service.calcularFaturaTotal(faturaveis);
            totalFaturas = String.format("R$ %.2f", soma);
        } catch (RegraTarifacaoException ex) {
            totalFaturas = "Erro ao calcular: " + ex.getMessage();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================\n");
        sb.append("           RELATÓRIO CONSOLIDADO — SMART GRID & ECOTARIFA           \n");
        sb.append("====================================================================\n\n");
        sb.append(String.format("Total de medidores cadastrados : %d (%d ativos)%n", todos.size(), ativos.size()));
        sb.append(String.format("  • Industriais (H2/JDBC)      : %d%n", industriais.size()));
        sb.append(String.format("  • Residenciais               : %d (%d prossumidores solares)%n%n",
                residenciais.size(), prossumidores.size()));

        sb.append("---- Consumo Industrial (Streams API) ------------------------------\n");
        sb.append(String.format("Consumo industrial total       : %.2f kWh%n", totalIndustrialKwh));
        maiorIndustrial.ifPresent(m -> sb.append(String.format(
                "Maior consumidor industrial    : %s (%.2f kWh — %s)%n",
                m.getMacAddress(), m.getConsumoKwh(), m.getEndereco())));
        sb.append("Consumo industrial por bandeira:\n");
        for (BandeiraTarifaria b : BandeiraTarifaria.values()) {
            double kwh = porBandeira.getOrDefault(b, 0.0);
            sb.append(String.format("  • %-8s (+%2d%%): %8.2f kWh%n", b.name(), b.getPercentualAcrescimo(), kwh));
        }

        sb.append("\n---- Microgeração Solar (Net Metering) -----------------------------\n");
        sb.append(String.format("Créditos solares acumulados    : %.2f kWh%n", totalCreditosSolar));

        sb.append("\n---- Faturamento Polimórfico (CalculadoraTarifaria) ----------------\n");
        sb.append(String.format("Faturamento total (ativos)     : %s%n", totalFaturas));

        areaRelatorio.setText(sb.toString());
        areaRelatorio.setCaretPosition(0);
    }

    /**
     * Ponto de entrada da aplicação gráfica.
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new MainView().setVisible(true));
    }
}
