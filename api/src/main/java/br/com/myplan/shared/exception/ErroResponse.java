package br.com.myplan.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Erro no formato Problem Details (RFC 9457), com campos extras do myplan")
public record ErroResponse(

        @Schema(description = "Link para a explicação do erro no catálogo",
                example = "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#tarefa-nao-encontrada")
        String type,

        @Schema(description = "Resumo do erro", example = "Tarefa não encontrada")
        String title,

        @Schema(description = "Status HTTP", example = "404")
        int status,

        @Schema(description = "O que aconteceu nesta requisição", example = "Não existe tarefa com o id 99")
        String detail,

        @Schema(description = "Endereço chamado", example = "/api/v2/tarefas/99")
        String instance,

        @Schema(description = "Código do erro, para o cliente tratar cada caso", example = "TAREFA_NAO_ENCONTRADA")
        String codigo,

        @Schema(description = "Como resolver",
                example = "Confira o id. Para ver os ids existentes, liste as tarefas em GET /api/v2/tarefas.")
        String dica,

        @Schema(description = "Erros por campo, quando houver")
        Map<String, String> campos,

        @Schema(description = "Id da requisição, o mesmo do header X-Request-Id")
        String requestId,

        LocalDateTime timestamp
) {
}
