package br.com.myplan.service;

import br.com.myplan.dto.TarefaRequest;
import br.com.myplan.exception.TarefaNaoEncontradaException;
import br.com.myplan.model.StatusTarefa;
import br.com.myplan.model.Tarefa;
import br.com.myplan.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository repository;

    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    public List<Tarefa> listar(StatusTarefa status) {
        if (status == null) {
            return repository.findAll();
        }
        return repository.findByStatus(status);
    }

    public Tarefa buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    @Transactional
    public Tarefa criar(TarefaRequest dados) {
        Tarefa tarefa = new Tarefa();
        preencher(tarefa, dados);
        return repository.save(tarefa);
    }

    @Transactional
    public Tarefa atualizar(Long id, TarefaRequest dados) {
        Tarefa tarefa = buscar(id);
        preencher(tarefa, dados);
        return repository.saveAndFlush(tarefa);
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new TarefaNaoEncontradaException(id);
        }
        repository.deleteById(id);
    }

    private void preencher(Tarefa tarefa, TarefaRequest dados) {
        tarefa.setTitulo(dados.titulo().trim());
        tarefa.setDescricao(dados.descricao());
        tarefa.setStatus(dados.status() != null ? dados.status() : StatusTarefa.PENDENTE);
        tarefa.setPrazo(dados.prazo());
    }
}
