package br.com.myplan.tarefa.exception;

import br.com.myplan.shared.exception.ApiException;
import br.com.myplan.shared.exception.CodigoErro;

public class TarefaNaoEncontradaException extends ApiException {

    public TarefaNaoEncontradaException(Long id) {
        super(CodigoErro.TAREFA_NAO_ENCONTRADA, "Não existe tarefa com o id " + id);
    }
}
