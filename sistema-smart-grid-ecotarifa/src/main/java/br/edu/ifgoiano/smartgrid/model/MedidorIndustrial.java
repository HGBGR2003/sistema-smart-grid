/* ============================================================================
 * VERSÃO OFICIAL - MEMBRO 2 (Philipy) - Especialista em Polimorfismo
 * Depende de: MedidorInteligente e BandeiraTarifaria (Parte do membro 1)
 * ============================================================================ */
package br.edu.ifgoiano.smartgrid.model;

import br.edu.ifgoiano.smartgrid.exception.RegraTarifacaoException;

/**
 * Medidor inteligente de consumidores industriais.
 * <p>
 * Além da bandeira tarifária, a fatura industrial é multiplicada pelo
 * <b>fator de demanda contratada</b>:
 *
 * <pre>
 * fatura = consumoKwh x TARIFA_BASE x adicional da bandeira x fatorDemandaContratada
 * </pre>
 *
 * Esta classe é usada pelo {@code ConsumoDAOJBC} (Membro 4), por isso o
 * construtor de 5 parâmetros e os getters {@code getConsumoKwh()},
 * {@code getFatorDemandaContratada()} e {@code getBandeira()} não devem mudar.
 *
 * @author Philipy Dekam Silva de Oliveira
 * @version 1.0
 */
// Parte do membro 1: a superclasse MedidorInteligente é do Membro 1
public class MedidorIndustrial extends MedidorInteligente implements CalculadoraTarifaria {
    private static final long serialVersionUID = 1L;

    /** Tarifa base industrial em R$ por kWh. */
    public static final double TARIFA_BASE = 0.50;

    /** Tensão mínima segura em Volts (220 V - 10%). */
    public static final double TENSAO_MINIMA = 198.0;

    /** Tensão máxima segura em Volts (220 V + 10%). */
    public static final double TENSAO_MAXIMA = 242.0;

    private double consumoKwh;
    private double fatorDemandaContratada;
    // Parte do membro 1: o enum BandeiraTarifaria é do Membro 1
    private BandeiraTarifaria bandeira;

    /**
     * Cria um medidor industrial.
     *
     * @param macAddress   endereço MAC do medidor
     * @param endereco     endereço físico da indústria
     * @param consumoKwh   consumo do mês em kWh
     * @param fatorDemanda fator de demanda contratada (deve ser maior que zero)
     * @param bandeira     bandeira tarifária vigente
     */
    public MedidorIndustrial(String macAddress, String endereco, double consumoKwh,
                             double fatorDemanda, BandeiraTarifaria bandeira) {
        // Parte do membro 1: construtor da superclasse MedidorInteligente
        super(macAddress, endereco);
        this.consumoKwh = consumoKwh;
        this.fatorDemandaContratada = fatorDemanda;
        this.bandeira = bandeira;
    }

    /**
     * Calcula a fatura industrial aplicando bandeira e fator de demanda.
     *
     * @return valor da fatura em R$
     * @throws RegraTarifacaoException se o consumo for negativo, o fator de
     *                                 demanda for menor ou igual a zero, a bandeira
     *                                 for nula ou a tensão estiver fora do limite
     */
    @Override
    public double calcularValorFatura() throws RegraTarifacaoException {
        validarRegras();
        // Parte do membro 1: getAdicional() vem do enum BandeiraTarifaria
        return consumoKwh * TARIFA_BASE * bandeira.getAdicional() * fatorDemandaContratada;
    }

    @Override
    public String getTipoConsumidor() {
        return "INDUSTRIAL";
    }

    private void validarRegras() throws RegraTarifacaoException {
        if (consumoKwh < 0) {
            throw new RegraTarifacaoException("Erro: O consumo industrial não pode ser negativo! (" + consumoKwh + " kWh)");
        }
        if (fatorDemandaContratada <= 0) {
            throw new RegraTarifacaoException("Erro: O fator de demanda contratada deve ser maior que zero! ("
                    + fatorDemandaContratada + ")");
        }
        if (bandeira == null) {
            throw new RegraTarifacaoException("Erro: A bandeira tarifária não foi informada!");
        }
        // Parte do membro 1: getTensaoAtual() vem da superclasse MedidorInteligente
        double tensao = getTensaoAtual();
        if (tensao < TENSAO_MINIMA || tensao > TENSAO_MAXIMA) {
            throw new RegraTarifacaoException("Erro: Tensão fora do limite seguro (" + tensao
                    + " V). Faixa permitida: " + TENSAO_MINIMA + " V a " + TENSAO_MAXIMA + " V.");
        }
    }

    public double getConsumoKwh() {
        return consumoKwh;
    }

    public void setConsumoKwh(double consumoKwh) throws RegraTarifacaoException {
        if (consumoKwh < 0) {
            throw new RegraTarifacaoException("Erro: O consumo industrial não pode ser negativo!");
        }
        this.consumoKwh = consumoKwh;
    }

    public double getFatorDemandaContratada() {
        return fatorDemandaContratada;
    }

    public void setFatorDemandaContratada(double fatorDemandaContratada) throws RegraTarifacaoException {
        if (fatorDemandaContratada <= 0) {
            throw new RegraTarifacaoException("Erro: O fator de demanda contratada deve ser maior que zero!");
        }
        this.fatorDemandaContratada = fatorDemandaContratada;
    }

    public BandeiraTarifaria getBandeira() {
        return bandeira;
    }

    public void setBandeira(BandeiraTarifaria bandeira) throws RegraTarifacaoException {
        if (bandeira == null) {
            throw new RegraTarifacaoException("Erro: A bandeira tarifária não pode ser nula!");
        }
        this.bandeira = bandeira;
    }

    @Override
    public String toString() {
        // Parte do membro 1: getMacAddress() e getEndereco() vêm de MedidorInteligente
        return String.format("MedidorIndustrial[MAC=%s, endereço=%s, consumo=%.2f kWh, fator demanda=%.2f, bandeira=%s]",
                getMacAddress(), getEndereco(), consumoKwh, fatorDemandaContratada, bandeira);
    }
}
