package br.com.myplan.shared.exception;

public class ParametroInvalidoException extends ApiException {

    public ParametroInvalidoException(String detalhe) {
        super(CodigoErro.PARAMETRO_INVALIDO, detalhe);
    }
}
