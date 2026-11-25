package br.edu.ifgoiano.smartgrid.model;

/**
 * Bandeiras tarifárias aplicadas pelo sistema EcoTarifa.
 * <p>
 * Cada bandeira carrega seus próprios atributos, definidos no construtor do
 * enum:
 * <ul>
 *   <li><b>adicional</b>: multiplicador aplicado sobre a tarifa base
 *       (1.0 = sem acréscimo, 1.2 = +20%, 1.5 = +50%);</li>
 *   <li><b>descricao</b>: explicação da situação de geração de energia;</li>
 *   <li><b>recomendacao</b>: orientação de consumo exibida ao cliente.</li>
 * </ul>
 *
 * <pre>
 * double valor = 100.0 * BandeiraTarifaria.VERMELHA.getAdicional(); // 150.0
 * </pre>
 *
 * Observação: o {@code toString()} não é sobrescrito de propósito. Assim ele
 * continua devolvendo o nome da constante ("VERDE", "AMARELA", "VERMELHA"),
 * que é o valor gravado no banco e lido de volta com {@link #valueOf(String)}.
 *
 * @author Lara Antonia Marques Oliveira
 * @version 1.0
 */
public enum BandeiraTarifaria {

    /** Condições favoráveis de geração: tarifa sem acréscimo. */
    VERDE(1.0,
            "Condições favoráveis de geração de energia",
            "Consumo liberado em qualquer horário, sem acréscimo na tarifa."),

    /** Condições menos favoráveis: tarifa com acréscimo de 20%. */
    AMARELA(1.2,
            "Condições de geração menos favoráveis",
            "Evite ligar equipamentos de alto consumo no horário de pico."),

    /** Condições críticas de geração: tarifa com acréscimo de 50%. */
    VERMELHA(1.5,
            "Condições críticas de geração (custo elevado)",
            "Desloque o consumo pesado para a madrugada ou fora do horário de pico.");

    private final double adicional;
    private final String descricao;
    private final String recomendacao;

    /**
     * Construtor do enum (sempre privado). Executado uma vez para cada
     * constante quando a classe é carregada.
     *
     * @param adicional    multiplicador aplicado sobre a tarifa base
     * @param descricao    descrição da situação de geração
     * @param recomendacao orientação de consumo para o cliente
     */
    BandeiraTarifaria(double adicional, String descricao, String recomendacao) {
        this.adicional = adicional;
        this.descricao = descricao;
        this.recomendacao = recomendacao;
    }

    /**
     * Multiplicador da bandeira, usado no cálculo da fatura pelos medidores.
     *
     * @return 1.0 (verde), 1.2 (amarela) ou 1.5 (vermelha)
     */
    public double getAdicional() {
        return adicional;
    }

    /**
     * Explica a situação de geração que levou a esta bandeira.
     *
     * @return descrição da situação de geração de energia
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Orientação exibida ao cliente enquanto esta bandeira estiver vigente.
     *
     * @return orientação de consumo para o cliente
     */
    public String getRecomendacao() {
        return recomendacao;
    }

    /**
     * Acréscimo da bandeira em porcentagem.
     *
     * @return 0 (verde), 20 (amarela) ou 50 (vermelha)
     */
    public int getPercentualAcrescimo() {
        return (int) Math.round((adicional - 1.0) * 100.0);
    }

    /**
     * Aplica o adicional da bandeira sobre um valor base.
     *
     * @param valorBase valor sem bandeira, em R$
     * @return valor com o adicional da bandeira, em R$
     */
    public double aplicarAdicional(double valorBase) {
        return valorBase * adicional;
    }

    /**
     * Indica se a bandeira exige atenção do consumidor (amarela ou vermelha).
     *
     * @return {@code true} se houver acréscimo na tarifa
     */
    public boolean isBandeiraDeAlerta() {
        return adicional > 1.0;
    }

    /**
     * Converte um texto na bandeira correspondente, ignorando maiúsculas,
     * minúsculas e espaços (ex.: " vermelha " vira {@link #VERMELHA}).
     *
     * @param nome nome da bandeira
     * @return a bandeira correspondente
     * @throws IllegalArgumentException se o nome for nulo ou não existir
     */
    public static BandeiraTarifaria deNome(String nome) {
        if (nome == null) {
            throw new IllegalArgumentException("Nome da bandeira não pode ser nulo.");
        }
        for (BandeiraTarifaria b : values()) {
            if (b.name().equalsIgnoreCase(nome.trim())) {
                return b;
            }
        }
        throw new IllegalArgumentException("Bandeira tarifária inexistente: " + nome);
    }
}
