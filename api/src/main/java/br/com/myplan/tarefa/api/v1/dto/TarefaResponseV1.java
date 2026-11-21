package br.com.myplan.tarefa.api.v1.dto;

import br.com.myplan.tarefa.domain.StatusTarefa;
import br.com.myplan.tarefa.domain.Tarefa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Tarefa no formato da v1 (o mesmo da primeira versão da API)")
public record TarefaResponseV1(
        @Schema(example = "1") Long id,
        @Schema(example = "Pagar conta de luz") String titulo,
        @Schema(example = "Vence no dia 10") String descricao,
        @Schema(example = "PENDENTE") StatusTarefa status,
        @Schema(example = "2026-12-10") LocalDate prazo,
        @Schema(example = "2026-11-20T22:10:32") LocalDateTime dataCriacao,
        @Schema(example = "2026-11-21T09:15:00") LocalDateTime dataAtualizacao
) {

    public static TarefaResponseV1 de(Tarefa tarefa) {
        return new TarefaResponseV1(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus(),
                tarefa.getPrazo(),
                tarefa.getDataCriacao(),
                tarefa.getDataAtualizacao());
    }
}
