package br.com.myplan.shared.exception;

public class RegraNegocioException extends ApiException {

    public RegraNegocioException(String detalhe) {
        super(CodigoErro.REGRA_DE_NEGOCIO, detalhe);
    }
}
