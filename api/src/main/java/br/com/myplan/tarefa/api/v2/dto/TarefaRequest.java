package br.com.myplan.tarefa.api.v2.dto;

import br.com.myplan.tarefa.domain.DadosTarefa;
import br.com.myplan.tarefa.domain.StatusTarefa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(name = "TarefaRequestV2", description = "Dados para criar ou atualizar uma tarefa")
public record TarefaRequest(

        @Schema(description = "Título da tarefa. Não pode repetir o de outra tarefa em aberto",
                example = "Pagar conta de luz", minLength = 3, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 3, max = 100, message = "O título deve ter entre 3 e 100 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "O título não pode conter os caracteres < ou >")
        String titulo,

        @Schema(description = "Detalhes da tarefa", example = "Vence no dia 10", maxLength = 500)
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "A descrição não pode conter os caracteres < ou >")
        String descricao,

        @Schema(description = "Situação da tarefa. Se não for enviado, fica PENDENTE", example = "PENDENTE")
        StatusTarefa status,

        @Schema(description = "Data limite (aaaa-mm-dd). Não pode ser uma data que já passou", example = "2026-12-10")
        LocalDate prazo
) {

    public DadosTarefa paraDados() {
        return new DadosTarefa(titulo, descricao, status, prazo);
    }
}
