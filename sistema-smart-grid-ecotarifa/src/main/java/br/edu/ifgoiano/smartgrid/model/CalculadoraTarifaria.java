package br.edu.ifgoiano.smartgrid.model;

import br.edu.ifgoiano.smartgrid.exception.RegraTarifacaoException;

/**
 * Interface que define o contrato polimórfico de cálculo de fatura.
 * <p>
 * Todo medidor que implementa esta interface ({@link MedidorResidencial},
 * {@link MedidorIndustrial}) é obrigado a fornecer sua própria regra de
 * cálculo. Assim, o restante do sistema pode tratar qualquer medidor como uma
 * {@code CalculadoraTarifaria} e chamar {@link #calcularValorFatura()} sem
 * saber qual é o tipo concreto (polimorfismo).
 *
 * <pre>
 * List&lt;CalculadoraTarifaria&gt; medidores = List.of(residencial, industrial);
 * for (CalculadoraTarifaria c : medidores) {
 *     System.out.println(c.getTipoConsumidor() + ": R$ " + c.calcularValorFatura());
 * }
 * </pre>
 *
 * @author Philipy Dekam Silva de Oliveira
 * @version 1.0
 */
public interface CalculadoraTarifaria {

    /**
     * Calcula o valor da fatura (em R$) de acordo com a regra de cada tipo de medidor.
     *
     * @return valor da fatura em reais
     * @throws RegraTarifacaoException se alguma regra de tarifação for violada
     */
    double calcularValorFatura() throws RegraTarifacaoException;

    /**
     * Retorna o tipo de consumidor (ex.: "RESIDENCIAL", "INDUSTRIAL").
     *
     * @return nome do tipo de consumidor
     */
    String getTipoConsumidor();

    /**
     * Método padrão que devolve a fatura já formatada em reais.
     * Funciona para qualquer implementação, pois chama o
     * {@link #calcularValorFatura()} específico de cada classe.
     *
     * @return texto no formato "R$ 123,45"
     * @throws RegraTarifacaoException se alguma regra de tarifação for violada
     */
    default String getFaturaFormatada() throws RegraTarifacaoException {
        return String.format("R$ %.2f", calcularValorFatura());
    }
}
