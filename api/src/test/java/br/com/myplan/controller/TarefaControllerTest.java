package br.com.myplan.controller;

import br.com.myplan.repository.TarefaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TarefaControllerTest {

    private static final String AMANHA = LocalDate.now().plusDays(1).toString();
    private static final String ONTEM = LocalDate.now().minusDays(1).toString();

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
        return mapper.readTree(resposta).get("id").asLong();
    }

    @Nested
    class Listar {

        @Test
        void semTarefas() throws Exception {
            mvc.perform(get("/api/tarefas"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-Total-Count", "0"))
                    .andExpect(jsonPath("$.conteudo", hasSize(0)))
                    .andExpect(jsonPath("$.totalElementos").value(0));
        }

        @Test
        void comPaginacao() throws Exception {
            for (int i = 1; i <= 7; i++) {
                novaTarefa("{\"titulo\":\"Tarefa " + i + "\"}");
            }

            mvc.perform(get("/api/tarefas").param("pagina", "1").param("tamanho", "5"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-Total-Count", "7"))
                    .andExpect(jsonPath("$.conteudo", hasSize(2)))
                    .andExpect(jsonPath("$.pagina").value(1))
                    .andExpect(jsonPath("$.totalPaginas").value(2))
                    .andExpect(jsonPath("$.ultima").value(true));
        }

        @Test
        void filtrandoPorStatusEBusca() throws Exception {
            novaTarefa("{\"titulo\":\"Pagar conta de luz\"}");
            novaTarefa("{\"titulo\":\"Pagar internet\",\"status\":\"CONCLUIDA\"}");
            novaTarefa("{\"titulo\":\"Ir ao mercado\",\"status\":\"CONCLUIDA\"}");

            mvc.perform(get("/api/tarefas").param("status", "CONCLUIDA").param("busca", "pagar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.conteudo", hasSize(1)))
                    .andExpect(jsonPath("$.conteudo[0].titulo").value("Pagar internet"));
        }

        @Test
        void ordenandoPorTitulo() throws Exception {
            novaTarefa("{\"titulo\":\"Comprar pão\"}");
            novaTarefa("{\"titulo\":\"Arrumar a casa\"}");

            mvc.perform(get("/api/tarefas").param("ordenarPor", "titulo").param("direcao", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.conteudo[0].titulo").value("Arrumar a casa"));
        }

        @Test
        void tamanhoAcimaDoLimite() throws Exception {
            mvc.perform(get("/api/tarefas").param("tamanho", "100"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"))
                    .andExpect(jsonPath("$.campos.tamanho").value("O tamanho máximo é 50"));
        }

        @Test
        void ordenacaoPorCampoNaoPermitido() throws Exception {
            mvc.perform(get("/api/tarefas").param("ordenarPor", "senha"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value(containsString("senha")));
        }

        @Test
        void statusInexistenteNoFiltro() throws Exception {
            mvc.perform(get("/api/tarefas").param("status", "FEITA"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value(containsString("Valores aceitos")));
        }
    }

    @Nested
    class Buscar {

        @Test
        void porId() throws Exception {
            long id = novaTarefa("{\"titulo\":\"Marcar dentista\"}");

            mvc.perform(get("/api/tarefas/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.titulo").value("Marcar dentista"))
                    .andExpect(jsonPath("$.atrasada").value(false));
        }

        @Test
        void idInexistente() throws Exception {
            mvc.perform(get("/api/tarefas/{id}", 999))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("TAREFA_NAO_ENCONTRADA"));
        }

        @Test
        void idNegativo() throws Exception {
            mvc.perform(get("/api/tarefas/{id}", -1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.id").value("O id deve ser positivo"));
        }

        @Test
        void idQueNaoEhNumero() throws Exception {
            mvc.perform(get("/api/tarefas/abc"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void rotaQueNaoExiste() throws Exception {
            mvc.perform(get("/api/tarefass"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("ROTA_NAO_ENCONTRADA"));
        }
    }

    @Nested
    class Criar {

        @Test
        void comSucesso() throws Exception {
            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"  Pagar conta de luz  \",\"prazo\":\"" + AMANHA + "\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", matchesPattern(".*/api/tarefas/\\d+")))
                    .andExpect(header().exists("X-Request-Id"))
                    .andExpect(jsonPath("$.titulo").value("Pagar conta de luz"))
                    .andExpect(jsonPath("$.status").value("PENDENTE"));
        }

        @Test
        void semTitulo() throws Exception {
            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"descricao\":\"sem título\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.titulo").value("O título é obrigatório"));
        }

        @Test
        void tituloCurto() throws Exception {
            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"ab\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.titulo").value("O título deve ter entre 3 e 100 caracteres"));
        }

        @Test
        void comTagHtml() throws Exception {
            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"<script>alert(1)</script>\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.titulo").value(containsString("< ou >")));
        }

        @Test
        void comPrazoNoPassado() throws Exception {
            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"Pagar boleto\",\"prazo\":\"" + ONTEM + "\"}"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"));
        }

        @Test
        void tituloRepetido() throws Exception {
            novaTarefa("{\"titulo\":\"Ir ao mercado\"}");

            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"ir ao MERCADO\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigo").value("TAREFA_DUPLICADA"));
        }

        @Test
        void tituloRepetidoDeTarefaConcluidaPode() throws Exception {
            novaTarefa("{\"titulo\":\"Ir ao mercado\",\"status\":\"CONCLUIDA\"}");
            novaTarefa("{\"titulo\":\"Ir ao mercado\"}");
        }

        @Test
        void comCampoNaoPermitido() throws Exception {
            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\":50,\"titulo\":\"Teste\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value("O campo 'id' não é aceito"));
        }

        @Test
        void comStatusInvalido() throws Exception {
            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"Teste\",\"status\":\"FEITA\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value(containsString("Valores aceitos")));
        }

        @Test
        void comDataMalFormatada() throws Exception {
            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"Teste\",\"prazo\":\"10/12/2026\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value(containsString("aaaa-mm-dd")));
        }

        @Test
        void comJsonQuebrado() throws Exception {
            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\": "))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("CORPO_INVALIDO"));
        }

        @Test
        void semJson() throws Exception {
            mvc.perform(post("/api/tarefas")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("Pagar conta"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    class Atualizar {

        @Test
        void comSucesso() throws Exception {
            long id = novaTarefa("{\"titulo\":\"Ir ao mercado\"}");

            mvc.perform(put("/api/tarefas/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"Ir ao mercado\",\"descricao\":\"Comprar frutas\",\"status\":\"EM_ANDAMENTO\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                    .andExpect(jsonPath("$.descricao").value("Comprar frutas"))
                    .andExpect(jsonPath("$.dataAtualizacao").isNotEmpty());
        }

        @Test
        void idInexistente() throws Exception {
            mvc.perform(put("/api/tarefas/{id}", 999)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"Qualquer\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void tituloVazio() throws Exception {
            long id = novaTarefa("{\"titulo\":\"Ir ao mercado\"}");

            mvc.perform(put("/api/tarefas/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void prazoParaOPassado() throws Exception {
            long id = novaTarefa("{\"titulo\":\"Ir ao mercado\"}");

            mvc.perform(put("/api/tarefas/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"Ir ao mercado\",\"prazo\":\"" + ONTEM + "\"}"))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void comTituloDeOutraTarefa() throws Exception {
            novaTarefa("{\"titulo\":\"Pagar conta de luz\"}");
            long id = novaTarefa("{\"titulo\":\"Ir ao mercado\"}");

            mvc.perform(put("/api/tarefas/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":\"Pagar conta de luz\"}"))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class AlterarStatus {

        @Test
        void comSucesso() throws Exception {
            long id = novaTarefa("{\"titulo\":\"Renovar CNH\"}");

            mvc.perform(patch("/api/tarefas/{id}/status", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"CONCLUIDA\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONCLUIDA"));
        }

        @Test
        void mesmoStatus() throws Exception {
            long id = novaTarefa("{\"titulo\":\"Renovar CNH\"}");

            mvc.perform(patch("/api/tarefas/{id}/status", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"PENDENTE\"}"))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void semStatus() throws Exception {
            long id = novaTarefa("{\"titulo\":\"Renovar CNH\"}");

            mvc.perform(patch("/api/tarefas/{id}/status", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.status").value("O status é obrigatório"));
        }

        @Test
        void idInexistente() throws Exception {
            mvc.perform(patch("/api/tarefas/{id}/status", 999)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"CONCLUIDA\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Excluir {

        @Test
        void comSucesso() throws Exception {
            long id = novaTarefa("{\"titulo\":\"Renovar CNH\"}");

            mvc.perform(delete("/api/tarefas/{id}", id))
                    .andExpect(status().isNoContent());

            mvc.perform(get("/api/tarefas/{id}", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        void idInexistente() throws Exception {
            mvc.perform(delete("/api/tarefas/{id}", 999))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void resumo() throws Exception {
        novaTarefa("{\"titulo\":\"Pagar conta de luz\"}");
        novaTarefa("{\"titulo\":\"Ir ao mercado\",\"status\":\"EM_ANDAMENTO\"}");
        novaTarefa("{\"titulo\":\"Renovar CNH\",\"status\":\"CONCLUIDA\"}");

        mvc.perform(get("/api/tarefas/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.pendentes").value(1))
                .andExpect(jsonPath("$.emAndamento").value(1))
                .andExpect(jsonPath("$.concluidas").value(1));
    }

    @Test
    void metodoNaoSuportado() throws Exception {
        mvc.perform(patch("/api/tarefas"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void healthDoActuator() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
