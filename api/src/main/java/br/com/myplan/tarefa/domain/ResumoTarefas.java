package br.com.myplan.tarefa.domain;

public record ResumoTarefas(
        long total,
        long pendentes,
        long emAndamento,
        long concluidas,
        long atrasadas
) {
}
