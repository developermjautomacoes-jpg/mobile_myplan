package br.com.myplan.shared.exception;

public abstract class ApiException extends RuntimeException {

    private final CodigoErro codigo;

    protected ApiException(CodigoErro codigo, String detalhe) {
        super(detalhe);
        this.codigo = codigo;
    }

    public CodigoErro getCodigo() {
        return codigo;
    }
}
