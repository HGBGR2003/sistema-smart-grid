package br.edu.ifgoiano.smartgrid.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Superclasse abstrata de todos os medidores inteligentes da rede (Smart Grid).
 * <p>
 * Reúne o que todo medidor tem em comum, independentemente do tipo de
 * consumidor:
 * <ul>
 *   <li>identificação (endereço MAC) e localização (endereço físico);</li>
 *   <li>tensão atual da rede no ponto de medição;</li>
 *   <li>status operacional, controlado por um disjuntor virtual;</li>
 *   <li>curva de carga horária (consumo registrado em cada hora do dia).</li>
 * </ul>
 * Cada tipo de medidor deve estender esta classe e informar o seu tipo de
 * consumidor em {@link #getTipoConsumidor()}.
 * <p>
 * <b>Serialização:</b> a classe implementa {@link Serializable} e declara
 * {@code serialVersionUID}. O atributo {@code tempoRespostaPingMs} é
 * {@code transient}, pois é uma medição momentânea da comunicação com o
 * medidor e não faz sentido ser salva; após desserializar, ele volta a 0.
 *
 * @author Lara Antonia Marques Oliveira
 * @version 1.0
 */
public abstract class MedidorInteligente implements Serializable {

    /** Versão da classe para a serialização. */
    private static final long serialVersionUID = 1L;

    /** Tensão nominal da rede em Volts, usada como padrão. */
    public static final double TENSAO_NOMINAL = 220.0;

    /** Status do medidor com o disjuntor virtual ligado. */
    public static final String STATUS_ATIVO = "ATIVO";

    /** Status do medidor com o disjuntor virtual desligado. */
    public static final String STATUS_DESLIGADO = "DESLIGADO";

    /** Endereço MAC: identificador único e imutável do medidor. */
    private final String macAddress;

    /** Endereço físico onde o medidor está instalado. */
    private String endereco;

    /** Tensão atual da rede no ponto de medição, em Volts. */
    private double tensaoAtual;

    /** "ATIVO" ou "DESLIGADO", conforme o disjuntor virtual. */
    private String statusOperacional;

    /** Consumo por hora do dia: chave = hora (0 a 23), valor = kWh. */
    private final TreeMap<Integer, Double> curvaCargaHoraria = new TreeMap<>();

    /** Latência da última comunicação, em ms. Não é serializada. */
    private transient long tempoRespostaPingMs;

    /**
     * Cria um medidor com a tensão nominal de 220 V e status ativo.
     *
     * @param macAddress endereço MAC do medidor (identificador único)
     * @param endereco   endereço físico onde o medidor está instalado
     * @throws IllegalArgumentException se o MAC for nulo ou vazio
     */
    protected MedidorInteligente(String macAddress, String endereco) {
        this(macAddress, endereco, TENSAO_NOMINAL);
    }

    /**
     * Cria um medidor informando a tensão lida no momento da instalação.
     *
     * @param macAddress  endereço MAC do medidor (identificador único)
     * @param endereco    endereço físico onde o medidor está instalado
     * @param tensaoAtual tensão inicial em Volts
     * @throws IllegalArgumentException se o MAC for nulo ou vazio, ou se a
     *                                  tensão for negativa
     */
    protected MedidorInteligente(String macAddress, String endereco, double tensaoAtual) {
        if (macAddress == null || macAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("O endereço MAC do medidor é obrigatório.");
        }
        validarTensao(tensaoAtual);
        this.macAddress = macAddress;
        this.endereco = endereco;
        this.tensaoAtual = tensaoAtual;
        this.statusOperacional = STATUS_ATIVO;
        this.tempoRespostaPingMs = 0L;
    }

    /**
     * Tipo de consumidor atendido pelo medidor. Cada subclasse define o seu.
     *
     * @return por exemplo "RESIDENCIAL" ou "INDUSTRIAL"
     */
    public abstract String getTipoConsumidor();

    // ------------------------------------------------------------------
    // Disjuntor virtual
    // ------------------------------------------------------------------

    /** Desliga o disjuntor virtual do medidor (status passa a DESLIGADO). */
    public void desligarDisjuntorVirtual() {
        this.statusOperacional = STATUS_DESLIGADO;
    }

    /** Religa o disjuntor virtual do medidor (status volta a ATIVO). */
    public void religarDisjuntorVirtual() {
        this.statusOperacional = STATUS_ATIVO;
    }

    /**
     * Informa se o medidor está operando.
     *
     * @return {@code true} se o disjuntor virtual estiver ligado
     */
    public boolean isAtivo() {
        return STATUS_ATIVO.equals(statusOperacional);
    }

    // ------------------------------------------------------------------
    // Curva de carga horária
    // ------------------------------------------------------------------

    /**
     * Registra uma leitura de consumo em uma hora do dia. Leituras na mesma
     * hora são somadas.
     *
     * @param hora       hora do dia, de 0 a 23
     * @param consumoKwh consumo lido em kWh (não pode ser negativo)
     * @throws IllegalArgumentException se a hora ou o consumo forem inválidos
     */
    public void registrarConsumoHorario(int hora, double consumoKwh) {
        if (hora < 0 || hora > 23) {
            throw new IllegalArgumentException("Hora inválida: " + hora + ". Use valores de 0 a 23.");
        }
        if (consumoKwh < 0) {
            throw new IllegalArgumentException("O consumo horário não pode ser negativo: " + consumoKwh);
        }
        curvaCargaHoraria.merge(hora, consumoKwh, Double::sum);
    }

    /**
     * Retorna a curva de carga em modo somente leitura, ordenada por hora.
     * Para alterar, use {@link #registrarConsumoHorario(int, double)}.
     *
     * @return mapa hora → kWh
     */
    public Map<Integer, Double> getCurvaCargaHoraria() {
        return Collections.unmodifiableMap(curvaCargaHoraria);
    }

    /**
     * Soma todo o consumo registrado na curva de carga.
     *
     * @return total em kWh
     */
    public double getConsumoTotalCurva() {
        return curvaCargaHoraria.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    /**
     * Hora do dia com o maior consumo registrado.
     *
     * @return a hora de pico, ou vazio se não houver leituras
     */
    public Optional<Integer> getHorarioDePico() {
        return curvaCargaHoraria.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    // ------------------------------------------------------------------
    // Getters e setters
    // ------------------------------------------------------------------

    /**
     * Retorna o identificador do medidor.
     *
     * @return endereço MAC (não muda depois de criado)
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Retorna a localização do medidor.
     *
     * @return endereço físico do medidor
     */
    public String getEndereco() {
        return endereco;
    }

    /**
     * Atualiza a localização do medidor.
     *
     * @param endereco novo endereço físico do medidor
     */
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    /**
     * Retorna a última tensão lida.
     *
     * @return tensão atual em Volts (padrão 220 V)
     */
    public double getTensaoAtual() {
        return tensaoAtual;
    }

    /**
     * Atualiza a tensão lida. Qualquer valor não negativo é aceito, pois o
     * medidor precisa registrar também as leituras anormais da rede.
     *
     * @param tensaoAtual tensão em Volts
     * @throws IllegalArgumentException se a tensão for negativa
     */
    public void setTensaoAtual(double tensaoAtual) {
        validarTensao(tensaoAtual);
        this.tensaoAtual = tensaoAtual;
    }

    /**
     * Retorna o status do disjuntor virtual.
     *
     * @return "ATIVO" ou "DESLIGADO"
     */
    public String getStatusOperacional() {
        return statusOperacional;
    }

    /**
     * Retorna a latência da última comunicação (atributo transient).
     *
     * @return latência em ms (0 após desserializar)
     */
    public long getTempoRespostaPingMs() {
        return tempoRespostaPingMs;
    }

    /**
     * Registra a latência da última comunicação com o medidor.
     *
     * @param tempoRespostaPingMs latência em ms (não pode ser negativa)
     * @throws IllegalArgumentException se o valor for negativo
     */
    public void setTempoRespostaPingMs(long tempoRespostaPingMs) {
        if (tempoRespostaPingMs < 0) {
            throw new IllegalArgumentException("O tempo de resposta não pode ser negativo.");
        }
        this.tempoRespostaPingMs = tempoRespostaPingMs;
    }

    private static void validarTensao(double tensao) {
        if (tensao < 0) {
            throw new IllegalArgumentException("A tensão não pode ser negativa: " + tensao + " V");
        }
    }

    // ------------------------------------------------------------------
    // equals, hashCode e toString
    // ------------------------------------------------------------------

    /**
     * Dois medidores são iguais quando têm o mesmo endereço MAC. Isso permite
     * usar medidores em um {@code Set} sem duplicar o mesmo aparelho.
     *
     * @param o objeto a comparar
     * @return {@code true} se for um medidor com o mesmo MAC
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MedidorInteligente)) {
            return false;
        }
        MedidorInteligente outro = (MedidorInteligente) o;
        return macAddress.equals(outro.macAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(macAddress);
    }

    @Override
    public String toString() {
        return String.format("%s[MAC=%s, endereço=%s, tensão=%.1f V, status=%s]",
                getTipoConsumidor(), macAddress, endereco, tensaoAtual, statusOperacional);
    }
}
