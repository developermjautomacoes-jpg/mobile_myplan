![myplan](docs/img/myplan-banner.png)

# myplan API

**Sua vida. Seus planos. Sempre em foco.**

Projeto autoral da disciplina Serviços Web e API Rest.

O myplan é um app de tarefas pessoais. Esta é a API dele, feita com Spring Boot: permite criar, listar, filtrar, atualizar, mudar o status e excluir tarefas.

## Entregas

| Entrega | O que foi feito |
|---|---|
| 1 | Projeto Spring Boot e CRUD de tarefas com GET, POST, PUT e DELETE |
| 2 | Validações e regras de negócio, paginação, filtros e ordenação, PATCH de status, resumo, respostas de erro detalhadas, logs e monitoramento com Actuator |

## Tecnologias

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Boot Actuator
- H2 (banco em memória)
- Springdoc OpenAPI (Swagger)

## Como rodar

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`.

A API sobe em `http://localhost:8080`. Nesse endereço tem uma página inicial do myplan com os endpoints e os links para a documentação.

- Swagger: `http://localhost:8080/swagger-ui.html`
- Saúde da aplicação: `http://localhost:8080/actuator/health`
- Console do H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:myplandb`, usuário `sa`, sem senha)

Como o banco é em memória, os dados são apagados quando a aplicação reinicia.

## Endpoints

URL base: `/api/tarefas`

| Método | Endpoint | Descrição | Sucesso |
|---|---|---|---|
| GET | `/api/tarefas` | Lista as tarefas com filtros, paginação e ordenação | 200 |
| GET | `/api/tarefas/resumo` | Quantidade de tarefas por status | 200 |
| GET | `/api/tarefas/{id}` | Busca uma tarefa | 200 |
| POST | `/api/tarefas` | Cria uma tarefa | 201 |
| PUT | `/api/tarefas/{id}` | Atualiza todos os dados de uma tarefa | 200 |
| PATCH | `/api/tarefas/{id}/status` | Muda só o status | 200 |
| DELETE | `/api/tarefas/{id}` | Exclui uma tarefa | 204 |

### Campos da tarefa

| Campo | Regras |
|---|---|
| `titulo` | Obrigatório, de 3 a 100 caracteres, sem `<` ou `>`. Não pode repetir o título de outra tarefa que ainda não foi concluída |
| `descricao` | Opcional, até 500 caracteres, sem `<` ou `>` |
| `status` | Opcional: `PENDENTE`, `EM_ANDAMENTO` ou `CONCLUIDA`. Se não for informado, fica `PENDENTE` |
| `prazo` | Opcional, formato `aaaa-mm-dd`. Não pode ser uma data que já passou |

A API preenche `id`, `dataCriacao` e `dataAtualizacao`, e calcula `atrasada` (prazo vencido e tarefa não concluída). Se o corpo trouxer um campo que não existe, como `id`, a requisição é recusada.

### GET /api/tarefas

| Parâmetro | Padrão | Descrição |
|---|---|---|
| `status` | - | Filtra pelo status |
| `busca` | - | Procura no título (sem diferenciar maiúsculas) |
| `pagina` | `0` | Número da página, começando em 0 |
| `tamanho` | `10` | Itens por página, de 1 a 50 |
| `ordenarPor` | `dataCriacao` | `titulo`, `prazo`, `status` ou `dataCriacao` |
| `direcao` | `desc` | `asc` ou `desc` |

Exemplo: `/api/tarefas?status=PENDENTE&busca=pagar&ordenarPor=prazo&direcao=asc`

A resposta traz a lista e os dados da paginação. O header `X-Total-Count` informa o total de tarefas encontradas.

```json
{
  "conteudo": [
    {
      "id": 1,
      "titulo": "Pagar conta de luz",
      "descricao": "Vence no dia 10",
      "status": "PENDENTE",
      "prazo": "2026-11-25",
      "atrasada": false,
      "dataCriacao": "2026-11-20T22:10:32.6",
      "dataAtualizacao": null
    }
  ],
  "pagina": 0,
  "tamanho": 10,
  "totalElementos": 1,
  "totalPaginas": 1,
  "primeira": true,
  "ultima": true
}
```

- 200: lista (pode vir vazia)
- 400: status inexistente, página negativa, tamanho fora de 1 a 50 ou ordenação por campo não permitido

### GET /api/tarefas/resumo

```json
{ "total": 5, "pendentes": 3, "emAndamento": 1, "concluidas": 1, "atrasadas": 0 }
```

- 200: resumo

### GET /api/tarefas/{id}

- 200: tarefa encontrada
- 400: id negativo ou que não é número
- 404: tarefa não existe

### POST /api/tarefas

```json
{
  "titulo": "Pagar conta de luz",
  "descricao": "Vence no dia 10",
  "prazo": "2026-12-10"
}
```

- 201: tarefa criada. O header `Location` traz o endereço dela
- 400: campos inválidos, campo não permitido, status inexistente, data fora do formato ou JSON mal formado
- 409: já existe uma tarefa aberta com esse título
- 415: corpo que não é JSON
- 422: prazo no passado

### PUT /api/tarefas/{id}

Envia a tarefa completa com os novos valores.

```json
{
  "titulo": "Ir ao mercado",
  "descricao": "Comprar frutas",
  "status": "EM_ANDAMENTO",
  "prazo": "2026-12-10"
}
```

- 200: tarefa atualizada
- 400: dados inválidos
- 404: tarefa não existe
- 409: título igual ao de outra tarefa aberta
- 422: prazo alterado para uma data que já passou

### PATCH /api/tarefas/{id}/status

```json
{ "status": "CONCLUIDA" }
```

- 200: status alterado
- 400: status ausente ou inexistente
- 404: tarefa não existe
- 422: a tarefa já está com esse status

### DELETE /api/tarefas/{id}

- 204: tarefa excluída
- 404: tarefa não existe

### Respostas de erro

Todos os erros seguem o mesmo formato. O campo `codigo` permite que o app trate cada caso, e `campos` aparece quando o problema é em campos específicos.

```json
{
  "timestamp": "2026-11-20T22:10:32.735",
  "status": 400,
  "erro": "Bad Request",
  "codigo": "DADOS_INVALIDOS",
  "mensagem": "Um ou mais campos estão inválidos",
  "caminho": "/api/tarefas",
  "campos": {
    "titulo": "O título deve ter entre 3 e 100 caracteres"
  }
}
```

| Status | Código | Quando acontece |
|---|---|---|
| 400 | `DADOS_INVALIDOS` | Campos do corpo fora das regras |
| 400 | `CORPO_INVALIDO` | JSON mal formado, campo não permitido, valor de status ou data inválido |
| 400 | `PARAMETRO_INVALIDO` | Parâmetro da URL inválido (id, página, tamanho, ordenação) |
| 404 | `TAREFA_NAO_ENCONTRADA` | Id que não existe |
| 404 | `ROTA_NAO_ENCONTRADA` | Endereço que não existe na API |
| 405 | `METODO_NAO_SUPORTADO` | Método HTTP não aceito no endereço |
| 409 | `TAREFA_DUPLICADA` | Título repetido em tarefa aberta |
| 415 | `TIPO_DE_CONTEUDO_INVALIDO` | Corpo enviado sem ser JSON |
| 422 | `REGRA_DE_NEGOCIO` | Prazo no passado ou status repetido |
| 500 | `ERRO_INTERNO` | Erro inesperado. O detalhe fica só no log, nunca vai para o cliente |

### Headers de resposta

- `X-Request-Id`: identificador da requisição, também gravado no log. Se o cliente enviar um, a API devolve o mesmo
- `X-Total-Count`: total de itens na listagem
- `Location`: endereço da tarefa criada no POST

## Monitoramento

- `GET /actuator/health`: indica se a API e o banco estão funcionando
- `GET /actuator/info`: nome e versão da API

Cada requisição em `/api` gera uma linha de log com o id, o método, o endereço, o status e o tempo de resposta:

```
[teste-001] POST /api/tarefas -> 201 (268 ms)
```

## Testes

Testei os endpoints com o Postman. A coleção está em `docs/myplan-api.postman_collection.json` e pode ser importada no Postman ou no Insomnia. Cada requisição confere o status esperado e as datas de prazo são geradas na hora. Rodar na ordem das pastas.

As requisições e respostas da execução estão em `docs/testes-api.md`.

Também há 38 testes automatizados com MockMvc, que cobrem sucesso e falha de todos os endpoints:

```bash
./mvnw test
```

## Estrutura

```
config/       OpenApiConfig (Swagger) e RequisicaoFilter (log e headers)
controller/   TarefaController (endpoints)
service/      TarefaService (regras de negócio)
repository/   TarefaRepository (JPA)
model/        Tarefa e StatusTarefa
dto/          dados de entrada e de resposta
exception/    exceções e tratamento global dos erros
```

## Conteúdo das unidades aplicado

**Unidade 2: Fundamentos de APIs RESTful**

- Recurso `tarefas` identificado por URI (`/api/tarefas` e `/api/tarefas/{id}`)
- Cada método HTTP ligado a uma operação do CRUD
- Comunicação sem estado e dados trocados em JSON

**Unidade 3: Implementação de Serviços Web com Spring Boot**

- Controlador RESTful com `@RestController`, `@RequestMapping` e as anotações de cada método (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`)
- Mapeamento de endpoints com variáveis de caminho (`@PathVariable`) e parâmetros de consulta (`@RequestParam`), seguindo a convenção de substantivos no plural e sub-recursos (`/{id}/status`)
- Validação de entrada com Bean Validation no corpo e nos parâmetros (`@Valid`, `@Validated`, `@Size`, `@Pattern`, `@Min`, `@Max`, `@Positive`)
- Exceções personalizadas tratadas de forma global com `@RestControllerAdvice`, incluindo erro interno (500) sem expor detalhes
- Respostas com `ResponseEntity`, códigos de status adequados e metadados no corpo (paginação) e nos headers (`Location`, `X-Total-Count`, `X-Request-Id`)
- DTOs separando o que entra, o que sai e a entidade do banco
- Paginação, ordenação e filtros
- Segurança: consultas pelo Spring Data JPA (parametrizadas, sem SQL montado na mão), bloqueio de `<` e `>` contra XSS, campos desconhecidos recusados, ordenação só por campos permitidos e limite no tamanho da página. A API não usa sessão nem cookies, então não fica exposta a CSRF
- Logs com SLF4J e monitoramento com Spring Boot Actuator

## Identidade visual

| Cor | Hex |
|---|---|
| Verde | `#5BBE3A` |
| Verde-lima | `#A6E35A` |
| Escuro | `#0A140F` |
| Grafite | `#2E343B` |
| Cinza claro | `#C9CCD1` |

Fonte: Plus Jakarta Sans.
