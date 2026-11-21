package br.com.myplan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myplanOpenApi() {
        return new OpenAPI().info(new Info()
                .title("myplan API")
                .version("2.0.0")
                .description("Sua vida. Seus planos. Sempre em foco.\n\n"
                        + "API de tarefas pessoais: criar, listar, filtrar, atualizar, mudar o status e excluir tarefas."));
    }
}
