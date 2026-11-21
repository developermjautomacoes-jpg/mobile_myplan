# Catálogo de erros da myplan API

Todos os erros da API seguem o formato **Problem Details** ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)), com o `Content-Type: application/problem+json`. Além dos campos do padrão, o myplan envia `codigo`, `dica`, `campos` e `requestId`.

```json
{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#tarefa-nao-encontrada",
  "title": "Tarefa não encontrada",
  "status": 404,
  "detail": "Não existe tarefa com o id 99",
  "instance": "/api/v2/tarefas/99",
  "codigo": "TAREFA_NAO_ENCONTRADA",
  "dica": "Confira o id. Para ver os ids existentes, liste as tarefas em GET /api/v2/tarefas.",
  "requestId": "3f2a9c1e-7b4d-4e8a-9f10-2c6d8e5a1b7f",
  "timestamp": "2026-11-20T22:40:12.345"
}
```

| Campo | O que é |
|---|---|
| `type` | Link para a explicação do erro neste catálogo |
| `title` | Resumo do erro (sempre o mesmo para o mesmo código) |
| `status` | Status HTTP |
| `detail` | O que aconteceu nesta requisição específica |
| `instance` | Endereço que foi chamado |
| `codigo` | Código fixo, para o app tratar cada caso |
| `dica` | Como corrigir |
| `campos` | Erros por campo (só quando o problema é em campos) |
| `requestId` | Mesmo valor do header `X-Request-Id`. Serve para achar a requisição no log |

## Lista de códigos

| Código | Status | Quando acontece |
|---|---|---|
| [`DADOS_INVALIDOS`](#dados-invalidos) | 400 | Campos do corpo fora das regras |
| [`CORPO_INVALIDO`](#corpo-invalido) | 400 | JSON mal formado, campo que não existe, status ou data em formato errado |
| [`PARAMETRO_INVALIDO`](#parametro-invalido) | 400 | Parâmetro da URL inválido |
| [`TAREFA_NAO_ENCONTRADA`](#tarefa-nao-encontrada) | 404 | Id que não existe |
| [`ROTA_NAO_ENCONTRADA`](#rota-nao-encontrada) | 404 | Endereço que não existe |
| [`VERSAO_NAO_SUPORTADA`](#versao-nao-suportada) | 404 | Versão da API que não existe |
| [`METODO_NAO_SUPORTADO`](#metodo-nao-suportado) | 405 | Método HTTP não aceito no endereço |
| [`TAREFA_DUPLICADA`](#tarefa-duplicada) | 409 | Título repetido em tarefa aberta |
| [`TIPO_DE_CONTEUDO_INVALIDO`](#tipo-de-conteudo-invalido) | 415 | Corpo enviado sem ser JSON |
| [`REGRA_DE_NEGOCIO`](#regra-de-negocio) | 422 | Regra do myplan violada |
| [`ERRO_INTERNO`](#erro-interno) | 500 | Erro inesperado no servidor |

---

### dados-invalidos

**400 – Dados inválidos**

Um ou mais campos do corpo não seguem as regras. O campo `campos` diz qual campo está errado e por quê.

```json
"campos": {
  "titulo": "O título deve ter entre 3 e 100 caracteres",
  "descricao": "A descrição não pode conter os caracteres < ou >"
}
```

**Como corrigir:** ajuste os campos listados. Regras: título de 3 a 100 caracteres (na v1, até 100), descrição até 500, nenhum dos dois com `<` ou `>`.

### corpo-invalido

**400 – Corpo da requisição inválido**

O JSON não pôde ser lido. Os casos mais comuns:

| Situação | Exemplo de `detail` |
|---|---|
| JSON quebrado | O corpo da requisição não é um JSON válido |
| Campo que não existe | O campo 'id' não existe na tarefa e não pode ser enviado |
| Status inexistente | O valor 'FEITA' não é válido para 'status'. Valores aceitos: [PENDENTE, EM_ANDAMENTO, CONCLUIDA] |
| Data fora do formato | A data '10/12/2026' em 'prazo' está fora do formato. Use aaaa-mm-dd, por exemplo 2026-12-10 |

**Como corrigir:** envie só `titulo`, `descricao`, `status` e `prazo`, com o status em maiúsculas e a data como `aaaa-mm-dd`. O `id` e as datas de criação são definidos pela API.

### parametro-invalido

**400 – Parâmetro inválido**

Algum valor na URL está errado: id negativo ou que não é número, página negativa, tamanho fora de 1 a 50, ordenação por campo não permitido ou direção diferente de `asc`/`desc`.

**Como corrigir:** confira os parâmetros na documentação do endpoint. Na listagem da v2: `ordenarPor` aceita `titulo`, `prazo`, `status` e `dataCriacao`.

### tarefa-nao-encontrada

**404 – Tarefa não encontrada**

Não existe tarefa com o id informado. Pode ter sido excluída.

**Como corrigir:** liste as tarefas (`GET /api/v2/tarefas`) para ver os ids existentes.

### rota-nao-encontrada

**404 – Endereço não encontrado**

O endereço chamado não existe. Também acontece ao usar na v1 um recurso que só existe na v2, como `PATCH /api/v1/tarefas/{id}/status`.

**Como corrigir:** confira o endereço. Os recursos ficam em `/api/v1/tarefas` e `/api/v2/tarefas`.

### versao-nao-suportada

**404 – Versão da API não suportada**

A URL pede uma versão que não existe, como `/api/v3/tarefas`.

**Como corrigir:** use `v1` ou `v2`. A lista de versões fica em `GET /api`.

### metodo-nao-suportado

**405 – Método não suportado**

O endereço existe, mas não aceita o método usado. O header `Allow` da resposta lista os métodos aceitos.

**Como corrigir:** use um dos métodos do header `Allow`.

### tarefa-duplicada

**409 – Tarefa duplicada**

Já existe uma tarefa com o mesmo título (sem diferenciar maiúsculas) que ainda não foi concluída.

**Como corrigir:** escolha outro título ou conclua a tarefa que já existe. Tarefas concluídas não contam.

### tipo-de-conteudo-invalido

**415 – Tipo de conteúdo não suportado**

O corpo foi enviado com um `Content-Type` diferente de JSON.

**Como corrigir:** envie o header `Content-Type: application/json`.

### regra-de-negocio

**422 – Operação não permitida**

Os dados estão no formato certo, mas não fazem sentido para o myplan:

- prazo com uma data que já passou (no POST, ou ao alterar o prazo no PUT)
- mudar o status para o mesmo status que a tarefa já tem

**Como corrigir:** leia o `detail`, que explica a regra que foi violada.

### erro-interno

**500 – Erro interno**

Algo inesperado aconteceu no servidor. Por segurança, a resposta não traz detalhes técnicos. Eles ficam registrados no log, junto com o `requestId`.

**Como corrigir:** tente de novo. Se continuar, informe o `requestId` ao suporte.
