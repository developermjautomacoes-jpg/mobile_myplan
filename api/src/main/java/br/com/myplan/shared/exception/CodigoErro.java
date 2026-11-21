package br.com.myplan.shared.exception;

import org.springframework.http.HttpStatus;

public enum CodigoErro {

    DADOS_INVALIDOS(HttpStatus.BAD_REQUEST, "Dados inválidos",
            "Corrija os campos listados em 'campos' e envie de novo."),

    CORPO_INVALIDO(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido",
            "Envie um JSON válido, só com os campos documentados (titulo, descricao, status, prazo)."),

    PARAMETRO_INVALIDO(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
            "Confira os parâmetros da URL na documentação do endpoint."),

    TAREFA_NAO_ENCONTRADA(HttpStatus.NOT_FOUND, "Tarefa não encontrada",
            "Confira o id. Para ver os ids existentes, liste as tarefas em GET /api/v2/tarefas."),

    ROTA_NAO_ENCONTRADA(HttpStatus.NOT_FOUND, "Endereço não encontrado",
            "Confira o endereço. Os endpoints da API começam com /api/v1 ou /api/v2."),

    VERSAO_NAO_SUPORTADA(HttpStatus.NOT_FOUND, "Versão da API não suportada",
            "Use uma das versões disponíveis: v1 ou v2. A lista fica em GET /api."),

    METODO_NAO_SUPORTADO(HttpStatus.METHOD_NOT_ALLOWED, "Método não suportado",
            "Use um dos métodos informados no header 'Allow' da resposta."),

    TAREFA_DUPLICADA(HttpStatus.CONFLICT, "Tarefa duplicada",
            "Use outro título ou conclua a tarefa que já existe com esse nome."),

    TIPO_DE_CONTEUDO_INVALIDO(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de conteúdo não suportado",
            "Envie o corpo em JSON com o header 'Content-Type: application/json'."),

    REGRA_DE_NEGOCIO(HttpStatus.UNPROCESSABLE_ENTITY, "Operação não permitida",
            "Os dados estão no formato certo, mas violam uma regra do myplan. Leia o 'detalhe'."),

    ERRO_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
            "Tente de novo em alguns instantes. Se continuar, informe o 'requestId' ao suporte.");

    private final HttpStatus status;
    private final String titulo;
    private final String dica;

    CodigoErro(HttpStatus status, String titulo, String dica) {
        this.status = status;
        this.titulo = titulo;
        this.dica = dica;
    }

    public HttpStatus getStatus() { return status; }

    public String getTitulo() { return titulo; }

    public String getDica() { return dica; }

    public String getTipo() {
        return "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#"
                + name().toLowerCase().replace('_', '-');
    }
}
