package br.com.myplan.tarefa.api.v2.dto;

import br.com.myplan.tarefa.domain.ResumoTarefas;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Quantidade de tarefas por situação")
public record ResumoResponse(
        @Schema(example = "5") long total,
        @Schema(example = "3") long pendentes,
        @Schema(example = "1") long emAndamento,
        @Schema(example = "1") long concluidas,
        @Schema(example = "0") long atrasadas
) {

    public static ResumoResponse de(ResumoTarefas resumo) {
        return new ResumoResponse(resumo.total(), resumo.pendentes(), resumo.emAndamento(),
                resumo.concluidas(), resumo.atrasadas());
    }
}
