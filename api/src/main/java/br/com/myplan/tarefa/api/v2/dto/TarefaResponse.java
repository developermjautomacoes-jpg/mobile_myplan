package br.com.myplan.tarefa.api.v2.dto;

import br.com.myplan.tarefa.domain.StatusTarefa;
import br.com.myplan.tarefa.domain.Tarefa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(name = "TarefaResponseV2", description = "Tarefa como a v2 devolve")
public record TarefaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Pagar conta de luz") String titulo,
        @Schema(example = "Vence no dia 10") String descricao,
        @Schema(example = "PENDENTE") StatusTarefa status,
        @Schema(example = "2026-12-10") LocalDate prazo,
        @Schema(description = "true quando o prazo passou e a tarefa não foi concluída", example = "false") boolean atrasada,
        @Schema(example = "2026-11-20T22:10:32") LocalDateTime dataCriacao,
        @Schema(example = "2026-11-21T09:15:00") LocalDateTime dataAtualizacao
) {

    public static TarefaResponse de(Tarefa tarefa) {
        boolean atrasada = tarefa.getPrazo() != null
                && tarefa.getStatus() != StatusTarefa.CONCLUIDA
                && tarefa.getPrazo().isBefore(LocalDate.now());

        return new TarefaResponse(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus(),
                tarefa.getPrazo(),
                atrasada,
                tarefa.getDataCriacao(),
                tarefa.getDataAtualizacao());
    }
}
