package br.com.myplan.tarefa.domain;

import java.time.LocalDate;

public record DadosTarefa(
        String titulo,
        String descricao,
        StatusTarefa status,
        LocalDate prazo
) {
}
