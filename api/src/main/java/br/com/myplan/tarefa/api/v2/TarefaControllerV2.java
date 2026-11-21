package br.com.myplan.tarefa.api.v2;

import br.com.myplan.shared.exception.ErroResponse;
import br.com.myplan.tarefa.api.v2.dto.PaginaResponse;
import br.com.myplan.tarefa.api.v2.dto.ResumoResponse;
import br.com.myplan.tarefa.api.v2.dto.StatusRequest;
import br.com.myplan.tarefa.api.v2.dto.TarefaRequest;
import br.com.myplan.tarefa.api.v2.dto.TarefaResponse;
import br.com.myplan.tarefa.domain.StatusTarefa;
import br.com.myplan.tarefa.domain.Tarefa;
import br.com.myplan.tarefa.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping(value = {"/api/v2/tarefas", "/api/tarefas"}, produces = "application/json")
@Tag(name = "Tarefas v2", description = "Versão atual da API de tarefas")
public class TarefaControllerV2 {

    private static final String PROBLEM = "application/problem+json";

    private final TarefaService service;

    public TarefaControllerV2(TarefaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista as tarefas",
            description = "Lista as tarefas com filtro por status, busca no título, paginação e ordenação. "
                    + "O header X-Total-Count traz o total encontrado.")
    @ApiResponse(responseCode = "200", description = "Página de tarefas (pode vir vazia)",
            headers = @Header(name = "X-Total-Count", description = "Total de tarefas encontradas"))
    @ApiResponse(responseCode = "400", description = "Parâmetro inválido",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "ParametroInvalido", ref = "#/components/examples/ParametroInvalido")))
    public ResponseEntity<PaginaResponse<TarefaResponse>> listar(
            @Parameter(description = "Filtra pelo status") @RequestParam(required = false) StatusTarefa status,
            @Parameter(description = "Procura no título, sem diferenciar maiúsculas", example = "pagar")
            @RequestParam(required = false) @Size(max = 100, message = "A busca deve ter no máximo 100 caracteres") String busca,
            @Parameter(description = "Página, começando em 0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página não pode ser negativa") int pagina,
            @Parameter(description = "Itens por página, de 1 a 50")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "O tamanho mínimo é 1")
            @Max(value = 50, message = "O tamanho máximo é 50") int tamanho,
            @Parameter(description = "Campo de ordenação: titulo, prazo, status ou dataCriacao")
            @RequestParam(defaultValue = "dataCriacao") String ordenarPor,
            @Parameter(description = "Direção: asc ou desc")
            @RequestParam(defaultValue = "desc") String direcao) {

        Page<Tarefa> page = service.listar(status, busca, pagina, tamanho, ordenarPor, direcao);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.getTotalElements()))
                .body(PaginaResponse.de(page, TarefaResponse::de));
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resumo por status", description = "Quantidade de tarefas pendentes, em andamento, concluídas e atrasadas.")
    @ApiResponse(responseCode = "200", description = "Resumo")
    public ResponseEntity<ResumoResponse> resumo() {
        return ResponseEntity.ok(ResumoResponse.de(service.resumo()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma tarefa")
    @ApiResponse(responseCode = "200", description = "Tarefa encontrada")
    @ApiResponse(responseCode = "400", description = "Id inválido",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Tarefa não existe",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "TarefaNaoEncontrada", ref = "#/components/examples/TarefaNaoEncontrada")))
    public ResponseEntity<TarefaResponse> buscar(
            @Parameter(description = "Id da tarefa", example = "1")
            @PathVariable @Positive(message = "O id deve ser um número positivo") Long id) {
        return ResponseEntity.ok(TarefaResponse.de(service.buscar(id)));
    }

    @PostMapping(consumes = "application/json")
    @Operation(summary = "Cria uma tarefa", description = "Devolve a tarefa criada e o header Location com o endereço dela.")
    @ApiResponse(responseCode = "201", description = "Tarefa criada",
            headers = @Header(name = "Location", description = "Endereço da nova tarefa"))
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "DadosInvalidos", ref = "#/components/examples/DadosInvalidos")))
    @ApiResponse(responseCode = "409", description = "Título repetido em tarefa aberta",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "TarefaDuplicada", ref = "#/components/examples/TarefaDuplicada")))
    @ApiResponse(responseCode = "415", description = "Corpo não está em JSON",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "TipoDeConteudoInvalido", ref = "#/components/examples/TipoDeConteudoInvalido")))
    @ApiResponse(responseCode = "422", description = "Prazo no passado",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "RegraDeNegocio", ref = "#/components/examples/RegraDeNegocio")))
    public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody TarefaRequest dados) {
        Tarefa tarefa = service.criar(dados.paraDados());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tarefa.getId())
                .toUri();
        return ResponseEntity.created(uri).body(TarefaResponse.de(tarefa));
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    @Operation(summary = "Atualiza uma tarefa", description = "Substitui todos os dados da tarefa. Campos não enviados ficam vazios e o status volta a PENDENTE.")
    @ApiResponse(responseCode = "200", description = "Tarefa atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "DadosInvalidos", ref = "#/components/examples/DadosInvalidos")))
    @ApiResponse(responseCode = "404", description = "Tarefa não existe",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "TarefaNaoEncontrada", ref = "#/components/examples/TarefaNaoEncontrada")))
    @ApiResponse(responseCode = "409", description = "Título igual ao de outra tarefa aberta",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "TarefaDuplicada", ref = "#/components/examples/TarefaDuplicada")))
    @ApiResponse(responseCode = "422", description = "Prazo alterado para uma data que já passou",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<TarefaResponse> atualizar(
            @Parameter(description = "Id da tarefa", example = "1")
            @PathVariable @Positive(message = "O id deve ser um número positivo") Long id,
            @Valid @RequestBody TarefaRequest dados) {
        return ResponseEntity.ok(TarefaResponse.de(service.atualizar(id, dados.paraDados())));
    }

    @PatchMapping(value = "/{id}/status", consumes = "application/json")
    @Operation(summary = "Muda o status", description = "Altera só o status, sem mexer nos outros dados.")
    @ApiResponse(responseCode = "200", description = "Status alterado")
    @ApiResponse(responseCode = "400", description = "Status ausente ou inexistente",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Tarefa não existe",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "TarefaNaoEncontrada", ref = "#/components/examples/TarefaNaoEncontrada")))
    @ApiResponse(responseCode = "422", description = "A tarefa já está com esse status",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<TarefaResponse> alterarStatus(
            @Parameter(description = "Id da tarefa", example = "1")
            @PathVariable @Positive(message = "O id deve ser um número positivo") Long id,
            @Valid @RequestBody StatusRequest dados) {
        return ResponseEntity.ok(TarefaResponse.de(service.alterarStatus(id, dados.status())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma tarefa")
    @ApiResponse(responseCode = "204", description = "Tarefa excluída")
    @ApiResponse(responseCode = "404", description = "Tarefa não existe",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ErroResponse.class),
                    examples = @ExampleObject(name = "TarefaNaoEncontrada", ref = "#/components/examples/TarefaNaoEncontrada")))
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Id da tarefa", example = "1")
            @PathVariable @Positive(message = "O id deve ser um número positivo") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
