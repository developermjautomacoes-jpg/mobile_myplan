package br.com.myplan.exception;

public class TarefaDuplicadaException extends RuntimeException {

    public TarefaDuplicadaException(String titulo) {
        super("Já existe uma tarefa em aberto com o título '" + titulo + "'");
    }
}
