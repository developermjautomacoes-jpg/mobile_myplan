package br.com.myplan.dto;

public record ResumoResponse(
        long total,
        long pendentes,
        long emAndamento,
        long concluidas,
        long atrasadas
) {
}
