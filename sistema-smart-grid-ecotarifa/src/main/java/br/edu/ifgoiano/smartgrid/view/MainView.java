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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

/**
 * Camada de apresentação (View) do <b>Sistema Smart Grid &amp; EcoTarifa</b>.
 * <p>
 * Integra as cinco camadas desenvolvidas pelo grupo:
 * <ol>
 *   <li><b>Domínio</b> ({@link MedidorInteligente}, {@link BandeiraTarifaria}): disjuntor virtual,
 *       tensão e curva de carga horária;</li>
 *   <li><b>Polimorfismo e exceções</b> ({@link CalculadoraTarifaria}, {@link MedidorIndustrial},
 *       {@link MedidorResidencial}, {@link RegraTarifacaoException});</li>
 *   <li><b>Banco H2 em memória</b> (criação das tabelas);</li>
 *   <li><b>Persistência JDBC</b> (CRUD transacional de medidores industriais);</li>
 *   <li><b>Streams API</b> ({@link TarifacaoService}): filtros, somas, agrupamentos e comparativos.</li>
 * </ol>
 * <p>
 * A aplicação oferece dois modos de execução:
 * <ul>
 *   <li><b>Gráfico (Swing)</b>, padrão ao abrir o {@code .jar} ou rodar {@link #main(String[])};</li>
 *   <li><b>Console</b>, acionado com o argumento {@code --console} ou automaticamente quando
 *       não há ambiente gráfico disponível.</li>
 * </ul>
 *
 * <pre>
 * java -jar smart-grid-ecotarifa.jar             (interface gráfica)
 * java -jar smart-grid-ecotarifa.jar --console   (menu no terminal)
 * </pre>
 *
 * @author Jeanderson Honorato de Souza
 * @version 1.0
 */
public class MainView {

    /** Expressão regular do endereço MAC no formato {@code AA:BB:CC:DD:EE:FF}. */
    private static final String REGEX_MAC = "^([0-9A-F]{2}:){5}[0-9A-F]{2}$";

    /** Tamanho máximo do endereço, igual à coluna {@code endereco VARCHAR(150)} do H2. */
    private static final int TAMANHO_MAX_ENDERECO = 150;

    /** Rótulo da opção que exibe todas as bandeiras no filtro da aba industrial. */
    private static final String FILTRO_TODAS = "TODAS";

    private static final Color AZUL_ESCURO = new Color(18, 52, 86);
    private static final Color AZUL_CLARO = new Color(200, 220, 240);
    private static final Color VERDE = new Color(22, 128, 61);
    private static final Color VERMELHO = new Color(185, 28, 28);
    private static final Color AMARELO = new Color(161, 98, 7);

    private final TarifacaoService service;
    private final List<MedidorResidencial> residenciais = new ArrayList<>();

    /**
     * MACs dos medidores industriais com disjuntor virtual desligado.
     * O DAO não persiste o status ao listar, então a View guarda esse estado
     * e o reaplica sempre que os medidores são recarregados do banco.
     */
    private final Set<String> industriaisDesligados = new HashSet<>();

    private JFrame janela;
    private DefaultTableModel modeloIndustrial;
    private JTable tabelaIndustrial;
    private JComboBox<String> filtroBandeira;
    private DefaultTableModel modeloResidencial;
    private JTable tabelaResidencial;
    private JTextArea areaRelatorio;
    private JLabel cardMedidores;
    private JLabel cardConsumo;
    private JLabel cardCreditos;
    private JLabel cardFaturamento;
    private JLabel barraStatus;

    public MainView() {
        this.service = new TarifacaoService();
        this.service.inicializarBanco();
        carregarDadosIniciais();
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
                "DD:EE:FF:02:03:03", "Rua 7, Qd. 14 - Vila Nova", 410.0, BandeiraTarifaria.VERMELHA));
    }

    /**
     * Lê os medidores industriais do H2 e reaplica o estado do disjuntor virtual.
     *
     * @return medidores industriais com o status operacional correto
     */
    private List<MedidorIndustrial> carregarIndustriais() {
        List<MedidorIndustrial> lista = service.listarMedidoresIndustriais();
        for (MedidorIndustrial m : lista) {
            if (industriaisDesligados.contains(m.getMacAddress())) {
                m.desligarDisjuntorVirtual();
            }
        }
        return lista;
    }

    /**
     * Valida e normaliza um endereço MAC, garantindo que ele ainda não esteja cadastrado.
     *
     * @param texto MAC digitado pelo usuário
     * @return MAC em letras maiúsculas
     * @throws IllegalArgumentException se o formato for inválido ou o MAC já existir
     */
    private String validarMacNovo(String texto) {
        String mac = texto == null ? "" : texto.trim().toUpperCase(Locale.ROOT).replace('-', ':');
        if (!mac.matches(REGEX_MAC)) {
            throw new IllegalArgumentException("MAC inválido. Use o formato AA:BB:CC:DD:EE:FF.");
        }
        boolean existeIndustrial = service.buscarIndustrialPorMac(mac).isPresent();
        boolean existeResidencial = residenciais.stream().anyMatch(r -> r.getMacAddress().equals(mac));
        if (existeIndustrial || existeResidencial) {
            throw new IllegalArgumentException("Já existe um medidor cadastrado com o MAC " + mac + ".");
        }
        return mac;
    }

    /**
     * Valida o endereço de instalação do medidor.
     *
     * @param texto endereço digitado
     * @return endereço sem espaços nas pontas
     * @throws IllegalArgumentException se estiver vazio ou exceder o limite do banco
     */
    private static String validarEndereco(String texto) {
        String endereco = texto == null ? "" : texto.trim();
        if (endereco.isEmpty()) {
            throw new IllegalArgumentException("Informe o endereço do medidor.");
        }
        if (endereco.length() > TAMANHO_MAX_ENDERECO) {
            throw new IllegalArgumentException("O endereço deve ter no máximo " + TAMANHO_MAX_ENDERECO + " caracteres.");
        }
        return endereco;
    }

    /**
     * Converte texto em número aceitando vírgula ou ponto como separador decimal.
     *
     * @param texto valor digitado
     * @param campo nome do campo, usado na mensagem de erro
     * @return valor numérico finito
     * @throws IllegalArgumentException se o texto não for um número válido
     */
    private static double lerNumero(String texto, String campo) {
        try {
            double valor = Double.parseDouble(texto.trim().replace(",", "."));
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Valor inválido para \"" + campo + "\": informe um número.");
        }
    }

    /**
     * Calcula a fatura de um medidor ou informa o motivo do bloqueio.
     *
     * @param medidor medidor que implementa {@link CalculadoraTarifaria}
     * @return valor formatado em reais ou o motivo do bloqueio
     */
    private static String descreverFatura(CalculadoraTarifaria medidor) {
        try {
            return medidor.getFaturaFormatada();
        } catch (RegraTarifacaoException e) {
            if (medidor instanceof MedidorInteligente m && !m.isAtivo()) {
                return "Bloqueada (disjuntor)";
            }
            return "Bloqueada (tensão)";
        }
    }

    /**
     * Gera o relatório consolidado usando os métodos de Streams API do {@link TarifacaoService}.
     * Medidores que violam alguma regra tarifária são listados à parte, sem interromper o relatório.
     *
     * @return texto do relatório
     */
    public String gerarRelatorio() {
        List<MedidorIndustrial> industriais = carregarIndustriais();
        List<MedidorInteligente> todos = new ArrayList<>(industriais);
        todos.addAll(residenciais);

        List<MedidorInteligente> ativos = service.filtrarAtivos(todos);
        int qtdIndustriais = service.filtrarPorTipo(todos, "INDUSTRIAL").size();
        int qtdResidenciais = service.filtrarPorTipo(todos, "RESIDENCIAL").size();
        List<MedidorResidencial> prossumidores = service.filtrarProssumidores(residenciais);
        double consumoIndustrial = service.somarConsumoIndustrialKwh(industriais);
        double creditosSolares = service.somarCreditosSolaresKwh(residenciais);
        Optional<MedidorIndustrial> maior = service.buscarMaiorConsumidorIndustrial(industriais);
        Map<BandeiraTarifaria, Double> porBandeira = service.agruparConsumoIndustrialPorBandeira(industriais);

        List<CalculadoraTarifaria> faturaveisIndustriais = new ArrayList<>();
        List<CalculadoraTarifaria> faturaveisResidenciais = new ArrayList<>();
        List<String> bloqueados = new ArrayList<>();
        for (MedidorInteligente m : todos) {
            if (!(m instanceof CalculadoraTarifaria calc)) {
                continue;
            }
            try {
                calc.calcularValorFatura();
                if (m instanceof MedidorIndustrial) {
                    faturaveisIndustriais.add(calc);
                } else {
                    faturaveisResidenciais.add(calc);
                }
            } catch (RegraTarifacaoException e) {
                bloqueados.add(m.getMacAddress() + " -> " + e.getMessage());
            }
        }

        StringBuilder sb = new StringBuilder();
        String linha = "=".repeat(72) + "\n";
        sb.append(linha);
        sb.append("              RELATÓRIO CONSOLIDADO - SMART GRID & ECOTARIFA\n");
        sb.append(linha).append('\n');

        sb.append(String.format("Medidores cadastrados ........ %d (%d ativos)%n", todos.size(), ativos.size()));
        sb.append(String.format("  - Industriais (H2/JDBC) .... %d%n", qtdIndustriais));
        sb.append(String.format("  - Residenciais ............. %d (%d prossumidores solares)%n%n",
                qtdResidenciais, prossumidores.size()));

        sb.append("---- Consumo industrial (Streams API) ").append("-".repeat(34)).append('\n');
        sb.append(String.format("Consumo industrial total ..... %,.2f kWh%n", consumoIndustrial));
        maior.ifPresent(m -> sb.append(String.format("Maior consumidor ............. %s (%,.2f kWh - %s)%n",
                m.getMacAddress(), m.getConsumoKwh(), m.getEndereco())));
        sb.append("Consumo por bandeira:\n");
        for (BandeiraTarifaria b : BandeiraTarifaria.values()) {
            sb.append(String.format("  - %-8s (+%2d%%) .......... %,10.2f kWh%n",
                    b.name(), b.getPercentualAcrescimo(), porBandeira.getOrDefault(b, 0.0)));
        }

        sb.append("\n---- Microgeração solar (Net Metering) ").append("-".repeat(33)).append('\n');
        sb.append(String.format("Créditos solares acumulados .. %,.2f kWh%n", creditosSolares));
        for (MedidorResidencial r : prossumidores) {
            sb.append(String.format("  - %s: injetou %,.1f kWh, saldo %,.1f kWh (%s)%n",
                    r.getMacAddress(), r.getInjecaoSolarKwh(), r.getSaldoLiquidoKwh(), r.getStatusFatura()));
        }

        sb.append("\n---- Faturamento polimórfico (CalculadoraTarifaria) ").append("-".repeat(20)).append('\n');
        try {
            BigDecimal totalInd = service.calcularFaturaTotal(faturaveisIndustriais);
            BigDecimal totalRes = service.calcularFaturaTotal(faturaveisResidenciais);
            sb.append(String.format("Industrial ................... R$ %,.2f%n", totalInd));
            sb.append(String.format("Residencial .................. R$ %,.2f%n", totalRes));
            sb.append(String.format("TOTAL ........................ R$ %,.2f%n", totalInd.add(totalRes)));
        } catch (RegraTarifacaoException e) {
            sb.append("Não foi possível calcular o faturamento: ").append(e.getMessage()).append('\n');
        }
        if (!bloqueados.isEmpty()) {
            sb.append("\nMedidores bloqueados (RegraTarifacaoException):\n");
            bloqueados.forEach(b -> sb.append("  ! ").append(b).append('\n'));
        }

        maior.filter(MedidorInteligente::isAtivo).ifPresent(m -> {
            sb.append("\n---- Simulação de bandeiras: ").append(m.getMacAddress()).append(' ')
              .append("-".repeat(24)).append('\n');
            sb.append(formatarComparativo(m));
        });
        return sb.toString();
    }

    /**
     * Monta o texto do comparativo de bandeiras de um medidor industrial.
     *
     * @param medidor medidor base da simulação
     * @return tabela textual com o valor da fatura em cada bandeira
     */
    private String formatarComparativo(MedidorIndustrial medidor) {
        try {
            Map<BandeiraTarifaria, BigDecimal> comparativo = service.compararBandeiras(medidor);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<BandeiraTarifaria, BigDecimal> e : comparativo.entrySet()) {
                BandeiraTarifaria b = e.getKey();
                sb.append(String.format("  %-8s (+%2d%%): R$ %,12.2f  %s%n",
                        b.name(), b.getPercentualAcrescimo(), e.getValue(), b.getRecomendacao()));
            }
            return sb.toString();
        } catch (RegraTarifacaoException e) {
            return "  Simulação indisponível: " + e.getMessage() + "\n";
        }
    }

    private void exibirJanela() {
        janela = new JFrame("Sistema Smart Grid & EcoTarifa - IF Goiano");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setSize(1180, 720);
        janela.setMinimumSize(new Dimension(960, 600));
        janela.setLayout(new BorderLayout());

        janela.add(criarCabecalho(), BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        abas.addTab("Medidores Industriais (H2 / JDBC)", criarAbaIndustrial());
        abas.addTab("Residenciais & Energia Solar", criarAbaResidencial());
        abas.addTab("Relatório (Streams API)", criarAbaRelatorio());
        janela.add(abas, BorderLayout.CENTER);

        barraStatus = new JLabel(" Sistema iniciado. Banco H2 em memória pronto.");
        barraStatus.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        janela.add(barraStatus, BorderLayout.SOUTH);

        atualizarTudo();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }

    /**
     * Cria o cabeçalho com título e cartões de resumo.
     *
     * @return painel do cabeçalho
     */
    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout(16, 8));
        cabecalho.setBackground(AZUL_ESCURO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titulo = new JLabel("Sistema Smart Grid & EcoTarifa");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        JLabel subtitulo = new JLabel("Medidores inteligentes, Net Metering, bandeiras tarifárias e banco H2");
        subtitulo.setForeground(AZUL_CLARO);
        subtitulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);
        cabecalho.add(textos, BorderLayout.WEST);

        JPanel cards = new JPanel(new GridLayout(1, 4, 10, 0));
        cards.setOpaque(false);
        cardMedidores = criarCard(cards, "Medidores ativos");
        cardConsumo = criarCard(cards, "Consumo industrial");
        cardCreditos = criarCard(cards, "Créditos solares");
        cardFaturamento = criarCard(cards, "Faturamento total");
        cabecalho.add(cards, BorderLayout.EAST);
        return cabecalho;
    }

    /**
     * Cria um cartão de resumo e o adiciona ao painel informado.
     *
     * @param destino painel que recebe o cartão
     * @param rotulo  título do cartão
     * @return rótulo onde o valor do cartão será exibido
     */
    private static JLabel criarCard(JPanel destino, String rotulo) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(new Color(30, 70, 110));
        card.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JLabel titulo = new JLabel(rotulo);
        titulo.setForeground(AZUL_CLARO);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        JLabel valor = new JLabel("-");
        valor.setForeground(Color.WHITE);
        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        card.add(titulo);
        card.add(valor);
        destino.add(card);
        return valor;
    }

    /**
     * Cria a aba de medidores industriais, persistidos no H2.
     *
     * @return painel da aba
     */
    private JPanel criarAbaIndustrial() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloIndustrial = criarModelo(
                new String[]{"MAC Address", "Endereço", "Consumo (kWh)", "Fator demanda", "Bandeira", "Status", "Fatura"},
                new Class<?>[]{String.class, String.class, Double.class, Double.class, String.class, String.class, String.class});
        tabelaIndustrial = criarTabela(modeloIndustrial);
        painel.add(new JScrollPane(tabelaIndustrial), BorderLayout.CENTER);

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtroBandeira = new JComboBox<>();
        filtroBandeira.addItem(FILTRO_TODAS);
        for (BandeiraTarifaria b : BandeiraTarifaria.values()) {
            filtroBandeira.addItem(b.name());
        }
        filtroBandeira.addActionListener(e -> atualizarTabelaIndustrial());
        topo.add(new JLabel("Filtrar por bandeira:"));
        topo.add(filtroBandeira);
        painel.add(topo, BorderLayout.NORTH);

        JTextField txtMac = new JTextField(13);
        JTextField txtEndereco = new JTextField(18);
        JTextField txtKwh = new JTextField(6);
        JTextField txtFator = new JTextField("1.20", 4);
        JComboBox<BandeiraTarifaria> cbBandeira = new JComboBox<>(BandeiraTarifaria.values());
        JButton btnSalvar = new JButton("Salvar no H2");

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        form.setBorder(BorderFactory.createTitledBorder("Cadastrar medidor industrial"));
        form.add(new JLabel("MAC:"));
        form.add(txtMac);
        form.add(new JLabel("Endereço:"));
        form.add(txtEndereco);
        form.add(new JLabel("kWh:"));
        form.add(txtKwh);
        form.add(new JLabel("Fator:"));
        form.add(txtFator);
        form.add(new JLabel("Bandeira:"));
        form.add(cbBandeira);
        form.add(btnSalvar);

        btnSalvar.addActionListener(e -> executarAcao(() -> {
            String mac = validarMacNovo(txtMac.getText());
            String endereco = validarEndereco(txtEndereco.getText());
            double kwh = lerNumero(txtKwh.getText(), "kWh");
            double fator = lerNumero(txtFator.getText(), "Fator");
            BandeiraTarifaria bandeira = (BandeiraTarifaria) cbBandeira.getSelectedItem();
            MedidorIndustrial novo = new MedidorIndustrial(mac, endereco, kwh, fator, bandeira);
            novo.calcularValorFatura(); // valida as regras antes de gravar no banco
            service.salvarMedidorIndustrial(novo);
            txtMac.setText("");
            txtEndereco.setText("");
            txtKwh.setText("");
            txtFator.setText("1.20");
            return "Medidor industrial " + mac + " salvo no banco H2.";
        }));

        JButton btnAtualizar = new JButton("Atualizar consumo/bandeira");
        btnAtualizar.addActionListener(e -> atualizarIndustrialSelecionado());
        JButton btnSimular = new JButton("Simular bandeiras");
        btnSimular.addActionListener(e -> simularBandeirasSelecionado());
        JButton btnDisjuntor = new JButton("Ligar/Desligar disjuntor");
        btnDisjuntor.addActionListener(e -> alternarDisjuntorIndustrial());

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        acoes.add(btnAtualizar);
        acoes.add(btnSimular);
        acoes.add(btnDisjuntor);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(form, BorderLayout.CENTER);
        rodape.add(acoes, BorderLayout.SOUTH);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    /**
     * Cria a aba de medidores residenciais com suporte a prossumidores solares.
     *
     * @return painel da aba
     */
    private JPanel criarAbaResidencial() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloResidencial = criarModelo(
                new String[]{"MAC Address", "Endereço", "Consumo", "Solar", "Saldo", "Créditos", "Perfil",
                    "Bandeira", "Tensão (V)", "Status", "Situação", "Fatura"},
                new Class<?>[]{String.class, String.class, Double.class, Double.class, Double.class, Double.class,
                    String.class, String.class, Double.class, String.class, String.class, String.class});
        tabelaResidencial = criarTabela(modeloResidencial);
        painel.add(new JScrollPane(tabelaResidencial), BorderLayout.CENTER);

        JTextField txtMac = new JTextField(13);
        JTextField txtEndereco = new JTextField(16);
        JTextField txtKwh = new JTextField(5);
        JTextField txtSolar = new JTextField("0", 5);
        JComboBox<BandeiraTarifaria> cbBandeira = new JComboBox<>(BandeiraTarifaria.values());
        JButton btnAdicionar = new JButton("Adicionar");

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        form.setBorder(BorderFactory.createTitledBorder("Cadastrar medidor residencial (consumo e injeção solar em kWh)"));
        form.add(new JLabel("MAC:"));
        form.add(txtMac);
        form.add(new JLabel("Endereço:"));
        form.add(txtEndereco);
        form.add(new JLabel("Consumo:"));
        form.add(txtKwh);
        form.add(new JLabel("Solar:"));
        form.add(txtSolar);
        form.add(new JLabel("Bandeira:"));
        form.add(cbBandeira);
        form.add(btnAdicionar);

        btnAdicionar.addActionListener(e -> executarAcao(() -> {
            String mac = validarMacNovo(txtMac.getText());
            String endereco = validarEndereco(txtEndereco.getText());
            double kwh = lerNumero(txtKwh.getText(), "Consumo");
            double solar = lerNumero(txtSolar.getText(), "Solar");
            BandeiraTarifaria bandeira = (BandeiraTarifaria) cbBandeira.getSelectedItem();
            MedidorResidencial novo = new MedidorResidencial(mac, endereco, kwh, solar, bandeira);
            novo.calcularValorFatura(); // valida as regras antes de adicionar
            residenciais.add(novo);
            txtMac.setText("");
            txtEndereco.setText("");
            txtKwh.setText("");
            txtSolar.setText("0");
            return "Medidor residencial " + mac + " adicionado"
                    + (novo.isProssumidor() ? " (prossumidor solar)." : ".");
        }));

        JButton btnDisjuntor = new JButton("Ligar/Desligar disjuntor");
        btnDisjuntor.addActionListener(e -> alternarDisjuntorResidencial());
        JButton btnTensao = new JButton("Alterar tensão");
        btnTensao.addActionListener(e -> alterarTensaoResidencial());
        JButton btnCurva = new JButton("Curva de carga");
        btnCurva.addActionListener(e -> mostrarCurvaDeCarga());
        JButton btnRemover = new JButton("Remover");
        btnRemover.addActionListener(e -> removerResidencial());

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        acoes.add(btnDisjuntor);
        acoes.add(btnTensao);
        acoes.add(btnCurva);
        acoes.add(btnRemover);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(form, BorderLayout.CENTER);
        rodape.add(acoes, BorderLayout.SOUTH);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    /**
     * Cria a aba do relatório consolidado.
     *
     * @return painel da aba
     */
    private JPanel criarAbaRelatorio() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        areaRelatorio = new JTextArea();
        areaRelatorio.setEditable(false);
        areaRelatorio.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaRelatorio.setMargin(new Insets(10, 12, 10, 12));
        painel.add(new JScrollPane(areaRelatorio), BorderLayout.CENTER);

        JButton btnAtualizar = new JButton("Atualizar relatório");
        btnAtualizar.addActionListener(e -> atualizarTudo());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barra.add(btnAtualizar);
        painel.add(barra, BorderLayout.SOUTH);
        return painel;
    }

    /**
     * Cria um modelo de tabela somente leitura com tipos de coluna definidos
     * (permite ordenar números corretamente ao clicar no cabeçalho).
     *
     * @param colunas nomes das colunas
     * @param tipos   classe de cada coluna
     * @return modelo de tabela
     */
    private static DefaultTableModel criarModelo(String[] colunas, Class<?>[] tipos) {
        return new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int coluna) {
                return tipos[coluna];
            }
        };
    }

    /**
     * Cria uma tabela com seleção única, ordenação e cores para status e faturas.
     *
     * @param modelo modelo de dados da tabela
     * @return tabela configurada
     */
    private static JTable criarTabela(DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(24);
        tabela.setAutoCreateRowSorter(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setDefaultRenderer(String.class, new RenderizadorStatus());
        return tabela;
    }

    /** Renderizador que destaca em cores os status operacionais, bandeiras e faturas bloqueadas. */
    private static final class RenderizadorStatus extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionado,
                                                       boolean foco, int linha, int coluna) {
            Component c = super.getTableCellRendererComponent(tabela, valor, selecionado, foco, linha, coluna);
            setHorizontalAlignment(SwingConstants.LEFT);
            if (selecionado) {
                return c;
            }
            String texto = valor == null ? "" : valor.toString();
            Color cor = Color.BLACK;
            if (texto.equals(MedidorInteligente.STATUS_ATIVO) || texto.equals("VERDE") || texto.equals("CRÉDITO")) {
                cor = VERDE;
            } else if (texto.equals(MedidorInteligente.STATUS_DESLIGADO) || texto.startsWith("Bloqueada")
                    || texto.equals("VERMELHA")) {
                cor = VERMELHO;
            } else if (texto.equals("AMARELA")) {
                cor = AMARELO;
            }
            c.setForeground(cor);
            return c;
        }
    }
    @FunctionalInterface
    private interface Acao {
        String executar() throws Exception;
    }

    /**
     * Executa uma ação tratando erros de forma amigável e atualizando a tela.
     *
     * @param acao ação a ser executada
     */
    private void executarAcao(Acao acao) {
        try {
            String mensagem = acao.executar();
            atualizarTudo();
            if (mensagem != null) {
                barraStatus.setText(" " + mensagem);
            }
        } catch (RegraTarifacaoException e) {
            mostrarErro("Regra de tarifação violada", e.getMessage());
        } catch (IllegalArgumentException e) {
            mostrarErro("Dados inválidos", e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro inesperado", e.getMessage());
        }
    }

    /**
     * Exibe uma mensagem de erro em diálogo e na barra de status.
     *
     * @param titulo   título do diálogo
     * @param mensagem mensagem a exibir
     */
    private void mostrarErro(String titulo, String mensagem) {
        barraStatus.setText(" " + titulo + ": " + mensagem);
        JOptionPane.showMessageDialog(janela, mensagem, titulo, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Retorna o MAC da linha selecionada em uma tabela, considerando a ordenação.
     *
     * @param tabela tabela consultada
     * @param tipo   descrição do tipo de medidor, usada no aviso
     * @return MAC selecionado, ou {@code null} se nenhuma linha estiver selecionada
     */
    private String macSelecionado(JTable tabela, String tipo) {
        int linhaVisual = tabela.getSelectedRow();
        if (linhaVisual < 0) {
            JOptionPane.showMessageDialog(janela, "Selecione um medidor " + tipo + " na tabela.",
                    "Nenhum medidor selecionado", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int linha = tabela.convertRowIndexToModel(linhaVisual);
        return (String) tabela.getModel().getValueAt(linha, 0);
    }

    /**
     * Localiza um medidor residencial pelo MAC.
     *
     * @param mac endereço MAC
     * @return medidor encontrado, ou vazio
     */
    private Optional<MedidorResidencial> buscarResidencial(String mac) {
        return residenciais.stream().filter(r -> r.getMacAddress().equals(mac)).findFirst();
    }

    private void atualizarIndustrialSelecionado() {
        String mac = macSelecionado(tabelaIndustrial, "industrial");
        if (mac == null) {
            return;
        }
        Optional<MedidorIndustrial> atual = service.buscarIndustrialPorMac(mac);
        if (atual.isEmpty()) {
            mostrarErro("Medidor não encontrado", "O medidor " + mac + " não está mais no banco.");
            return;
        }
        String entrada = JOptionPane.showInputDialog(janela, "Novo consumo (kWh) para " + mac + ":",
                String.valueOf(atual.get().getConsumoKwh()));
        if (entrada == null) {
            return;
        }
        BandeiraTarifaria novaBandeira = (BandeiraTarifaria) JOptionPane.showInputDialog(janela,
                "Nova bandeira tarifária:", "Atualizar bandeira", JOptionPane.QUESTION_MESSAGE, null,
                BandeiraTarifaria.values(), atual.get().getBandeira());
        if (novaBandeira == null) {
            return;
        }
        executarAcao(() -> {
            double consumo = lerNumero(entrada, "Consumo");
            atual.get().setConsumoKwh(consumo); // valida (lança RegraTarifacaoException se negativo)
            service.atualizarConsumoIndustrial(mac, consumo, novaBandeira);
            return "Medidor " + mac + " atualizado: " + String.format("%,.2f kWh", consumo) + ", bandeira " + novaBandeira + ".";
        });
    }

    private void simularBandeirasSelecionado() {
        String mac = macSelecionado(tabelaIndustrial, "industrial");
        if (mac == null) {
            return;
        }
        service.buscarIndustrialPorMac(mac).ifPresent(m -> {
            String texto = String.format("Medidor %s%nConsumo: %,.2f kWh | Fator de demanda: %.2f%n%n",
                    m.getMacAddress(), m.getConsumoKwh(), m.getFatorDemandaContratada()) + formatarComparativo(m);
            mostrarTextoMonoespacado("Comparativo EcoTarifa", texto);
        });
    }

    private void alternarDisjuntorIndustrial() {
        String mac = macSelecionado(tabelaIndustrial, "industrial");
        if (mac == null) {
            return;
        }
        executarAcao(() -> {
            if (industriaisDesligados.remove(mac)) {
                return "Disjuntor virtual do medidor " + mac + " religado.";
            }
            industriaisDesligados.add(mac);
            return "Disjuntor virtual do medidor " + mac + " desligado (fatura bloqueada).";
        });
    }

    private void alternarDisjuntorResidencial() {
        String mac = macSelecionado(tabelaResidencial, "residencial");
        if (mac == null) {
            return;
        }
        buscarResidencial(mac).ifPresent(m -> executarAcao(() -> {
            if (m.isAtivo()) {
                m.desligarDisjuntorVirtual();
                return "Disjuntor virtual do medidor " + mac + " desligado (fatura bloqueada).";
            }
            m.religarDisjuntorVirtual();
            return "Disjuntor virtual do medidor " + mac + " religado.";
        }));
    }

    private void alterarTensaoResidencial() {
        String mac = macSelecionado(tabelaResidencial, "residencial");
        if (mac == null) {
            return;
        }
        Optional<MedidorResidencial> medidor = buscarResidencial(mac);
        if (medidor.isEmpty()) {
            return;
        }
        String entrada = JOptionPane.showInputDialog(janela, String.format(
                "Nova tensão (V) para %s.%nFaixa segura: %.0f V a %.0f V (nominal %.0f V).",
                mac, MedidorResidencial.TENSAO_MINIMA, MedidorResidencial.TENSAO_MAXIMA,
                MedidorInteligente.TENSAO_NOMINAL), String.valueOf(medidor.get().getTensaoAtual()));
        if (entrada == null) {
            return;
        }
        executarAcao(() -> {
            MedidorResidencial m = medidor.get();
            m.setTensaoAtual(lerNumero(entrada, "Tensão"));
            try {
                m.calcularValorFatura();
                return String.format("Tensão do medidor %s ajustada para %.1f V (dentro da faixa segura).",
                        mac, m.getTensaoAtual());
            } catch (RegraTarifacaoException e) {
                return "Proteção acionada em " + mac + ": " + e.getMessage();
            }
        });
    }

    /**
     * Exibe a curva de carga horária do medidor residencial selecionado. Se ainda não houver
     * leituras, gera um perfil típico de 24 horas proporcional ao consumo mensal.
     */
    private void mostrarCurvaDeCarga() {
        String mac = macSelecionado(tabelaResidencial, "residencial");
        if (mac == null) {
            return;
        }
        buscarResidencial(mac).ifPresent(m -> {
            if (m.getCurvaCargaHoraria().isEmpty()) {
                gerarCurvaSimulada(m);
            }
            mostrarTextoMonoespacado("Curva de carga - " + mac, formatarCurva(m));
        });
    }

    /**
     * Registra no medidor um perfil diário típico (pico no início da noite).
     *
     * @param m medidor que receberá as leituras horárias
     */
    private static void gerarCurvaSimulada(MedidorInteligente m) {
        double[] pesos = {0.6, 0.5, 0.5, 0.5, 0.5, 0.7, 1.0, 1.3, 1.1, 0.9, 0.9, 1.0,
            1.2, 1.0, 0.9, 0.9, 1.0, 1.4, 2.0, 2.3, 2.1, 1.7, 1.2, 0.8};
        double consumoDiario = (m instanceof MedidorResidencial r ? r.getConsumoKwh() : 0.0) / 30.0;
        double somaPesos = 0.0;
        for (double p : pesos) {
            somaPesos += p;
        }
        for (int hora = 0; hora < pesos.length; hora++) {
            m.registrarConsumoHorario(hora, consumoDiario * pesos[hora] / somaPesos);
        }
    }

    /**
     * Formata a curva de carga como um gráfico de barras em texto.
     *
     * @param m medidor com leituras horárias
     * @return texto da curva de carga
     */
    private static String formatarCurva(MedidorInteligente m) {
        Map<Integer, Double> curva = m.getCurvaCargaHoraria();
        double maximo = curva.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        Optional<Integer> pico = m.getHorarioDePico();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Consumo diário registrado: %.2f kWh%n", m.getConsumoTotalCurva()));
        pico.ifPresent(h -> sb.append(String.format("Horário de pico: %02dh%n", h)));
        sb.append('\n');
        for (Map.Entry<Integer, Double> e : curva.entrySet()) {
            int barras = maximo > 0 ? (int) Math.round(e.getValue() / maximo * 30) : 0;
            sb.append(String.format("%02dh | %-30s %.3f kWh%s%n", e.getKey(), "#".repeat(barras), e.getValue(),
                    pico.isPresent() && pico.get().equals(e.getKey()) ? "  <- pico" : ""));
        }
        return sb.toString();
    }

    /** Remove o medidor residencial selecionado após confirmação. */
    private void removerResidencial() {
        String mac = macSelecionado(tabelaResidencial, "residencial");
        if (mac == null) {
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(janela, "Remover o medidor " + mac + "?",
                "Confirmar remoção", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            executarAcao(() -> {
                residenciais.removeIf(r -> r.getMacAddress().equals(mac));
                return "Medidor residencial " + mac + " removido.";
            });
        }
    }

    /**
     * Exibe um texto longo em diálogo com fonte monoespaçada.
     *
     * @param titulo título do diálogo
     * @param texto  conteúdo a exibir
     */
    private void mostrarTextoMonoespacado(String titulo, String texto) {
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane rolagem = new JScrollPane(area);
        rolagem.setPreferredSize(new Dimension(760, Math.min(520, 60 + texto.split("\n").length * 20)));
        JOptionPane.showMessageDialog(janela, rolagem, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /** Recarrega tabelas, cartões de resumo e relatório. */
    private void atualizarTudo() {
        atualizarTabelaIndustrial();
        atualizarTabelaResidencial();
        atualizarResumo();
        areaRelatorio.setText(gerarRelatorio());
        areaRelatorio.setCaretPosition(0);
    }

    /** Preenche a tabela industrial aplicando o filtro de bandeira (Streams API). */
    private void atualizarTabelaIndustrial() {
        if (modeloIndustrial == null) {
            return;
        }
        List<MedidorIndustrial> lista = carregarIndustriais();
        Object filtro = filtroBandeira.getSelectedItem();
        if (filtro != null && !FILTRO_TODAS.equals(filtro)) {
            lista = service.filtrarIndustriaisPorBandeira(lista, BandeiraTarifaria.deNome(filtro.toString()));
        }
        modeloIndustrial.setRowCount(0);
        for (MedidorIndustrial m : lista) {
            modeloIndustrial.addRow(new Object[]{
                m.getMacAddress(), m.getEndereco(), m.getConsumoKwh(), m.getFatorDemandaContratada(),
                m.getBandeira().name(), m.getStatusOperacional(), descreverFatura(m)
            });
        }
    }

    /** Preenche a tabela de medidores residenciais. */
    private void atualizarTabelaResidencial() {
        modeloResidencial.setRowCount(0);
        for (MedidorResidencial m : residenciais) {
            modeloResidencial.addRow(new Object[]{
                m.getMacAddress(), m.getEndereco(), m.getConsumoKwh(), m.getInjecaoSolarKwh(),
                m.getSaldoLiquidoKwh(), m.getCreditosKwh(), m.isProssumidor() ? "Prossumidor" : "Consumidor",
                m.getBandeira().name(), m.getTensaoAtual(), m.getStatusOperacional(), m.getStatusFatura(),
                descreverFatura(m)
            });
        }
    }

    /** Atualiza os cartões de resumo do cabeçalho. */
    private void atualizarResumo() {
        List<MedidorIndustrial> industriais = carregarIndustriais();
        List<MedidorInteligente> todos = new ArrayList<>(industriais);
        todos.addAll(residenciais);

        cardMedidores.setText(service.filtrarAtivos(todos).size() + " / " + todos.size());
        cardConsumo.setText(String.format("%,.0f kWh", service.somarConsumoIndustrialKwh(industriais)));
        cardCreditos.setText(String.format("%,.0f kWh", service.somarCreditosSolaresKwh(residenciais)));

        List<CalculadoraTarifaria> validos = new ArrayList<>();
        for (MedidorInteligente m : todos) {
            if (m instanceof CalculadoraTarifaria calc) {
                try {
                    calc.calcularValorFatura();
                    validos.add(calc);
                } catch (RegraTarifacaoException e) {
                    // medidor bloqueado não entra no faturamento
                }
            }
        }
        try {
            cardFaturamento.setText(String.format("R$ %,.2f", service.calcularFaturaTotal(validos)));
        } catch (RegraTarifacaoException e) {
            cardFaturamento.setText("Indisponível");
        }
    }

    private void executarConsole() {
        Scanner entrada = new Scanner(System.in);
        System.out.println("=== Sistema Smart Grid & EcoTarifa (modo console) ===");
        while (true) {
            System.out.println();
            System.out.println("1 - Listar medidores industriais (H2)");
            System.out.println("2 - Listar medidores residenciais");
            System.out.println("3 - Relatório consolidado (Streams API)");
            System.out.println("4 - Simular bandeiras de um medidor industrial");
            System.out.println("5 - Cadastrar medidor industrial");
            System.out.println("0 - Sair");
            String opcao = perguntar(entrada, "Opção: ");
            if (opcao == null || opcao.equals("0")) {
                System.out.println("Encerrando o sistema.");
                return;
            }
            try {
                switch (opcao) {
                    case "1" -> carregarIndustriais().forEach(m ->
                            System.out.printf("%s | fatura: %s%n", m, descreverFatura(m)));
                    case "2" -> residenciais.forEach(m ->
                            System.out.printf("%s | %s | fatura: %s%n", m, m.getStatusFatura(), descreverFatura(m)));
                    case "3" -> System.out.println(gerarRelatorio());
                    case "4" -> {
                        String mac = perguntar(entrada, "MAC do medidor industrial: ");
                        Optional<MedidorIndustrial> m = mac == null ? Optional.empty()
                                : service.buscarIndustrialPorMac(mac.trim().toUpperCase(Locale.ROOT));
                        System.out.print(m.map(this::formatarComparativo).orElse("Medidor não encontrado.\n"));
                    }
                    case "5" -> cadastrarIndustrialConsole(entrada);
                    default -> System.out.println("Opção inválida.");
                }
            } catch (RegraTarifacaoException | IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    /**
     * Cadastra um medidor industrial pelo terminal.
     *
     * @param entrada leitor do terminal
     * @throws RegraTarifacaoException se os dados violarem uma regra tarifária
     */
    private void cadastrarIndustrialConsole(Scanner entrada) throws RegraTarifacaoException {
        String mac = validarMacNovo(perguntar(entrada, "MAC (AA:BB:CC:DD:EE:FF): "));
        String endereco = validarEndereco(perguntar(entrada, "Endereço: "));
        double kwh = lerNumero(perguntar(entrada, "Consumo (kWh): "), "Consumo");
        double fator = lerNumero(perguntar(entrada, "Fator de demanda: "), "Fator");
        String nomeBandeira = perguntar(entrada, "Bandeira (VERDE/AMARELA/VERMELHA): ");
        MedidorIndustrial novo = new MedidorIndustrial(mac, endereco, kwh, fator, BandeiraTarifaria.deNome(nomeBandeira));
        novo.calcularValorFatura();
        service.salvarMedidorIndustrial(novo);
        System.out.println("Medidor salvo no H2. Fatura: " + novo.getFaturaFormatada());
    }

    /**
     * Exibe uma pergunta e lê a resposta do terminal.
     *
     * @param entrada  leitor do terminal
     * @param pergunta texto exibido
     * @return resposta digitada, ou {@code null} se a entrada terminou
     */
    private static String perguntar(Scanner entrada, String pergunta) {
        System.out.print(pergunta);
        return entrada.hasNextLine() ? entrada.nextLine().trim() : null;
    }

    // ======================================================================
    // Ponto de entrada
    // ======================================================================

    /**
     * Ponto de entrada do sistema. Abre a interface gráfica ou, com o argumento
     * {@code --console} (ou sem ambiente gráfico), o menu no terminal.
     *
     * @param args argumentos de linha de comando; aceita {@code --console}
     */
    public static void main(String[] args) {
        boolean console = GraphicsEnvironment.isHeadless();
        for (String arg : args) {
            if ("--console".equalsIgnoreCase(arg)) {
                console = true;
            }
        }

        MainView app;
        try {
            app = new MainView();
        } catch (RuntimeException e) {
            System.err.println("Falha ao inicializar o banco de dados H2: " + e.getMessage());
            if (!console) {
                JOptionPane.showMessageDialog(null, "Falha ao inicializar o banco de dados H2:\n" + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        if (console) {
            app.executarConsole();
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(app::exibirJanela);
    }
}
