package br.edu.ifgoiano.smartgrid.model;

import br.edu.ifgoiano.smartgrid.exception.RegraTarifacaoException;

/**
 * Medidor inteligente de consumidores residenciais.
 * <p>
 * Suporta o perfil de <b>prossumidor</b>: a residência pode ter painéis
 * solares e injetar energia na rede. A fatura é calculada sobre o
 * <b>saldo líquido</b> (Net Metering):
 *
 * <pre>
 * saldoLiquido = consumoKwh - injecaoSolarKwh
 * fatura       = saldoLiquido x TARIFA_BASE x adicional da bandeira
 * </pre>
 *
 * Se a injeção solar for maior ou igual ao consumo, a fatura é R$ 0,00 e o
 * excedente vira crédito energético em kWh.
 *
 * @author Philipy Dekam Silva de Oliveira
 * @version 1.0
 */
// Parte do membro 1: a superclasse MedidorInteligente é do Membro 1
public class MedidorResidencial extends MedidorInteligente implements CalculadoraTarifaria {
    private static final long serialVersionUID = 1L;

    /** Tarifa base residencial em R$ por kWh. */
    public static final double TARIFA_BASE = 0.65;

    /** Tensão mínima segura em Volts (220 V - 10%). */
    public static final double TENSAO_MINIMA = 198.0;

    /** Tensão máxima segura em Volts (220 V + 10%). */
    public static final double TENSAO_MAXIMA = 242.0;

    private double consumoKwh;
    private double injecaoSolarKwh;
    // Parte do membro 1: o enum BandeiraTarifaria é do Membro 1
    private BandeiraTarifaria bandeira;

    /**
     * Cria um medidor residencial sem geração solar.
     *
     * @param macAddress endereço MAC do medidor
     * @param endereco   endereço físico da residência
     * @param consumoKwh consumo do mês em kWh
     * @param bandeira   bandeira tarifária vigente
     */
    public MedidorResidencial(String macAddress, String endereco, double consumoKwh, BandeiraTarifaria bandeira) {
        this(macAddress, endereco, consumoKwh, 0.0, bandeira);
    }

    /**
     * Cria um medidor residencial de um prossumidor (com geração solar).
     *
     * @param macAddress      endereço MAC do medidor
     * @param endereco        endereço físico da residência
     * @param consumoKwh      consumo do mês em kWh
     * @param injecaoSolarKwh energia solar injetada na rede no mês, em kWh
     * @param bandeira        bandeira tarifária vigente
     */
    public MedidorResidencial(String macAddress, String endereco, double consumoKwh,
                              double injecaoSolarKwh, BandeiraTarifaria bandeira) {
        // Parte do membro 1: construtor da superclasse MedidorInteligente
        super(macAddress, endereco);
        this.consumoKwh = consumoKwh;
        this.injecaoSolarKwh = injecaoSolarKwh;
        this.bandeira = bandeira;
    }

    /**
     * Calcula a fatura residencial sobre o saldo líquido de energia.
     *
     * @return valor da fatura em R$ (0,00 quando há créditos)
     * @throws RegraTarifacaoException se consumo/injeção forem negativos, a
     *                                 bandeira for nula ou a tensão estiver fora do limite
     */
    @Override
    public double calcularValorFatura() throws RegraTarifacaoException {
        validarRegras();
        double saldo = getSaldoLiquidoKwh();
        if (saldo <= 0) {
            return 0.0;
        }
        // Parte do membro 1: getAdicional() vem do enum BandeiraTarifaria
        return saldo * TARIFA_BASE * bandeira.getAdicional();
    }

    @Override
    public String getTipoConsumidor() {
        return "RESIDENCIAL";
    }

    /**
     * @return consumo menos injeção solar, em kWh (negativo = sobra de energia)
     */
    public double getSaldoLiquidoKwh() {
        return consumoKwh - injecaoSolarKwh;
    }

    /**
     * @return créditos energéticos gerados no mês, em kWh (0 se não houver)
     */
    public double getCreditosKwh() {
        return Math.max(0.0, injecaoSolarKwh - consumoKwh);
    }

    /**
     * @return "A PAGAR", "ZERADA" ou "CRÉDITO"
     */
    public String getStatusFatura() {
        double saldo = getSaldoLiquidoKwh();
        if (saldo > 0) {
            return "A PAGAR";
        } else if (saldo == 0) {
            return "ZERADA";
        }
        return "CRÉDITO";
    }

    /** @return true se a residência injeta energia solar na rede */
    public boolean isProssumidor() {
        return injecaoSolarKwh > 0;
    }

    private void validarRegras() throws RegraTarifacaoException {
        if (consumoKwh < 0) {
            throw new RegraTarifacaoException("Erro: O consumo residencial não pode ser negativo! (" + consumoKwh + " kWh)");
        }
        if (injecaoSolarKwh < 0) {
            throw new RegraTarifacaoException("Erro: A injeção solar não pode ser negativa! (" + injecaoSolarKwh + " kWh)");
        }
        if (bandeira == null) {
            throw new RegraTarifacaoException("Erro: A bandeira tarifária não foi informada!");
        }
        // Parte do membro 1: getTensaoAtual() e isAtivo() vêm da superclasse MedidorInteligente
        if (!isAtivo()) {
            throw new RegraTarifacaoException("Erro: O medidor residencial está com o disjuntor virtual desligado!");
        }
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
            throw new RegraTarifacaoException("Erro: O consumo residencial não pode ser negativo!");
        }
        this.consumoKwh = consumoKwh;
    }

    public double getInjecaoSolarKwh() {
        return injecaoSolarKwh;
    }

    public void setInjecaoSolarKwh(double injecaoSolarKwh) throws RegraTarifacaoException {
        if (injecaoSolarKwh < 0) {
            throw new RegraTarifacaoException("Erro: A injeção solar não pode ser negativa!");
        }
        this.injecaoSolarKwh = injecaoSolarKwh;
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
        return String.format("MedidorResidencial[MAC=%s, endereço=%s, consumo=%.2f kWh, injeção solar=%.2f kWh, bandeira=%s]",
                getMacAddress(), getEndereco(), consumoKwh, injecaoSolarKwh, bandeira);
    }
}
