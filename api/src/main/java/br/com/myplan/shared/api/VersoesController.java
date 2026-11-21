package br.com.myplan.shared.api;

import br.com.myplan.shared.config.VersaoApiFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Versões", description = "Versões disponíveis da API")
public class VersoesController {

    @Schema(description = "Versão da API")
    public record Versao(
            @Schema(example = "v2") String versao,
            @Schema(example = "/api/v2/tarefas") String endereco,
            @Schema(example = "ATUAL") String situacao,
            @Schema(description = "Data em que a versão deixa de funcionar, quando houver") String fimDoSuporte,
            @Schema(example = "Paginação, filtros, ordenação, PATCH de status e resumo") String novidades
    ) {
    }

    @GetMapping(value = "/api", produces = "application/json")
    @Operation(summary = "Lista as versões da API")
    public ResponseEntity<List<Versao>> versoes() {
        return ResponseEntity.ok(List.of(
                new Versao("v1", "/api/v1/tarefas", "DEPRECIADA", VersaoApiFilter.FIM_DO_SUPORTE_V1,
                        "CRUD básico com lista simples"),
                new Versao("v2", "/api/v2/tarefas", "ATUAL", null,
                        "Paginação, filtros, ordenação, PATCH de status, resumo e campo 'atrasada'")));
    }
}
