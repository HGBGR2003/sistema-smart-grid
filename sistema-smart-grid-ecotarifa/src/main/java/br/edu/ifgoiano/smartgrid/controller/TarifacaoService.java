package br.edu.ifgoiano.smartgrid.controller;

import br.edu.ifgoiano.smartgrid.dao.ConsumoDAOJBC;
import br.edu.ifgoiano.smartgrid.exception.RegraTarifacaoException;
import br.edu.ifgoiano.smartgrid.model.BandeiraTarifaria;
import br.edu.ifgoiano.smartgrid.model.CalculadoraTarifaria;
import br.edu.ifgoiano.smartgrid.model.MedidorIndustrial;
import br.edu.ifgoiano.smartgrid.model.MedidorInteligente;
import br.edu.ifgoiano.smartgrid.model.MedidorResidencial;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Camada de serviço e controle responsável pelas regras de negócio, análises
 * com <b>Java Streams API</b> e orquestração entre os modelos tarifários e a
 * persistência em {@link ConsumoDAOJBC}.
 * <p>
 * Permite filtrar medidores por status operacional, tipo ou bandeira tarifária,
 * somar consumos e créditos solares, comparar simulações entre bandeiras e
 * calcular o faturamento consolidado de forma polimórfica via
 * {@link CalculadoraTarifaria}.
 *
 * <pre>
 * TarifacaoService service = new TarifacaoService();
 * service.inicializarBanco();
 * List&lt;MedidorIndustrial&gt; medidores = service.listarMedidoresIndustriais();
 * double totalKwh = service.somarConsumoIndustrialKwh(medidores);
 * </pre>
 *
 * @author Guilherme Meira de Melo
 * @version 1.0
 */
public class TarifacaoService {

    private final ConsumoDAOJBC dao;

    /**
     * Cria o serviço utilizando uma instância padrão de {@link ConsumoDAOJBC}.
     */
    public TarifacaoService() {
        this(new ConsumoDAOJBC());
    }

    /**
     * Cria o serviço recebendo uma instância de {@link ConsumoDAOJBC}.
     *
     * @param dao objeto de acesso a dados utilizado para persistência
     */
    public TarifacaoService(ConsumoDAOJBC dao) {
        this.dao = dao;
    }

    // ------------------------------------------------------------------
    // Orquestração com o DAO (JDBC / H2)
    // ------------------------------------------------------------------

    /** Inicializa as tabelas do banco de dados H2. */
    public void inicializarBanco() {
        dao.criarTabelas();
    }

    /**
     * Persiste um medidor industrial no banco de dados.
     *
     * @param medidor medidor industrial a cadastrar
     */
    public void salvarMedidorIndustrial(MedidorIndustrial medidor) {
        dao.salvar(medidor);
    }

    /**
     * Lista todos os medidores industriais cadastrados no banco de dados.
     *
     * @return lista de {@link MedidorIndustrial}
     */
    public List<MedidorIndustrial> listarMedidoresIndustriais() {
        return dao.buscarTodos();
    }

    /**
     * Busca um medidor industrial pelo endereço MAC.
     *
     * @param macAddress endereço MAC do medidor
     * @return {@link Optional} contendo o medidor encontrado, ou vazio
     */
    public Optional<MedidorIndustrial> buscarIndustrialPorMac(String macAddress) {
        return Optional.ofNullable(dao.buscarPorMac(macAddress));
    }

    /**
     * Atualiza o consumo e a bandeira tarifária de um medidor industrial no banco.
     *
     * @param macAddress   endereço MAC do medidor
     * @param novoConsumo  novo consumo mensal em kWh
     * @param novaBandeira nova bandeira tarifária vigente
     */
    public void atualizarConsumoIndustrial(String macAddress, double novoConsumo, BandeiraTarifaria novaBandeira) {
        dao.atualizarConsumo(macAddress, novoConsumo, novaBandeira);
    }

    // ------------------------------------------------------------------
    // Filtros, agregações e cálculos com Streams API
    // ------------------------------------------------------------------

    /**
     * Filtra apenas os medidores que estão com o disjuntor virtual ativo.
     *
     * @param medidores lista de medidores a filtrar
     * @param <T>       subtipo de {@link MedidorInteligente}
     * @return lista contendo somente medidores ativos
     */
    public <T extends MedidorInteligente> List<T> filtrarAtivos(List<T> medidores) {
        return medidores.stream()
                .filter(MedidorInteligente::isAtivo)
                .collect(Collectors.toList());
    }

    /**
     * Filtra medidores pelo tipo de consumidor (ex.: {@code "RESIDENCIAL"} ou {@code "INDUSTRIAL"}).
     *
     * @param medidores      lista polimórfica de medidores
     * @param tipoConsumidor tipo desejado (ignora maiúsculas/minúsculas)
     * @return lista de medidores do tipo informado
     */
    public List<MedidorInteligente> filtrarPorTipo(List<MedidorInteligente> medidores, String tipoConsumidor) {
        return medidores.stream()
                .filter(m -> m.getTipoConsumidor().equalsIgnoreCase(tipoConsumidor))
                .collect(Collectors.toList());
    }

    /**
     * Filtra medidores industriais por bandeira tarifária.
     *
     * @param medidores lista de medidores industriais
     * @param bandeira  bandeira desejada
     * @return lista de medidores na bandeira informada
     */
    public List<MedidorIndustrial> filtrarIndustriaisPorBandeira(List<MedidorIndustrial> medidores,
                                                               BandeiraTarifaria bandeira) {
        return medidores.stream()
                .filter(m -> m.getBandeira() == bandeira)
                .collect(Collectors.toList());
    }

    /**
     * Filtra medidores residenciais com perfil de prossumidor (que injetam energia solar).
     *
     * @param medidores lista de medidores residenciais
     * @return lista contendo somente prossumidores
     */
    public List<MedidorResidencial> filtrarProssumidores(List<MedidorResidencial> medidores) {
        return medidores.stream()
                .filter(MedidorResidencial::isProssumidor)
                .collect(Collectors.toList());
    }

    /**
     * Soma o consumo total em kWh de uma lista de medidores industriais.
     *
     * @param medidores lista de medidores industriais
     * @return soma total do consumo em kWh
     */
    public double somarConsumoIndustrialKwh(List<MedidorIndustrial> medidores) {
        return medidores.stream()
                .mapToDouble(MedidorIndustrial::getConsumoKwh)
                .sum();
    }

    /**
     * Soma o total de créditos energéticos gerados (em kWh) por medidores residenciais.
     *
     * @param medidores lista de medidores residenciais
     * @return total de créditos solares acumulados em kWh
     */
    public double somarCreditosSolaresKwh(List<MedidorResidencial> medidores) {
        return medidores.stream()
                .mapToDouble(MedidorResidencial::getCreditosKwh)
                .sum();
    }

    /**
     * Agrupa e soma o consumo industrial (em kWh) por {@link BandeiraTarifaria}.
     *
     * @param medidores lista de medidores industriais
     * @return mapa associando cada bandeira ao total de kWh consumidos
     */
    public Map<BandeiraTarifaria, Double> agruparConsumoIndustrialPorBandeira(List<MedidorIndustrial> medidores) {
        return medidores.stream()
                .filter(m -> m.getBandeira() != null)
                .collect(Collectors.groupingBy(
                        MedidorIndustrial::getBandeira,
                        Collectors.summingDouble(MedidorIndustrial::getConsumoKwh)));
    }

    /**
     * Identifica o medidor industrial com maior consumo em kWh.
     *
     * @param medidores lista de medidores industriais
     * @return {@link Optional} com o maior consumidor, ou vazio se a lista estiver vazia
     */
    public Optional<MedidorIndustrial> buscarMaiorConsumidorIndustrial(List<MedidorIndustrial> medidores) {
        return medidores.stream()
                .max(Comparator.comparingDouble(MedidorIndustrial::getConsumoKwh));
    }

    /**
     * Calcula o valor total das faturas de uma lista polimórfica de
     * {@link CalculadoraTarifaria}, arredondado para 2 casas decimais.
     *
     * @param calculadoras lista de medidores que implementam {@link CalculadoraTarifaria}
     * @return soma das faturas em reais ({@link BigDecimal})
     * @throws RegraTarifacaoException se algum medidor violar uma regra de tarifação
     */
    public BigDecimal calcularFaturaTotal(List<? extends CalculadoraTarifaria> calculadoras)
            throws RegraTarifacaoException {
        BigDecimal total = BigDecimal.ZERO;
        for (CalculadoraTarifaria calc : calculadoras) {
            total = total.add(BigDecimal.valueOf(calc.calcularValorFatura()));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Simula o valor da fatura de um medidor industrial em todas as
     * {@link BandeiraTarifaria} disponíveis, permitindo comparar o impacto
     * financeiro de cada bandeira.
     *
     * @param medidor medidor industrial base para a simulação
     * @return mapa ordenado com o valor da fatura em cada bandeira
     * @throws RegraTarifacaoException se os dados do medidor violarem alguma regra
     */
    public Map<BandeiraTarifaria, BigDecimal> compararBandeiras(MedidorIndustrial medidor)
            throws RegraTarifacaoException {
        Map<BandeiraTarifaria, BigDecimal> comparativo = new EnumMap<>(BandeiraTarifaria.class);
        for (BandeiraTarifaria bandeira : BandeiraTarifaria.values()) {
            MedidorIndustrial simulacao = new MedidorIndustrial(
                    medidor.getMacAddress(),
                    medidor.getEndereco(),
                    medidor.getConsumoKwh(),
                    medidor.getFatorDemandaContratada(),
                    bandeira);
            BigDecimal valor = BigDecimal.valueOf(simulacao.calcularValorFatura())
                    .setScale(2, RoundingMode.HALF_UP);
            comparativo.put(bandeira, valor);
        }
        return comparativo;
    }
}
