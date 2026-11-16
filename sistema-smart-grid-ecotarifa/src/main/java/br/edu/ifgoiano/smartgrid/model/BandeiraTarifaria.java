/* ============================================================================
 * VERSÃO PROVISÓRIA (STUB PARA COMPILAÇÃO E TESTES DO DAO)
 * Substituir este arquivo quando o Membro 1 enviar a versão oficial.
 * ============================================================================ */
package br.edu.ifgoiano.smartgrid.model;

/**
 * Enum que representa as bandeiras tarifárias e seus adicionais financeiros.
 * 
 * @version 0.1-PROVISORIA (Aguardando código do Membro 1)
 */
public enum BandeiraTarifaria {
    VERDE(1.0),
    AMARELA(1.2),
    VERMELHA(1.5);

    private final double adicional;

    BandeiraTarifaria(double adicional) {
        this.adicional = adicional;
    }

    public double getAdicional() {
        return adicional;
    }
}