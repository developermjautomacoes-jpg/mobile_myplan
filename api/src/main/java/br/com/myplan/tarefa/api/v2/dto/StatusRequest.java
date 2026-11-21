package br.com.myplan.tarefa.api.v2.dto;

import br.com.myplan.tarefa.domain.StatusTarefa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Novo status da tarefa")
public record StatusRequest(

        @Schema(example = "CONCLUIDA", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O status é obrigatório")
        StatusTarefa status
) {
}
