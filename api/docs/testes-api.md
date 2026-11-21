# Testes da myplan API (entrega 2)

Requisições feitas com a aplicação rodando localmente (porta 8080) e as respostas que a API devolveu. Os headers `Date`, `Connection` e afins foram omitidos.

# 1. Criar (POST)

## POST com sucesso (201)

```
POST /api/tarefas
{"titulo":"Pagar conta de luz","descricao":"Vence no dia 10","prazo":"2026-11-25"}
```

```
HTTP/1.1 201 
X-Request-Id: teste-001
Location: http://localhost:8080/api/tarefas/1
Content-Type: application/json

{
  "id": 1,
  "titulo": "Pagar conta de luz",
  "descricao": "Vence no dia 10",
  "status": "PENDENTE",
  "prazo": "2026-11-25",
  "atrasada": false,
  "dataCriacao": "2026-11-20T22:10:32.585404278",
  "dataAtualizacao": null
}
```

## Campos inválidos (400)

```
POST /api/tarefas
{"titulo":"ab","descricao":"<b>teste</b>"}
```

```
HTTP/1.1 400 
X-Request-Id: d4ddb723-ae07-4d3f-a885-fdc106345356
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:32.735456821",
  "status": 400,
  "erro": "Bad Request",
  "codigo": "DADOS_INVALIDOS",
  "mensagem": "Um ou mais campos estão inválidos",
  "caminho": "/api/tarefas",
  "campos": {
    "titulo": "O título deve ter entre 3 e 100 caracteres",
    "descricao": "A descrição não pode conter os caracteres < ou >"
  }
}
```

## Campo que não existe no recurso (400)

```
POST /api/tarefas
{"id":99,"titulo":"Tentando escolher o id"}
```

```
HTTP/1.1 400 
X-Request-Id: 29eb8bf3-96ce-4a78-bb43-7e0071e3ca57
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:32.763169484",
  "status": 400,
  "erro": "Bad Request",
  "codigo": "CORPO_INVALIDO",
  "mensagem": "O campo 'id' não é aceito",
  "caminho": "/api/tarefas"
}
```

## Status inválido (400)

```
POST /api/tarefas
{"titulo":"Teste","status":"FEITA"}
```

```
HTTP/1.1 400 
X-Request-Id: 12a2886e-36b6-41ee-925b-fadb7bd5e3ca
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:32.80433286",
  "status": 400,
  "erro": "Bad Request",
  "codigo": "CORPO_INVALIDO",
  "mensagem": "Valor 'FEITA' inválido para 'status'. Valores aceitos: [PENDENTE, EM_ANDAMENTO, CONCLUIDA]",
  "caminho": "/api/tarefas"
}
```

## Data em formato errado (400)

```
POST /api/tarefas
{"titulo":"Teste","prazo":"10/12/2026"}
```

```
HTTP/1.1 400 
X-Request-Id: 31e47c85-c3b7-4e48-9797-54f67f0dc840
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:32.835974956",
  "status": 400,
  "erro": "Bad Request",
  "codigo": "CORPO_INVALIDO",
  "mensagem": "Valor '10/12/2026' inválido para 'prazo'. Use o formato aaaa-mm-dd",
  "caminho": "/api/tarefas"
}
```

## Sem JSON no corpo (415)

```
POST /api/tarefas  (Content-Type: text/plain)
Pagar conta
```

```
HTTP/1.1 415 
X-Request-Id: b3aa2111-fadc-44a7-ada5-0ff1dcbce7c3
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:32.860097279",
  "status": 415,
  "erro": "Unsupported Media Type",
  "codigo": "TIPO_DE_CONTEUDO_INVALIDO",
  "mensagem": "Envie o corpo em JSON (Content-Type: application/json)",
  "caminho": "/api/tarefas"
}
```

## Título repetido em tarefa aberta (409)

```
POST /api/tarefas
{"titulo":"pagar CONTA de luz"}
```

```
HTTP/1.1 409 
X-Request-Id: 6387eac2-f6ac-463e-8ddc-ac249dbbae5b
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:32.888512854",
  "status": 409,
  "erro": "Conflict",
  "codigo": "TAREFA_DUPLICADA",
  "mensagem": "Já existe uma tarefa em aberto com o título 'pagar CONTA de luz'",
  "caminho": "/api/tarefas"
}
```

## Prazo no passado (422)

```
POST /api/tarefas
{"titulo":"Pagar boleto","prazo":"2026-11-19"}
```

```
HTTP/1.1 422 
X-Request-Id: 1b6017f6-d23f-40b5-bfbe-4e31688f6aeb
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:32.910436571",
  "status": 422,
  "erro": "Unprocessable Entity",
  "codigo": "REGRA_DE_NEGOCIO",
  "mensagem": "O prazo não pode ser uma data que já passou",
  "caminho": "/api/tarefas"
}
```

# 2. Consultar (GET)

## Listar com paginação (200)

```
GET /api/tarefas?pagina=0&tamanho=2
```

```
HTTP/1.1 200 
X-Request-Id: 2557a2b9-3dbc-447e-96f2-52fe2378556e
X-Total-Count: 5
Content-Type: application/json

{
  "conteudo": [
    {
      "id": 5,
      "titulo": "Pagar internet",
      "descricao": null,
      "status": "PENDENTE",
      "prazo": null,
      "atrasada": false,
      "dataCriacao": "2026-11-20T22:10:32.703678",
      "dataAtualizacao": null
    },
    {
      "id": 4,
      "titulo": "Marcar dentista",
      "descricao": null,
      "status": "PENDENTE",
      "prazo": null,
      "atrasada": false,
      "dataCriacao": "2026-11-20T22:10:32.693095",
      "dataAtualizacao": null
    }
  ],
  "pagina": 0,
  "tamanho": 2,
  "totalElementos": 5,
  "totalPaginas": 3,
  "primeira": true,
  "ultima": false
}
```

## Filtrar por status e busca, ordenando por título (200)

```
GET /api/tarefas?status=PENDENTE&busca=pagar&ordenarPor=titulo&direcao=asc
```

```
HTTP/1.1 200 
X-Request-Id: abb49c0b-fbab-41e2-8899-4fc6ab349b75
X-Total-Count: 2
Content-Type: application/json

{
  "conteudo": [
    {
      "id": 1,
      "titulo": "Pagar conta de luz",
      "descricao": "Vence no dia 10",
      "status": "PENDENTE",
      "prazo": "2026-11-25",
      "atrasada": false,
      "dataCriacao": "2026-11-20T22:10:32.585404",
      "dataAtualizacao": null
    },
    {
      "id": 5,
      "titulo": "Pagar internet",
      "descricao": null,
      "status": "PENDENTE",
      "prazo": null,
      "atrasada": false,
      "dataCriacao": "2026-11-20T22:10:32.703678",
      "dataAtualizacao": null
    }
  ],
  "pagina": 0,
  "tamanho": 10,
  "totalElementos": 2,
  "totalPaginas": 1,
  "primeira": true,
  "ultima": true
}
```

## Tamanho de página acima do limite (400)

```
GET /api/tarefas?tamanho=500
```

```
HTTP/1.1 400 
X-Request-Id: 70f11bc4-629e-4949-a8db-5f3632e56c0c
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:33.096914833",
  "status": 400,
  "erro": "Bad Request",
  "codigo": "PARAMETRO_INVALIDO",
  "mensagem": "Um ou mais parâmetros estão inválidos",
  "caminho": "/api/tarefas",
  "campos": {
    "tamanho": "O tamanho máximo é 50"
  }
}
```

## Ordenação por campo não permitido (400)

```
GET /api/tarefas?ordenarPor=senha
```

```
HTTP/1.1 400 
X-Request-Id: 1d9a92a3-40bd-4226-8b7c-9fe6727cdf56
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:33.125334459",
  "status": 400,
  "erro": "Bad Request",
  "codigo": "PARAMETRO_INVALIDO",
  "mensagem": "Não é possível ordenar por 'senha'. Use: titulo, prazo, status ou dataCriacao",
  "caminho": "/api/tarefas"
}
```

## Resumo por status (200)

```
GET /api/tarefas/resumo
```

```
HTTP/1.1 200 
X-Request-Id: 60cd474f-d3fc-4869-a0de-f64d6d687f48
Content-Type: application/json

{
  "total": 5,
  "pendentes": 3,
  "emAndamento": 1,
  "concluidas": 1,
  "atrasadas": 0
}
```

## Buscar por id (200)

```
GET /api/tarefas/1
```

```
HTTP/1.1 200 
X-Request-Id: 433ec464-354f-468e-8ea6-7eecda195c02
Content-Type: application/json

{
  "id": 1,
  "titulo": "Pagar conta de luz",
  "descricao": "Vence no dia 10",
  "status": "PENDENTE",
  "prazo": "2026-11-25",
  "atrasada": false,
  "dataCriacao": "2026-11-20T22:10:32.585404",
  "dataAtualizacao": null
}
```

## Id que não existe (404)

```
GET /api/tarefas/99
```

```
HTTP/1.1 404 
X-Request-Id: 4f890ec9-7874-4aba-b665-e551eac0659d
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:33.411334208",
  "status": 404,
  "erro": "Not Found",
  "codigo": "TAREFA_NAO_ENCONTRADA",
  "mensagem": "Tarefa 99 não encontrada",
  "caminho": "/api/tarefas/99"
}
```

## Id negativo (400)

```
GET /api/tarefas/-1
```

```
HTTP/1.1 400 
X-Request-Id: e5ab56e1-143e-423c-a33b-e12c583c9f74
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:33.43565897",
  "status": 400,
  "erro": "Bad Request",
  "codigo": "PARAMETRO_INVALIDO",
  "mensagem": "Um ou mais parâmetros estão inválidos",
  "caminho": "/api/tarefas/-1",
  "campos": {
    "id": "O id deve ser positivo"
  }
}
```

## Endereço que não existe (404)

```
GET /api/tarefass
```

```
HTTP/1.1 404 
X-Request-Id: 3e67685e-3953-4cc6-9f6c-20b9644c09b9
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:33.464732491",
  "status": 404,
  "erro": "Not Found",
  "codigo": "ROTA_NAO_ENCONTRADA",
  "mensagem": "O endereço /api/tarefass não existe nesta API",
  "caminho": "/api/tarefass"
}
```

# 3. Atualizar (PUT e PATCH)

## PUT com sucesso (200)

```
PUT /api/tarefas/2
{"titulo":"Ir ao mercado","descricao":"Comprar frutas","status":"EM_ANDAMENTO"}
```

```
HTTP/1.1 200 
X-Request-Id: 0756eed2-f0e0-4bd9-be92-9c2b8fb92a19
Content-Type: application/json

{
  "id": 2,
  "titulo": "Ir ao mercado",
  "descricao": "Comprar frutas",
  "status": "EM_ANDAMENTO",
  "prazo": null,
  "atrasada": false,
  "dataCriacao": "2026-11-20T22:10:32.666845",
  "dataAtualizacao": "2026-11-20T22:10:33.499701192"
}
```

## PUT com título de outra tarefa aberta (409)

```
PUT /api/tarefas/2
{"titulo":"Marcar dentista"}
```

```
HTTP/1.1 409 
X-Request-Id: d70df08e-db7c-4d37-968d-52f97fa40d5c
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:33.539046915",
  "status": 409,
  "erro": "Conflict",
  "codigo": "TAREFA_DUPLICADA",
  "mensagem": "Já existe uma tarefa em aberto com o título 'Marcar dentista'",
  "caminho": "/api/tarefas/2"
}
```

## PUT em id que não existe (404)

```
PUT /api/tarefas/99
{"titulo":"Não existe"}
```

```
HTTP/1.1 404 
X-Request-Id: aed344ac-03ec-4e69-b6c9-7d0f59ae2fea
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:33.556972732",
  "status": 404,
  "erro": "Not Found",
  "codigo": "TAREFA_NAO_ENCONTRADA",
  "mensagem": "Tarefa 99 não encontrada",
  "caminho": "/api/tarefas/99"
}
```

## PATCH do status (200)

```
PATCH /api/tarefas/1/status
{"status":"CONCLUIDA"}
```

```
HTTP/1.1 200 
X-Request-Id: 27c1b8f8-10ef-457c-a8bb-0b92d63a9c4d
Content-Type: application/json

{
  "id": 1,
  "titulo": "Pagar conta de luz",
  "descricao": "Vence no dia 10",
  "status": "CONCLUIDA",
  "prazo": "2026-11-25",
  "atrasada": false,
  "dataCriacao": "2026-11-20T22:10:32.585404",
  "dataAtualizacao": "2026-11-20T22:10:33.589261216"
}
```

## PATCH para o mesmo status (422)

```
PATCH /api/tarefas/1/status
{"status":"CONCLUIDA"}
```

```
HTTP/1.1 422 
X-Request-Id: 3d2a518f-29ad-4daf-9ce7-a3f7e4407a10
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:33.608608644",
  "status": 422,
  "erro": "Unprocessable Entity",
  "codigo": "REGRA_DE_NEGOCIO",
  "mensagem": "A tarefa já está com o status CONCLUIDA",
  "caminho": "/api/tarefas/1/status"
}
```

# 4. Excluir (DELETE)

## DELETE com sucesso (204)

```
DELETE /api/tarefas/5
```

```
HTTP/1.1 204 
X-Request-Id: bc4969cd-ecd7-4d92-9832-d57e8dda5c40
```

## DELETE em id que não existe (404)

```
DELETE /api/tarefas/5
```

```
HTTP/1.1 404 
X-Request-Id: abebb745-9c4b-4699-b661-eb271f99b403
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:33.68860717",
  "status": 404,
  "erro": "Not Found",
  "codigo": "TAREFA_NAO_ENCONTRADA",
  "mensagem": "Tarefa 5 não encontrada",
  "caminho": "/api/tarefas/5"
}
```

## Método não suportado (405)

```
PATCH /api/tarefas
```

```
HTTP/1.1 405 
X-Request-Id: a6ad8233-5eb8-44ca-b83a-0f8f7aa08b79
Content-Type: application/json

{
  "timestamp": "2026-11-20T22:10:33.723919345",
  "status": 405,
  "erro": "Method Not Allowed",
  "codigo": "METODO_NAO_SUPORTADO",
  "mensagem": "O método PATCH não é suportado neste endereço",
  "caminho": "/api/tarefas"
}
```

# 5. Monitoramento (Actuator)

## Saúde da aplicação (200)

```
GET /actuator/health
```

```
HTTP/1.1 200 
Content-Type: application/vnd.spring-boot.actuator.v3+json

{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "ping": {
      "status": "UP"
    },
    "ssl": {
      "status": "UP",
      "details": {
        "validChains": [],
        "invalidChains": []
      }
    }
  }
}
```

## Informações da aplicação (200)

```
GET /actuator/info
```

```
HTTP/1.1 200 
Content-Type: application/vnd.spring-boot.actuator.v3+json

{
  "app": {
    "nome": "myplan API",
    "versao": "2.0.0",
    "descricao": "API de tarefas pessoais"
  }
}
```

# 6. Log das requisições

Trecho do log da aplicação durante os testes acima:

```
2026-11-20T22:10:32.620-03:00  Tarefa 1 criada
2026-11-20T22:10:32.643-03:00  [teste-001] POST /api/tarefas -> 201 (268 ms)
2026-11-20T22:10:32.667-03:00  Tarefa 2 criada
2026-11-20T22:10:32.669-03:00  [942852d3-caaf-4dd7-92ee-97e93172a9fc] POST /api/tarefas -> 201 (10 ms)
2026-11-20T22:10:32.680-03:00  Tarefa 3 criada
2026-11-20T22:10:32.682-03:00  [f3147b78-99da-4ef3-a6a0-467845ba764d] POST /api/tarefas -> 201 (7 ms)
2026-11-20T22:10:32.693-03:00  Tarefa 4 criada
2026-11-20T22:10:32.695-03:00  [89cf992e-7f04-4f4c-9221-9f687b1404e5] POST /api/tarefas -> 201 (6 ms)
2026-11-20T22:10:32.704-03:00  Tarefa 5 criada
2026-11-20T22:10:32.705-03:00  [c043324c-6bb4-40de-94c0-fee76cd8c016] POST /api/tarefas -> 201 (4 ms)
2026-11-20T22:10:32.743-03:00  [d4ddb723-ae07-4d3f-a885-fdc106345356] POST /api/tarefas -> 400 (21 ms)
2026-11-20T22:10:32.764-03:00  [29eb8bf3-96ce-4a78-bb43-7e0071e3ca57] POST /api/tarefas -> 400 (6 ms)
```
