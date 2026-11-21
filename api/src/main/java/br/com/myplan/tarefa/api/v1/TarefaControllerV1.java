package br.com.myplan.tarefa.api.v1;

import br.com.myplan.shared.exception.ErroResponse;
import br.com.myplan.tarefa.api.v1.dto.TarefaRequestV1;
import br.com.myplan.tarefa.api.v1.dto.TarefaResponseV1;
import br.com.myplan.tarefa.domain.StatusTarefa;
import br.com.myplan.tarefa.domain.Tarefa;
import br.com.myplan.tarefa.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Mantém o contrato da primeira versão (lista simples, sem paginação) para não quebrar
 * quem já usa a API. Recursos novos entram só na v2.
 */
@Deprecated
@Validated
@RestController
@RequestMapping(value = "/api/v1/tarefas", produces = "application/json")
@Tag(name = "Tarefas v1", description = "Versão antiga, mantida por compatibilidade. Use a v2 em projetos novos.")
public class TarefaControllerV1 {

    private static final String PROBLEM = "application/problem+json";

    private final TarefaService service;

    public TarefaControllerV1(TarefaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todas as tarefas", deprecated = true,
            description = "Devolve todas as tarefas numa lista simples, ordenadas pelo id. Na v2 a lista é paginada.")
    @ApiResponse(responseCode = "200", description = "Lista de tarefas (pode vir vazia)")
    @ApiResponse(responseCode = "400", description = "Status inválido",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<List<TarefaResponseV1>> listar(
            @Parameter(description = "Filtra pelo status") @RequestParam(required = false) StatusTarefa status) {
        List<TarefaResponseV1> tarefas = service.listarTodas(status).stream()
                .map(TarefaResponseV1::de)
                .toList();
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma tarefa", deprecated = true)
    @ApiResponse(responseCode = "200", description = "Tarefa encontrada")
    @ApiResponse(responseCode = "404", description = "Tarefa não existe",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "TarefaNaoEncontrada", ref = "#/components/examples/TarefaNaoEncontrada")))
    public ResponseEntity<TarefaResponseV1> buscar(
            @Parameter(description = "Id da tarefa", example = "1")
            @PathVariable @Positive(message = "O id deve ser um número positivo") Long id) {
        return ResponseEntity.ok(TarefaResponseV1.de(service.buscar(id)));
    }

    @PostMapping(consumes = "application/json")
    @Operation(summary = "Cria uma tarefa", deprecated = true)
    @ApiResponse(responseCode = "201", description = "Tarefa criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "DadosInvalidos", ref = "#/components/examples/DadosInvalidos")))
    @ApiResponse(responseCode = "409", description = "Título repetido em tarefa aberta",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "422", description = "Prazo no passado",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<TarefaResponseV1> criar(@Valid @RequestBody TarefaRequestV1 dados) {
        Tarefa tarefa = service.criar(dados.paraDados());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tarefa.getId())
                .toUri();
        return ResponseEntity.created(uri).body(TarefaResponseV1.de(tarefa));
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    @Operation(summary = "Atualiza uma tarefa", deprecated = true)
    @ApiResponse(responseCode = "200", description = "Tarefa atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Tarefa não existe",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<TarefaResponseV1> atualizar(
            @Parameter(description = "Id da tarefa", example = "1")
            @PathVariable @Positive(message = "O id deve ser um número positivo") Long id,
            @Valid @RequestBody TarefaRequestV1 dados) {
        return ResponseEntity.ok(TarefaResponseV1.de(service.atualizar(id, dados.paraDados())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma tarefa", deprecated = true)
    @ApiResponse(responseCode = "204", description = "Tarefa excluída")
    @ApiResponse(responseCode = "404", description = "Tarefa não existe",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Id da tarefa", example = "1")
            @PathVariable @Positive(message = "O id deve ser um número positivo") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
