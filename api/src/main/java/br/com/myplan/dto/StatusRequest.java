package br.com.myplan.dto;

import br.com.myplan.model.StatusTarefa;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(

        @NotNull(message = "O status é obrigatório")
        StatusTarefa status
) {
}
