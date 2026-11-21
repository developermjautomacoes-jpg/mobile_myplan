package br.com.myplan.dto;

import br.com.myplan.model.StatusTarefa;
import br.com.myplan.model.Tarefa;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TarefaResponse(
        Long id,
        String titulo,
        String descricao,
        StatusTarefa status,
        LocalDate prazo,
        boolean atrasada,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
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
