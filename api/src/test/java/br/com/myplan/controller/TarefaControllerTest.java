package br.com.myplan.controller;

import br.com.myplan.repository.TarefaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TarefaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TarefaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private long novaTarefa(String json) throws Exception {
        String resposta = mvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(resposta);
        return node.get("id").asLong();
    }

    @Test
    void listarSemTarefas() throws Exception {
        mvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listarTodas() throws Exception {
        novaTarefa("{\"titulo\":\"Pagar conta de luz\"}");
        novaTarefa("{\"titulo\":\"Ir ao mercado\"}");

        mvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listarPorStatus() throws Exception {
        novaTarefa("{\"titulo\":\"Pagar conta de luz\"}");
        novaTarefa("{\"titulo\":\"Renovar CNH\",\"status\":\"CONCLUIDA\"}");

        mvc.perform(get("/api/tarefas").param("status", "CONCLUIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Renovar CNH"));
    }

    @Test
    void buscarPorId() throws Exception {
        long id = novaTarefa("{\"titulo\":\"Marcar dentista\"}");

        mvc.perform(get("/api/tarefas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Marcar dentista"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void buscarIdInexistente() throws Exception {
        mvc.perform(get("/api/tarefas/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarIdInvalido() throws Exception {
        mvc.perform(get("/api/tarefas/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarTarefa() throws Exception {
        mvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Pagar conta de luz\",\"prazo\":\"2026-12-10\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/tarefas/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.prazo").value("2026-12-10"));
    }

    @Test
    void criarSemTitulo() throws Exception {
        mvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"sem título\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").value("O título é obrigatório"));
    }

    @Test
    void criarComStatusInvalido() throws Exception {
        mvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Teste\",\"status\":\"FEITA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarComJsonQuebrado() throws Exception {
        mvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarTarefa() throws Exception {
        long id = novaTarefa("{\"titulo\":\"Ir ao mercado\"}");

        mvc.perform(put("/api/tarefas/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Ir ao mercado\",\"status\":\"CONCLUIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.dataAtualizacao").isNotEmpty());
    }

    @Test
    void atualizarIdInexistente() throws Exception {
        mvc.perform(put("/api/tarefas/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Qualquer\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarComTituloVazio() throws Exception {
        long id = novaTarefa("{\"titulo\":\"Ir ao mercado\"}");

        mvc.perform(put("/api/tarefas/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void excluirTarefa() throws Exception {
        long id = novaTarefa("{\"titulo\":\"Renovar CNH\"}");

        mvc.perform(delete("/api/tarefas/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/tarefas/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirIdInexistente() throws Exception {
        mvc.perform(delete("/api/tarefas/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
