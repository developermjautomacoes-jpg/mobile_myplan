package br.com.myplan.controller;

import br.com.myplan.dto.PaginaResponse;
import br.com.myplan.dto.ResumoResponse;
import br.com.myplan.dto.StatusRequest;
import br.com.myplan.dto.TarefaRequest;
import br.com.myplan.dto.TarefaResponse;
import br.com.myplan.model.StatusTarefa;
import br.com.myplan.model.Tarefa;
import br.com.myplan.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Validated
@RestController
@RequestMapping(value = "/api/tarefas", produces = "application/json")
@Tag(name = "Tarefas")
public class TarefaController {

    private final TarefaService service;

    public TarefaController(TarefaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista as tarefas com filtros, paginação e ordenação")
    public ResponseEntity<PaginaResponse<TarefaResponse>> listar(
            @RequestParam(required = false) StatusTarefa status,
            @RequestParam(required = false) @Size(max = 100, message = "A busca deve ter no máximo 100 caracteres") String busca,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página não pode ser negativa") int pagina,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "O tamanho mínimo é 1")
            @Max(value = 50, message = "O tamanho máximo é 50") int tamanho,
            @RequestParam(defaultValue = "dataCriacao") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direcao) {

        Page<Tarefa> page = service.listar(status, busca, pagina, tamanho, ordenarPor, direcao);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.getTotalElements()))
                .body(PaginaResponse.de(page, TarefaResponse::de));
    }

    @GetMapping("/resumo")
    @Operation(summary = "Mostra quantas tarefas existem em cada status")
    public ResponseEntity<ResumoResponse> resumo() {
        return ResponseEntity.ok(service.resumo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma tarefa pelo id")
    public ResponseEntity<TarefaResponse> buscar(@PathVariable @Positive(message = "O id deve ser positivo") Long id) {
        return ResponseEntity.ok(TarefaResponse.de(service.buscar(id)));
    }

    @PostMapping(consumes = "application/json")
    @Operation(summary = "Cria uma tarefa")
    public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody TarefaRequest dados) {
        Tarefa tarefa = service.criar(dados);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tarefa.getId())
                .toUri();
        return ResponseEntity.created(uri).body(TarefaResponse.de(tarefa));
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    @Operation(summary = "Atualiza todos os dados de uma tarefa")
    public ResponseEntity<TarefaResponse> atualizar(@PathVariable @Positive(message = "O id deve ser positivo") Long id,
                                                    @Valid @RequestBody TarefaRequest dados) {
        return ResponseEntity.ok(TarefaResponse.de(service.atualizar(id, dados)));
    }

    @PatchMapping(value = "/{id}/status", consumes = "application/json")
    @Operation(summary = "Altera apenas o status de uma tarefa")
    public ResponseEntity<TarefaResponse> alterarStatus(@PathVariable @Positive(message = "O id deve ser positivo") Long id,
                                                        @Valid @RequestBody StatusRequest dados) {
        return ResponseEntity.ok(TarefaResponse.de(service.alterarStatus(id, dados.status())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma tarefa")
    public ResponseEntity<Void> excluir(@PathVariable @Positive(message = "O id deve ser positivo") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
