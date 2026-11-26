package br.edu.ifgoiano.smartgrid.view;

/**
 * Classe auxiliar de inicialização que delega a execução para {@link MainView}.
 *
 * @author Jeanderson Honorato de Souza
 * @version 1.0
 */
public class Main {
    /**
     * Inicia o sistema repassando os argumentos para {@link MainView#main(String[])}.
     *
     * @param args argumentos de linha de comando; aceita {@code --console}
     */
    public static void main(String[] args) {
        MainView.main(args);
    }
}
