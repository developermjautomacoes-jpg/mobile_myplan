![myplan](docs/img/myplan-banner.png)

# myplan API

**Sua vida. Seus planos. Sempre em foco.**

Atividade Processual 2 da disciplina Serviços Web e API Rest.

O myplan é um app de tarefas pessoais. Esta é a API dele, feita com Spring Boot: permite criar, listar, atualizar e excluir tarefas usando os métodos GET, POST, PUT e DELETE.

## Tecnologias

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 (banco em memória)
- Springdoc OpenAPI (Swagger)

## Como rodar

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`.

A API sobe em `http://localhost:8080`. Nesse endereço tem uma página inicial do myplan com os endpoints e o link para a documentação.

- Swagger: `http://localhost:8080/swagger-ui.html`
- Console do H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:myplandb`, usuário `sa`, sem senha)

Como o banco é em memória, os dados são apagados quando a aplicação reinicia.

## Endpoints

URL base: `/api/tarefas`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/tarefas` | Lista todas as tarefas |
| GET | `/api/tarefas/{id}` | Busca uma tarefa pelo id |
| POST | `/api/tarefas` | Cria uma tarefa |
| PUT | `/api/tarefas/{id}` | Atualiza uma tarefa |
| DELETE | `/api/tarefas/{id}` | Exclui uma tarefa |

### Campos da tarefa

- `titulo` (obrigatório, até 100 caracteres)
- `descricao` (opcional, até 500 caracteres)
- `status` (opcional): `PENDENTE`, `EM_ANDAMENTO` ou `CONCLUIDA`. Se não for informado, fica `PENDENTE`
- `prazo` (opcional): data no formato `aaaa-mm-dd`

Os campos `id`, `dataCriacao` e `dataAtualizacao` são preenchidos pela API.

### GET /api/tarefas

Retorna a lista de tarefas. Aceita o parâmetro opcional `status` para filtrar, por exemplo `/api/tarefas?status=PENDENTE`.

- 200: lista de tarefas (pode vir vazia)
- 400: status inválido

### GET /api/tarefas/{id}

- 200: tarefa encontrada
- 404: tarefa não existe
- 400: id inválido (ex.: `/api/tarefas/abc`)

### POST /api/tarefas

```json
{
  "titulo": "Pagar conta de luz",
  "descricao": "Vence dia 10",
  "prazo": "2026-12-10"
}
```

- 201: tarefa criada. O header `Location` traz o endereço da nova tarefa
- 400: título vazio, status inválido, data errada ou JSON mal formado

### PUT /api/tarefas/{id}

Envia a tarefa completa com os novos valores.

```json
{
  "titulo": "Pagar conta de luz",
  "descricao": "Pago",
  "status": "CONCLUIDA",
  "prazo": "2026-12-10"
}
```

- 200: tarefa atualizada
- 400: dados inválidos
- 404: tarefa não existe

### DELETE /api/tarefas/{id}

- 204: tarefa excluída
- 404: tarefa não existe

### Resposta de erro

```json
{
  "timestamp": "2026-11-20T21:43:55",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos",
  "caminho": "/api/tarefas",
  "campos": {
    "titulo": "O título é obrigatório"
  }
}
```

## Testes

Testei os endpoints com o Postman. A coleção está em `docs/myplan-api.postman_collection.json` e pode ser importada no Postman ou no Insomnia. Cada requisição já confere o status esperado. Rodar na ordem das pastas: POST, GET, PUT e DELETE.

As requisições e respostas da execução estão em `docs/testes-api.md`.

Também há testes automatizados com MockMvc, que cobrem sucesso e falha de cada método:

```bash
./mvnw test
```

## Estrutura

```
config/       OpenApiConfig (dados do Swagger)
controller/   TarefaController (endpoints)
service/      TarefaService (regras)
repository/   TarefaRepository (JPA)
model/        Tarefa e StatusTarefa
dto/          TarefaRequest (dados de entrada com validação)
exception/    tratamento dos erros
```

## Conteúdo da unidade aplicado

- Recurso `tarefas` identificado por URI (`/api/tarefas` e `/api/tarefas/{id}`)
- Cada método HTTP ligado a uma operação do CRUD: GET (ler), POST (criar), PUT (atualizar) e DELETE (excluir)
- Comunicação sem estado: cada requisição traz tudo o que precisa
- Dados trocados em JSON
- Códigos de status: 200, 201, 204, 400 e 404
- Validação da entrada com `@Valid` e tratamento de erros com `@RestControllerAdvice`

## Identidade visual

| Cor | Hex |
|---|---|
| Verde | `#5BBE3A` |
| Verde-lima | `#A6E35A` |
| Escuro | `#0A140F` |
| Grafite | `#2E343B` |
| Cinza claro | `#C9CCD1` |

Fonte: Plus Jakarta Sans.
