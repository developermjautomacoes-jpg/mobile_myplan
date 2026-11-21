package br.com.myplan.tarefa.repository;

import br.com.myplan.tarefa.domain.StatusTarefa;
import br.com.myplan.tarefa.domain.Tarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findByStatus(StatusTarefa status, Sort sort);

    Page<Tarefa> findByTituloContainingIgnoreCase(String busca, Pageable pageable);

    Page<Tarefa> findByStatusAndTituloContainingIgnoreCase(StatusTarefa status, String busca, Pageable pageable);

    long countByStatus(StatusTarefa status);

    long countByStatusNotAndPrazoBefore(StatusTarefa status, LocalDate data);

    boolean existsByTituloIgnoreCaseAndStatusNot(String titulo, StatusTarefa status);

    boolean existsByTituloIgnoreCaseAndStatusNotAndIdNot(String titulo, StatusTarefa status, Long id);
}
