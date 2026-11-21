# Testes da myplan API (entrega 3)

Requisições feitas com a aplicação rodando localmente (porta 8080) e as respostas que a API devolveu. Os headers `Date`, `Connection` e afins foram omitidos.

## 1. Versionamento

### Versões disponíveis

```
GET /api
```

```
HTTP/1.1 200 
X-Request-Id: 027ab726-37da-4390-a785-9b0871470246
Content-Type: application/json

[
  {
    "versao": "v1",
    "endereco": "/api/v1/tarefas",
    "situacao": "DEPRECIADA",
    "fimDoSuporte": "Wed, 30 Jun 2027 23:59:59 GMT",
    "novidades": "CRUD básico com lista simples"
  },
  {
    "versao": "v2",
    "endereco": "/api/v2/tarefas",
    "situacao": "ATUAL",
    "fimDoSuporte": null,
    "novidades": "Paginação, filtros, ordenação, PATCH de status, resumo e campo 'atrasada'"
  }
]
```

### v2: criar tarefa (201)

```
POST /api/v2/tarefas
{"titulo":"Pagar conta de luz","descricao":"Vence no dia 10","prazo":"2026-11-25"}
```

```
HTTP/1.1 201 
X-Request-Id: entrega3-001
API-Version: 2
Location: http://localhost:8080/api/v2/tarefas/1
Content-Type: application/json

{
  "id": 1,
  "titulo": "Pagar conta de luz",
  "descricao": "Vence no dia 10",
  "status": "PENDENTE",
  "prazo": "2026-11-25",
  "atrasada": false,
  "dataCriacao": "2026-11-20T22:29:06.632403168",
  "dataAtualizacao": null
}
```

### v1: criar tarefa (201)

```
POST /api/v1/tarefas
{"titulo":"Ir ao mercado","status":"EM_ANDAMENTO"}
```

```
HTTP/1.1 201 
X-Request-Id: 2b2f7a16-bd8f-4705-9f8a-12d77e389316
API-Version: 1
Deprecation: true
Sunset: Wed, 30 Jun 2027 23:59:59 GMT
Link: </api/v2/tarefas>; rel="successor-version"
Location: http://localhost:8080/api/v1/tarefas/2
Content-Type: application/json

{
  "id": 2,
  "titulo": "Ir ao mercado",
  "descricao": null,
  "status": "EM_ANDAMENTO",
  "prazo": null,
  "dataCriacao": "2026-11-20T22:29:06.724757615",
  "dataAtualizacao": null
}
```

### v2: listar (lista paginada)

```
GET /api/v2/tarefas?tamanho=5
```

```
HTTP/1.1 200 
X-Request-Id: b1336ba5-a55e-4824-8ccd-33703bb3cac4
API-Version: 2
X-Total-Count: 2
Content-Type: application/json

{
  "conteudo": [
    {
      "id": 2,
      "titulo": "Ir ao mercado",
      "descricao": null,
      "status": "EM_ANDAMENTO",
      "prazo": null,
      "atrasada": false,
      "dataCriacao": "2026-11-20T22:29:06.724758",
      "dataAtualizacao": null
    },
    {
      "id": 1,
      "titulo": "Pagar conta de luz",
      "descricao": "Vence no dia 10",
      "status": "PENDENTE",
      "prazo": "2026-11-25",
      "atrasada": false,
      "dataCriacao": "2026-11-20T22:29:06.632403",
      "dataAtualizacao": null
    }
  ],
  "pagina": 0,
  "tamanho": 5,
  "totalElementos": 2,
  "totalPaginas": 1,
  "primeira": true,
  "ultima": true
}
```

### v1: listar (lista simples, com aviso de depreciação nos headers)

```
GET /api/v1/tarefas
```

```
HTTP/1.1 200 
X-Request-Id: 59387d25-f29d-4377-8b58-fde7ccff0c1c
API-Version: 1
Deprecation: true
Sunset: Wed, 30 Jun 2027 23:59:59 GMT
Link: </api/v2/tarefas>; rel="successor-version"
Content-Type: application/json

[
  {
    "id": 1,
    "titulo": "Pagar conta de luz",
    "descricao": "Vence no dia 10",
    "status": "PENDENTE",
    "prazo": "2026-11-25",
    "dataCriacao": "2026-11-20T22:29:06.632403",
    "dataAtualizacao": null
  },
  {
    "id": 2,
    "titulo": "Ir ao mercado",
    "descricao": null,
    "status": "EM_ANDAMENTO",
    "prazo": null,
    "dataCriacao": "2026-11-20T22:29:06.724758",
    "dataAtualizacao": null
  }
]
```

### Endereço sem versão continua funcionando e aponta para a v2

```
GET /api/tarefas/1
```

```
HTTP/1.1 200 
X-Request-Id: 09512e4f-b48c-4d65-bb54-877fb5423d15
API-Version: 2
Deprecation: true
Link: </api/v2/tarefas/1>; rel="successor-version"
Content-Type: application/json

{
  "id": 1,
  "titulo": "Pagar conta de luz",
  "descricao": "Vence no dia 10",
  "status": "PENDENTE",
  "prazo": "2026-11-25",
  "atrasada": false,
  "dataCriacao": "2026-11-20T22:29:06.632403",
  "dataAtualizacao": null
}
```

### Versão que não existe (404)

```
GET /api/v3/tarefas
```

```
HTTP/1.1 404 
X-Request-Id: 9f770017-efee-47c0-9275-3a6695c35be5
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#versao-nao-suportada",
  "title": "Versão da API não suportada",
  "status": 404,
  "detail": "A versão v3 não existe",
  "instance": "/api/v3/tarefas",
  "codigo": "VERSAO_NAO_SUPORTADA",
  "dica": "Use uma das versões disponíveis: v1 ou v2. A lista fica em GET /api.",
  "requestId": "9f770017-efee-47c0-9275-3a6695c35be5",
  "timestamp": "2026-11-20T22:29:06.941853592"
}
```

### Recurso que só existe na v2, chamado na v1 (404)

```
PATCH /api/v1/tarefas/1/status
{"status":"CONCLUIDA"}
```

```
HTTP/1.1 404 
X-Request-Id: 100009af-cd4d-467b-9b9e-dd16771f0566
API-Version: 1
Deprecation: true
Sunset: Wed, 30 Jun 2027 23:59:59 GMT
Link: </api/v2/tarefas/1/status>; rel="successor-version"
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#rota-nao-encontrada",
  "title": "Endereço não encontrado",
  "status": 404,
  "detail": "O endereço /api/v1/tarefas/1/status não existe nesta API",
  "instance": "/api/v1/tarefas/1/status",
  "codigo": "ROTA_NAO_ENCONTRADA",
  "dica": "Confira o endereço. Os endpoints da API começam com /api/v1 ou /api/v2.",
  "requestId": "100009af-cd4d-467b-9b9e-dd16771f0566",
  "timestamp": "2026-11-20T22:29:06.95984039"
}
```

### O mesmo recurso na v2 (200)

```
PATCH /api/v2/tarefas/1/status
{"status":"CONCLUIDA"}
```

```
HTTP/1.1 200 
X-Request-Id: 7d6ea944-fd37-4fb9-972f-3cd754f1f165
API-Version: 2
Content-Type: application/json

{
  "id": 1,
  "titulo": "Pagar conta de luz",
  "descricao": "Vence no dia 10",
  "status": "CONCLUIDA",
  "prazo": "2026-11-25",
  "atrasada": false,
  "dataCriacao": "2026-11-20T22:29:06.632403",
  "dataAtualizacao": "2026-11-20T22:29:07.00256953"
}
```

## 2. Tratamento de erros (formato Problem Details)

### Campos inválidos (400)

```
POST /api/v2/tarefas
{"titulo":"ab","descricao":"<b>teste</b>"}
```

```
HTTP/1.1 400 
X-Request-Id: 490d453b-93ea-4de4-b4ee-f151cb09a328
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#dados-invalidos",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "2 campos estão inválidos",
  "instance": "/api/v2/tarefas",
  "codigo": "DADOS_INVALIDOS",
  "dica": "Corrija os campos listados em 'campos' e envie de novo.",
  "campos": {
    "descricao": "A descrição não pode conter os caracteres < ou >",
    "titulo": "O título deve ter entre 3 e 100 caracteres"
  },
  "requestId": "490d453b-93ea-4de4-b4ee-f151cb09a328",
  "timestamp": "2026-11-20T22:29:07.031677182"
}
```

### Campo que não existe (400)

```
POST /api/v2/tarefas
{"id":99,"titulo":"Tentando escolher o id"}
```

```
HTTP/1.1 400 
X-Request-Id: c84b1871-b05a-48e7-9402-bba7c41a9d61
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#corpo-invalido",
  "title": "Corpo da requisição inválido",
  "status": 400,
  "detail": "O campo 'id' não existe na tarefa e não pode ser enviado",
  "instance": "/api/v2/tarefas",
  "codigo": "CORPO_INVALIDO",
  "dica": "Envie um JSON válido, só com os campos documentados (titulo, descricao, status, prazo).",
  "requestId": "c84b1871-b05a-48e7-9402-bba7c41a9d61",
  "timestamp": "2026-11-20T22:29:07.062107632"
}
```

### Data fora do formato (400)

```
POST /api/v2/tarefas
{"titulo":"Pagar boleto","prazo":"10/12/2026"}
```

```
HTTP/1.1 400 
X-Request-Id: c097e283-adc1-456e-a281-b695c24b8646
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#corpo-invalido",
  "title": "Corpo da requisição inválido",
  "status": 400,
  "detail": "A data '10/12/2026' em 'prazo' está fora do formato. Use aaaa-mm-dd, por exemplo 2026-12-10",
  "instance": "/api/v2/tarefas",
  "codigo": "CORPO_INVALIDO",
  "dica": "Envie um JSON válido, só com os campos documentados (titulo, descricao, status, prazo).",
  "requestId": "c097e283-adc1-456e-a281-b695c24b8646",
  "timestamp": "2026-11-20T22:29:07.081149378"
}
```

### Status inexistente (400)

```
POST /api/v2/tarefas
{"titulo":"Teste","status":"FEITA"}
```

```
HTTP/1.1 400 
X-Request-Id: 71cafa54-fed8-4144-8086-1c9a7deba959
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#corpo-invalido",
  "title": "Corpo da requisição inválido",
  "status": 400,
  "detail": "O valor 'FEITA' não é válido para 'status'. Valores aceitos: [PENDENTE, EM_ANDAMENTO, CONCLUIDA]",
  "instance": "/api/v2/tarefas",
  "codigo": "CORPO_INVALIDO",
  "dica": "Envie um JSON válido, só com os campos documentados (titulo, descricao, status, prazo).",
  "requestId": "71cafa54-fed8-4144-8086-1c9a7deba959",
  "timestamp": "2026-11-20T22:29:07.106551804"
}
```

### Parâmetros da URL inválidos (400)

```
GET /api/v2/tarefas?tamanho=500&pagina=-1
```

```
HTTP/1.1 400 
X-Request-Id: a0ab6613-a098-46a3-b168-c8c7785d93df
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#parametro-invalido",
  "title": "Parâmetro inválido",
  "status": 400,
  "detail": "Um ou mais parâmetros da URL estão inválidos",
  "instance": "/api/v2/tarefas",
  "codigo": "PARAMETRO_INVALIDO",
  "dica": "Confira os parâmetros da URL na documentação do endpoint.",
  "campos": {
    "pagina": "A página não pode ser negativa",
    "tamanho": "O tamanho máximo é 50"
  },
  "requestId": "a0ab6613-a098-46a3-b168-c8c7785d93df",
  "timestamp": "2026-11-20T22:29:07.128403471"
}
```

### Id que não é número (400)

```
GET /api/v2/tarefas/abc
```

```
HTTP/1.1 400 
X-Request-Id: a0054e92-d53a-4e4f-9e1a-fac8ed974d19
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#parametro-invalido",
  "title": "Parâmetro inválido",
  "status": 400,
  "detail": "O valor 'abc' não é válido para 'id'. Informe um número inteiro",
  "instance": "/api/v2/tarefas/abc",
  "codigo": "PARAMETRO_INVALIDO",
  "dica": "Confira os parâmetros da URL na documentação do endpoint.",
  "requestId": "a0054e92-d53a-4e4f-9e1a-fac8ed974d19",
  "timestamp": "2026-11-20T22:29:07.153007692"
}
```

### Tarefa que não existe (404)

```
GET /api/v2/tarefas/99
```

```
HTTP/1.1 404 
X-Request-Id: 487eb73d-4caf-49fb-afb8-781b88e1515f
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#tarefa-nao-encontrada",
  "title": "Tarefa não encontrada",
  "status": 404,
  "detail": "Não existe tarefa com o id 99",
  "instance": "/api/v2/tarefas/99",
  "codigo": "TAREFA_NAO_ENCONTRADA",
  "dica": "Confira o id. Para ver os ids existentes, liste as tarefas em GET /api/v2/tarefas.",
  "requestId": "487eb73d-4caf-49fb-afb8-781b88e1515f",
  "timestamp": "2026-11-20T22:29:07.182796837"
}
```

### Endereço que não existe (404)

```
GET /api/v2/tarefass
```

```
HTTP/1.1 404 
X-Request-Id: 6419d0b7-7050-4090-a663-bc2a4652c557
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#rota-nao-encontrada",
  "title": "Endereço não encontrado",
  "status": 404,
  "detail": "O endereço /api/v2/tarefass não existe nesta API",
  "instance": "/api/v2/tarefass",
  "codigo": "ROTA_NAO_ENCONTRADA",
  "dica": "Confira o endereço. Os endpoints da API começam com /api/v1 ou /api/v2.",
  "requestId": "6419d0b7-7050-4090-a663-bc2a4652c557",
  "timestamp": "2026-11-20T22:29:07.211519805"
}
```

### Método não suportado (405)

```
DELETE /api/v2/tarefas
```

```
HTTP/1.1 405 
X-Request-Id: c346e051-8466-46a7-ad3f-2f22951d6dbb
API-Version: 2
Allow: POST,GET
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#metodo-nao-suportado",
  "title": "Método não suportado",
  "status": 405,
  "detail": "O método DELETE não é aceito em /api/v2/tarefas. Métodos aceitos: [POST, GET]",
  "instance": "/api/v2/tarefas",
  "codigo": "METODO_NAO_SUPORTADO",
  "dica": "Use um dos métodos informados no header 'Allow' da resposta.",
  "requestId": "c346e051-8466-46a7-ad3f-2f22951d6dbb",
  "timestamp": "2026-11-20T22:29:07.244279163"
}
```

### Título repetido (409)

```
POST /api/v1/tarefas
{"titulo":"ir ao MERCADO"}
```

```
HTTP/1.1 409 
X-Request-Id: f60c8cf4-72bf-49ff-9431-398369acceff
API-Version: 1
Deprecation: true
Sunset: Wed, 30 Jun 2027 23:59:59 GMT
Link: </api/v2/tarefas>; rel="successor-version"
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#tarefa-duplicada",
  "title": "Tarefa duplicada",
  "status": 409,
  "detail": "Já existe uma tarefa em aberto com o título 'ir ao MERCADO'",
  "instance": "/api/v1/tarefas",
  "codigo": "TAREFA_DUPLICADA",
  "dica": "Use outro título ou conclua a tarefa que já existe com esse nome.",
  "requestId": "f60c8cf4-72bf-49ff-9431-398369acceff",
  "timestamp": "2026-11-20T22:29:07.27014202"
}
```

### Corpo que não é JSON (415)

```
POST /api/v2/tarefas  (Content-Type: text/plain)
Pagar conta
```

```
HTTP/1.1 415 
X-Request-Id: 0ff1ca27-b91c-425c-ad6b-0001756a5b87
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#tipo-de-conteudo-invalido",
  "title": "Tipo de conteúdo não suportado",
  "status": 415,
  "detail": "O Content-Type recebido foi 'text/plain', mas este endpoint só aceita application/json",
  "instance": "/api/v2/tarefas",
  "codigo": "TIPO_DE_CONTEUDO_INVALIDO",
  "dica": "Envie o corpo em JSON com o header 'Content-Type: application/json'.",
  "requestId": "0ff1ca27-b91c-425c-ad6b-0001756a5b87",
  "timestamp": "2026-11-20T22:29:07.29462073"
}
```

### Prazo no passado (422)

```
POST /api/v2/tarefas
{"titulo":"Pagar boleto","prazo":"2026-11-19"}
```

```
HTTP/1.1 422 
X-Request-Id: 22ad08e9-3f9d-4f56-88f4-9a1d4a0a7583
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#regra-de-negocio",
  "title": "Operação não permitida",
  "status": 422,
  "detail": "O prazo 2026-11-19 já passou. Informe a data de hoje ou uma data futura",
  "instance": "/api/v2/tarefas",
  "codigo": "REGRA_DE_NEGOCIO",
  "dica": "Os dados estão no formato certo, mas violam uma regra do myplan. Leia o 'detalhe'.",
  "requestId": "22ad08e9-3f9d-4f56-88f4-9a1d4a0a7583",
  "timestamp": "2026-11-20T22:29:07.32902394"
}
```

### Mesmo status (422)

```
PATCH /api/v2/tarefas/1/status
{"status":"CONCLUIDA"}
```

```
HTTP/1.1 422 
X-Request-Id: e370763f-7362-4149-951d-dc7b386e1ea9
API-Version: 2
Content-Type: application/problem+json

{
  "type": "https://github.com/developermjautomacoes-jpg/mobile_myplan/blob/main/api/docs/erros.md#regra-de-negocio",
  "title": "Operação não permitida",
  "status": 422,
  "detail": "A tarefa 1 já está com o status CONCLUIDA",
  "instance": "/api/v2/tarefas/1/status",
  "codigo": "REGRA_DE_NEGOCIO",
  "dica": "Os dados estão no formato certo, mas violam uma regra do myplan. Leia o 'detalhe'.",
  "requestId": "e370763f-7362-4149-951d-dc7b386e1ea9",
  "timestamp": "2026-11-20T22:29:07.360957415"
}
```

## 3. Log dos erros

Cada erro também fica registrado no log, com o mesmo `requestId` que vai na resposta:

```
2026-11-20T22:29:06.941-03:00  [9f770017-efee-47c0-9275-3a6695c35be5] GET /api/v3/tarefas -> 404 VERSAO_NAO_SUPORTADA: A versão v3 não existe
2026-11-20T22:29:06.959-03:00  [100009af-cd4d-467b-9b9e-dd16771f0566] PATCH /api/v1/tarefas/1/status -> 404 ROTA_NAO_ENCONTRADA: O endereço /api/v1/tarefas/1/status não existe nesta API
2026-11-20T22:29:07.031-03:00  [490d453b-93ea-4de4-b4ee-f151cb09a328] POST /api/v2/tarefas -> 400 DADOS_INVALIDOS: 2 campos estão inválidos
2026-11-20T22:29:07.061-03:00  [c84b1871-b05a-48e7-9402-bba7c41a9d61] POST /api/v2/tarefas -> 400 CORPO_INVALIDO: O campo 'id' não existe na tarefa e não pode ser enviado
2026-11-20T22:29:07.080-03:00  [c097e283-adc1-456e-a281-b695c24b8646] POST /api/v2/tarefas -> 400 CORPO_INVALIDO: A data '10/12/2026' em 'prazo' está fora do formato. Use aaaa-mm-dd, por exemplo 2026-12-10
2026-11-20T22:29:07.106-03:00  [71cafa54-fed8-4144-8086-1c9a7deba959] POST /api/v2/tarefas -> 400 CORPO_INVALIDO: O valor 'FEITA' não é válido para 'status'. Valores aceitos: [PENDENTE, EM_ANDAMENTO, CONCLUIDA]
2026-11-20T22:29:07.128-03:00  [a0ab6613-a098-46a3-b168-c8c7785d93df] GET /api/v2/tarefas -> 400 PARAMETRO_INVALIDO: Um ou mais parâmetros da URL estão inválidos
2026-11-20T22:29:07.152-03:00  [a0054e92-d53a-4e4f-9e1a-fac8ed974d19] GET /api/v2/tarefas/abc -> 400 PARAMETRO_INVALIDO: O valor 'abc' não é válido para 'id'. Informe um número inteiro
```
