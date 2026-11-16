/* ============================================================================
 * VERSÃO PROVISÓRIA (STUB PARA COMPILAÇÃO E TESTES DO DAO)
 * Substituir este arquivo quando o Membro 2 enviar a versão oficial.
 * ============================================================================ */
package br.edu.ifgoiano.smartgrid.model;

import br.edu.ifgoiano.smartgrid.exception.RegraTarifacaoException;

/**
 * Subclasse especializada para consumidores industriais.
 * 
 * @version 0.1-PROVISORIA (Aguardando código do Membro 2)
 */
public class MedidorIndustrial extends MedidorInteligente implements CalculadoraTarifaria {
    private static final long serialVersionUID = 1L;

    private double consumoKwh;
    private double fatorDemandaContratada;
    private BandeiraTarifaria bandeira;

    public MedidorIndustrial(String macAddress, String endereco, double consumoKwh, double fatorDemanda, BandeiraTarifaria bandeira) {
        super(macAddress, endereco);
        this.consumoKwh = consumoKwh;
        this.fatorDemandaContratada = fatorDemanda;
        this.bandeira = bandeira;
    }

    @Override
    public double calcularValorFatura() throws RegraTarifacaoException {
        if (consumoKwh < 0) {
            throw new RegraTarifacaoException("Erro: O consumo não pode ser negativo!");
        }
        double tarifaBase = 0.50;
        return (this.consumoKwh * tarifaBase * this.bandeira.getAdicional()) * this.fatorDemandaContratada;
    }

    public double getConsumoKwh() {
        return consumoKwh;
    }

    public double getFatorDemandaContratada() {
        return fatorDemandaContratada;
    }

    public BandeiraTarifaria getBandeira() {
        return bandeira;
    }
}