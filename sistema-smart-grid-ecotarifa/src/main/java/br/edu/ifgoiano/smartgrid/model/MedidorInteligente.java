/* ============================================================================
 * VERSÃO PROVISÓRIA (STUB PARA COMPILAÇÃO E TESTES DO DAO)
 * Substituir este arquivo quando o Membro 1 enviar a versão oficial.
 * ============================================================================ */
package br.edu.ifgoiano.smartgrid.model;

import java.io.Serializable;

/**
 * Superclasse abstrata que define a base para qualquer medidor da rede.
 * 
 * @version 0.1-PROVISORIA (Aguardando código do Membro 1)
 */
public abstract class MedidorInteligente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String macAddress;
    private String endereco;
    private double tensaoAtual;
    private String statusOperacional;
    // private transient long tempoRespostaPingMs; 

    /*
    QUEM FOR MEXER NESSA PARTE SÓ DESCOMENTA!😉
    */

    public MedidorInteligente(String macAddress, String endereco) {
        this.macAddress = macAddress;
        this.endereco = endereco;
        this.tensaoAtual = 220.0;
        this.statusOperacional = "ATIVO";
        // this.tempoRespostaPingMs = 15;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getEndereco() {
        return endereco;
    }

    public double getTensaoAtual() {
        return tensaoAtual;
    }

    public String getStatusOperacional() {
        return statusOperacional;
    }
}