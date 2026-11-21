# Changelog

## 3.0.0 (entrega 3)

### Adicionado
- Versionamento por URL: `/api/v1/tarefas` e `/api/v2/tarefas`, cada um com o seu controlador e os seus DTOs
- `GET /api` com a lista de versões
- Headers `API-Version`, `Deprecation`, `Sunset` e `Link` nas respostas
- Erros no formato Problem Details (RFC 9457), com `type`, `title`, `detail`, `instance`, `codigo`, `dica` e `requestId`
- Erro `VERSAO_NAO_SUPORTADA` para versões que não existem
- Header `Allow` no erro 405
- Catálogo de erros em `docs/erros.md`
- Swagger com um grupo por versão, exemplos de erro e guia de início rápido
- Especificação OpenAPI exportada em `docs/openapi/`
- Testes da v1, do versionamento e do formato de erro

### Alterado
- Pacotes reorganizados por módulo (`tarefa`) e camada, com o que é comum em `shared`
- O serviço passou a usar objetos do domínio (`DadosTarefa`, `ResumoTarefas`) e não depende mais dos DTOs
- Mensagens de erro mais específicas: dizem o valor recebido e o formato esperado
- O campo `mensagem` dos erros virou `detail`, seguindo o padrão

### Depreciado
- v1 (`/api/v1/tarefas`): funciona até 30/06/2027
- Endereço sem versão (`/api/tarefas`): continua respondendo como a v2, mas o certo é usar `/api/v2/tarefas`

## 2.0.0 (entrega 2)

- Validações nos campos e parâmetros, regras de prazo e título repetido
- Paginação, filtros e ordenação
- `PATCH /api/tarefas/{id}/status` e `GET /api/tarefas/resumo`
- DTOs de entrada e resposta
- Erros com código para cada caso
- Log das requisições e Actuator

## 1.0.0 (entrega 1)

- Projeto Spring Boot
- CRUD de tarefas: GET, POST, PUT e DELETE em `/api/tarefas`
- Validação e tratamento de erros básico
