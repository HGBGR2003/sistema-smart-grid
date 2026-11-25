package br.edu.ifgoiano.smartgrid.exception;

/**
 * Exceção personalizada (checked) lançada quando alguma regra de tarifação do
 * Smart Grid é violada, por exemplo:
 * <ul>
 *   <li>consumo ou injeção solar negativos;</li>
 *   <li>fator de demanda contratada inválido (menor ou igual a zero);</li>
 *   <li>bandeira tarifária não informada;</li>
 *   <li>tensão elétrica fora dos limites seguros;</li>
 *   <li>medidor com o disjuntor virtual desligado.</li>
 * </ul>
 *
 * Por ser uma exceção verificada (estende {@link Exception}), o compilador
 * obriga quem chama {@code calcularValorFatura()} a tratar o erro.
 *
 * @author Philipy Dekam Silva de Oliveira
 * @version 1.0
 */
public class RegraTarifacaoException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com uma mensagem explicando a regra violada.
     *
     * @param mensagem descrição do erro
     */
    public RegraTarifacaoException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria a exceção com uma mensagem e a causa original do erro.
     *
     * @param mensagem descrição do erro
     * @param causa    exceção que originou o problema
     */
    public RegraTarifacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
