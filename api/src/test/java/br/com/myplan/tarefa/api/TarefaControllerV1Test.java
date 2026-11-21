package br.com.myplan.tarefa.api;

import br.com.myplan.tarefa.repository.TarefaRepository;
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
class TarefaControllerV1Test {

    private static final String URL = "/api/v1/tarefas";

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
        String resposta = mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(resposta).get("id").asLong();
    }

    @Test
    void listaContinuaSendoUmArraySimples() throws Exception {
        novaTarefa("{\"titulo\":\"Pagar conta de luz\"}");
        novaTarefa("{\"titulo\":\"Ir ao mercado\"}");

        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo").value("Pagar conta de luz"))
                .andExpect(jsonPath("$[0].atrasada").doesNotExist());
    }

    @Test
    void filtroPorStatus() throws Exception {
        novaTarefa("{\"titulo\":\"Pagar conta de luz\"}");
        novaTarefa("{\"titulo\":\"Renovar CNH\",\"status\":\"CONCLUIDA\"}");

        mvc.perform(get(URL).param("status", "CONCLUIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void crudCompleto() throws Exception {
        long id = novaTarefa("{\"titulo\":\"Ir ao mercado\"}");

        mvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        mvc.perform(put(URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Ir ao mercado\",\"status\":\"CONCLUIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        mvc.perform(delete(URL + "/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarDevolveLocationNaV1() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"Marcar dentista\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/v1/tarefas/\\d+")));
    }

    @Test
    void respostasDaV1AvisamQueEstaDepreciada() throws Exception {
        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(header().string("API-Version", "1"))
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().exists("Sunset"))
                .andExpect(header().string("Link", "</api/v2/tarefas>; rel=\"successor-version\""));
    }

    @Test
    void validacaoTambemFuncionaNaV1() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"descricao\":\"sem título\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos.titulo").value("O título é obrigatório"));
    }

    @Test
    void patchDeStatusNaoExisteNaV1() throws Exception {
        long id = novaTarefa("{\"titulo\":\"Renovar CNH\"}");

        mvc.perform(patch(URL + "/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONCLUIDA\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ROTA_NAO_ENCONTRADA"));
    }
}
