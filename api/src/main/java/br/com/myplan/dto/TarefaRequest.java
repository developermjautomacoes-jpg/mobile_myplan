package br.com.myplan.dto;

import br.com.myplan.model.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TarefaRequest(

        @NotBlank(message = "O título é obrigatório")
        @Size(min = 3, max = 100, message = "O título deve ter entre 3 e 100 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "O título não pode conter os caracteres < ou >")
        String titulo,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "A descrição não pode conter os caracteres < ou >")
        String descricao,

        StatusTarefa status,

        LocalDate prazo
) {
}
