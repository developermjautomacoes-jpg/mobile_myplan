package br.com.myplan.shared;

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
class VersionamentoEErrosTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void listaAsVersoes() throws Exception {
        mvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].versao").value("v1"))
                .andExpect(jsonPath("$[0].situacao").value("DEPRECIADA"))
                .andExpect(jsonPath("$[1].versao").value("v2"))
                .andExpect(jsonPath("$[1].situacao").value("ATUAL"));
    }

    @Test
    void v2InformaAVersao() throws Exception {
        mvc.perform(get("/api/v2/tarefas"))
                .andExpect(status().isOk())
                .andExpect(header().string("API-Version", "2"))
                .andExpect(header().doesNotExist("Deprecation"));
    }

    @Test
    void enderecoSemVersaoContinuaFuncionandoEApontaParaAV2() throws Exception {
        mvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").isArray())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("Link", "</api/v2/tarefas>; rel=\"successor-version\""));
    }

    @Test
    void versaoQueNaoExiste() throws Exception {
        mvc.perform(get("/api/v3/tarefas"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("VERSAO_NAO_SUPORTADA"))
                .andExpect(jsonPath("$.detail").value("A versão v3 não existe"))
                .andExpect(jsonPath("$.dica").value(containsString("v1 ou v2")));
    }

    @Test
    void erroSegueOFormatoProblemDetails() throws Exception {
        mvc.perform(get("/api/v2/tarefas/999").header("X-Request-Id", "teste-123"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(endsWith("erros.md#tarefa-nao-encontrada")))
                .andExpect(jsonPath("$.title").value("Tarefa não encontrada"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Não existe tarefa com o id 999"))
                .andExpect(jsonPath("$.instance").value("/api/v2/tarefas/999"))
                .andExpect(jsonPath("$.codigo").value("TAREFA_NAO_ENCONTRADA"))
                .andExpect(jsonPath("$.dica").isNotEmpty())
                .andExpect(jsonPath("$.requestId").value("teste-123"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void metodoNaoSuportadoInformaOsMetodosAceitos() throws Exception {
        mvc.perform(patch("/api/v2/tarefas"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.detail").value(containsString("Métodos aceitos")));
    }

    @Test
    void contentTypeErradoExplicaOQueFoiRecebido() throws Exception {
        mvc.perform(post("/api/v2/tarefas").contentType(MediaType.TEXT_PLAIN).content("Pagar conta"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.detail").value(containsString("text/plain")));
    }

    @Test
    void documentacaoDasDuasVersoes() throws Exception {
        mvc.perform(get("/v3/api-docs/v2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("myplan API"))
                .andExpect(jsonPath("$.paths['/api/v2/tarefas']").exists())
                .andExpect(jsonPath("$.components.examples.TarefaNaoEncontrada").exists());

        mvc.perform(get("/v3/api-docs/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/tarefas'].get.deprecated").value(true));
    }
}
