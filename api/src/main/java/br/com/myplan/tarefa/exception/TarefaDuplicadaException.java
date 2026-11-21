package br.com.myplan.tarefa.exception;

import br.com.myplan.shared.exception.ApiException;
import br.com.myplan.shared.exception.CodigoErro;

public class TarefaDuplicadaException extends ApiException {

    public TarefaDuplicadaException(String titulo) {
        super(CodigoErro.TAREFA_DUPLICADA, "Já existe uma tarefa em aberto com o título '" + titulo + "'");
    }
}
