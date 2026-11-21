package br.com.myplan.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    private static final String CATALOGO =
            "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#";

    @Bean
    public OpenAPI myplanOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("myplan API")
                        .version("3.0.0")
                        .contact(new Contact().name("Monique Coelho"))
                        .description("""
                                Sua vida. Seus planos. Sempre em foco.

                                API de tarefas pessoais do myplan.

                                **Versões**
                                - **v2** (atual): `/api/v2/tarefas`
                                - **v1** (depreciada, funciona até 30/06/2027): `/api/v1/tarefas`

                                Escolha a versão no seletor "Select a definition", no topo da página.

                                **Início rápido**
                                1. Crie uma tarefa: `POST /api/v2/tarefas` com `{"titulo": "Pagar conta de luz"}`
                                2. Liste: `GET /api/v2/tarefas`
                                3. Conclua: `PATCH /api/v2/tarefas/1/status` com `{"status": "CONCLUIDA"}`

                                **Erros** seguem o formato Problem Details (RFC 9457), com `codigo` e `dica` de como corrigir.
                                """))
                .externalDocs(new ExternalDocumentation()
                        .description("README e catálogo de erros no GitHub")
                        .url("https://github.com/developermjautomacoes-jpg/mobile_myplan/tree/main/api"))
                .components(new Components().examples(exemplosDeErro()));
    }

    @Bean
    public GroupedOpenApi apiV2() {
        return GroupedOpenApi.builder()
                .group("v2")
                .displayName("Tarefas v2 (atual)")
                .pathsToMatch("/api/v2/**", "/api")
                .build();
    }

    @Bean
    public GroupedOpenApi apiV1() {
        return GroupedOpenApi.builder()
                .group("v1")
                .displayName("Tarefas v1 (depreciada)")
                .pathsToMatch("/api/v1/**", "/api")
                .build();
    }

    private Map<String, Example> exemplosDeErro() {
        Map<String, Example> exemplos = new LinkedHashMap<>();

        exemplos.put("TarefaNaoEncontrada", exemplo("Id que não existe", mapa(
                "type", CATALOGO + "tarefa-nao-encontrada",
                "title", "Tarefa não encontrada",
                "status", 404,
                "detail", "Não existe tarefa com o id 99",
                "instance", "/api/v2/tarefas/99",
                "codigo", "TAREFA_NAO_ENCONTRADA",
                "dica", "Confira o id. Para ver os ids existentes, liste as tarefas em GET /api/v2/tarefas.",
                "requestId", "3f2a9c1e-7b4d-4e8a-9f10-2c6d8e5a1b7f")));

        exemplos.put("DadosInvalidos", exemplo("Campos fora das regras", mapa(
                "type", CATALOGO + "dados-invalidos",
                "title", "Dados inválidos",
                "status", 400,
                "detail", "2 campos estão inválidos",
                "instance", "/api/v2/tarefas",
                "codigo", "DADOS_INVALIDOS",
                "dica", "Corrija os campos listados em 'campos' e envie de novo.",
                "campos", mapa(
                        "titulo", "O título deve ter entre 3 e 100 caracteres",
                        "descricao", "A descrição não pode conter os caracteres < ou >"))));

        exemplos.put("TarefaDuplicada", exemplo("Título repetido", mapa(
                "type", CATALOGO + "tarefa-duplicada",
                "title", "Tarefa duplicada",
                "status", 409,
                "detail", "Já existe uma tarefa em aberto com o título 'Pagar conta de luz'",
                "instance", "/api/v2/tarefas",
                "codigo", "TAREFA_DUPLICADA",
                "dica", "Use outro título ou conclua a tarefa que já existe com esse nome.")));

        exemplos.put("RegraDeNegocio", exemplo("Prazo no passado", mapa(
                "type", CATALOGO + "regra-de-negocio",
                "title", "Operação não permitida",
                "status", 422,
                "detail", "O prazo 2026-11-19 já passou. Informe a data de hoje ou uma data futura",
                "instance", "/api/v2/tarefas",
                "codigo", "REGRA_DE_NEGOCIO",
                "dica", "Os dados estão no formato certo, mas violam uma regra do myplan. Leia o 'detalhe'.")));

        exemplos.put("ParametroInvalido", exemplo("Tamanho de página acima do limite", mapa(
                "type", CATALOGO + "parametro-invalido",
                "title", "Parâmetro inválido",
                "status", 400,
                "detail", "Um ou mais parâmetros da URL estão inválidos",
                "instance", "/api/v2/tarefas",
                "codigo", "PARAMETRO_INVALIDO",
                "dica", "Confira os parâmetros da URL na documentação do endpoint.",
                "campos", mapa("tamanho", "O tamanho máximo é 50"))));

        exemplos.put("TipoDeConteudoInvalido", exemplo("Corpo enviado como texto", mapa(
                "type", CATALOGO + "tipo-de-conteudo-invalido",
                "title", "Tipo de conteúdo não suportado",
                "status", 415,
                "detail", "O Content-Type recebido foi 'text/plain', mas este endpoint só aceita application/json",
                "instance", "/api/v2/tarefas",
                "codigo", "TIPO_DE_CONTEUDO_INVALIDO",
                "dica", "Envie o corpo em JSON com o header 'Content-Type: application/json'.")));

        return exemplos;
    }

    private Map<String, Object> mapa(Object... chavesEValores) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (int i = 0; i < chavesEValores.length; i += 2) {
            mapa.put((String) chavesEValores[i], chavesEValores[i + 1]);
        }
        return mapa;
    }

    private Example exemplo(String resumo, Object valor) {
        return new Example().summary(resumo).value(valor);
    }
}
