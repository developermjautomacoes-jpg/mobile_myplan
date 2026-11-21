package br.com.myplan.tarefa.api.v2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Schema(description = "Página de resultados com os dados da paginação")
public record PaginaResponse<T>(
        List<T> conteudo,
        @Schema(description = "Número da página, começando em 0", example = "0") int pagina,
        @Schema(description = "Itens por página", example = "10") int tamanho,
        @Schema(description = "Total de tarefas encontradas", example = "7") long totalElementos,
        @Schema(example = "1") int totalPaginas,
        boolean primeira,
        boolean ultima
) {

    public static <E, T> PaginaResponse<T> de(Page<E> page, Function<E, T> conversor) {
        return new PaginaResponse<>(
                page.getContent().stream().map(conversor).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
