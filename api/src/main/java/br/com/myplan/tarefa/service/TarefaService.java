package br.com.myplan.tarefa.service;

import br.com.myplan.shared.exception.ParametroInvalidoException;
import br.com.myplan.shared.exception.RegraNegocioException;
import br.com.myplan.tarefa.domain.DadosTarefa;
import br.com.myplan.tarefa.domain.ResumoTarefas;
import br.com.myplan.tarefa.domain.StatusTarefa;
import br.com.myplan.tarefa.domain.Tarefa;
import br.com.myplan.tarefa.exception.TarefaDuplicadaException;
import br.com.myplan.tarefa.exception.TarefaNaoEncontradaException;
import br.com.myplan.tarefa.repository.TarefaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class TarefaService {

    private static final Logger log = LoggerFactory.getLogger(TarefaService.class);

    // só esses campos podem ser usados na ordenação
    private static final Set<String> CAMPOS_ORDENACAO = Set.of("titulo", "prazo", "status", "dataCriacao");

    private final TarefaRepository repository;

    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    public List<Tarefa> listarTodas(StatusTarefa status) {
        Sort sort = Sort.by("id");
        if (status == null) {
            return repository.findAll(sort);
        }
        return repository.findByStatus(status, sort);
    }

    public Page<Tarefa> listar(StatusTarefa status, String busca, int pagina, int tamanho,
                               String ordenarPor, String direcao) {
        Pageable pageable = PageRequest.of(pagina, tamanho, ordenacao(ordenarPor, direcao));
        String termo = busca == null ? "" : busca.trim();

        if (status == null) {
            return repository.findByTituloContainingIgnoreCase(termo, pageable);
        }
        return repository.findByStatusAndTituloContainingIgnoreCase(status, termo, pageable);
    }

    public Tarefa buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    public ResumoTarefas resumo() {
        return new ResumoTarefas(
                repository.count(),
                repository.countByStatus(StatusTarefa.PENDENTE),
                repository.countByStatus(StatusTarefa.EM_ANDAMENTO),
                repository.countByStatus(StatusTarefa.CONCLUIDA),
                repository.countByStatusNotAndPrazoBefore(StatusTarefa.CONCLUIDA, LocalDate.now()));
    }

    @Transactional
    public Tarefa criar(DadosTarefa dados) {
        String titulo = dados.titulo().trim();

        if (dados.prazo() != null && dados.prazo().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("O prazo " + dados.prazo() + " já passou. Informe a data de hoje ou uma data futura");
        }
        if (repository.existsByTituloIgnoreCaseAndStatusNot(titulo, StatusTarefa.CONCLUIDA)) {
            throw new TarefaDuplicadaException(titulo);
        }

        Tarefa tarefa = new Tarefa();
        preencher(tarefa, dados);
        Tarefa salva = repository.save(tarefa);
        log.info("Tarefa {} criada", salva.getId());
        return salva;
    }

    @Transactional
    public Tarefa atualizar(Long id, DadosTarefa dados) {
        Tarefa tarefa = buscar(id);
        String titulo = dados.titulo().trim();

        boolean prazoMudou = !Objects.equals(tarefa.getPrazo(), dados.prazo());
        if (prazoMudou && dados.prazo() != null && dados.prazo().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("O prazo não pode ser alterado para " + dados.prazo()
                    + ", que já passou. Informe a data de hoje ou uma data futura");
        }
        if (repository.existsByTituloIgnoreCaseAndStatusNotAndIdNot(titulo, StatusTarefa.CONCLUIDA, id)) {
            throw new TarefaDuplicadaException(titulo);
        }

        preencher(tarefa, dados);
        Tarefa salva = repository.saveAndFlush(tarefa);
        log.info("Tarefa {} atualizada", id);
        return salva;
    }

    @Transactional
    public Tarefa alterarStatus(Long id, StatusTarefa novoStatus) {
        Tarefa tarefa = buscar(id);
        if (tarefa.getStatus() == novoStatus) {
            throw new RegraNegocioException("A tarefa " + id + " já está com o status " + novoStatus);
        }
        tarefa.setStatus(novoStatus);
        Tarefa salva = repository.saveAndFlush(tarefa);
        log.info("Tarefa {} mudou para {}", id, novoStatus);
        return salva;
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new TarefaNaoEncontradaException(id);
        }
        repository.deleteById(id);
        log.info("Tarefa {} excluída", id);
    }

    private Sort ordenacao(String ordenarPor, String direcao) {
        if (!CAMPOS_ORDENACAO.contains(ordenarPor)) {
            throw new ParametroInvalidoException(
                    "Não é possível ordenar por '" + ordenarPor + "'. Use: titulo, prazo, status ou dataCriacao");
        }
        if (!"asc".equalsIgnoreCase(direcao) && !"desc".equalsIgnoreCase(direcao)) {
            throw new ParametroInvalidoException("A direção '" + direcao + "' não existe. Use 'asc' ou 'desc'");
        }
        return Sort.by(Sort.Direction.fromString(direcao), ordenarPor);
    }

    private void preencher(Tarefa tarefa, DadosTarefa dados) {
        tarefa.setTitulo(dados.titulo().trim());
        tarefa.setDescricao(dados.descricao() == null ? null : dados.descricao().trim());
        tarefa.setStatus(dados.status() != null ? dados.status() : StatusTarefa.PENDENTE);
        tarefa.setPrazo(dados.prazo());
    }
}
