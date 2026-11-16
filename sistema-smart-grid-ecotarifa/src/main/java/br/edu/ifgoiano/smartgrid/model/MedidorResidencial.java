/* ============================================================================
 * VERSÃO PROVISÓRIA (STUB PARA COMPILAÇÃO E TESTES)
 * Substituir este arquivo quando o Membro 2 enviar a versão oficial.
 * ============================================================================ */
package br.edu.ifgoiano.smartgrid.model;

import br.edu.ifgoiano.smartgrid.exception.RegraTarifacaoException;

/**
 * Subclasse especializada para consumidores residenciais.
 * Garante o requisito da rubrica de ter pelo menos duas subclasses especializadas.
 * 
 * @version 0.1-PROVISORIA (Aguardando código do Membro 2)
 */
public class MedidorResidencial extends MedidorInteligente implements CalculadoraTarifaria {
    private static final long serialVersionUID = 1L;

    private double consumoKwh;
    private BandeiraTarifaria bandeira;

    public MedidorResidencial(String macAddress, String endereco, double consumoKwh, BandeiraTarifaria bandeira) {
        super(macAddress, endereco);
        this.consumoKwh = consumoKwh;
        this.bandeira = bandeira;
    }

    @Override
    public double calcularValorFatura() throws RegraTarifacaoException {
        if (consumoKwh < 0) {
            throw new RegraTarifacaoException("Erro: O consumo residencial não pode ser negativo!");
        }
        double tarifaBase = 0.65;
        return this.consumoKwh * tarifaBase * this.bandeira.getAdicional();
    }

    public double getConsumoKwh() {
        return consumoKwh;
    }

    public BandeiraTarifaria getBandeira() {
        return bandeira;
    }
}