package br.com.myplan.tarefa.api.v1.dto;

import br.com.myplan.tarefa.domain.DadosTarefa;
import br.com.myplan.tarefa.domain.StatusTarefa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para criar ou atualizar uma tarefa (v1)")
public record TarefaRequestV1(

        @Schema(example = "Pagar conta de luz", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "O título não pode conter os caracteres < ou >")
        String titulo,

        @Schema(example = "Vence no dia 10", maxLength = 500)
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "A descrição não pode conter os caracteres < ou >")
        String descricao,

        @Schema(example = "PENDENTE")
        StatusTarefa status,

        @Schema(example = "2026-12-10")
        LocalDate prazo
) {

    public DadosTarefa paraDados() {
        return new DadosTarefa(titulo, descricao, status, prazo);
    }
}
