# Testes da myplan API

Testes feitos com a aplicação rodando localmente (porta 8080). Abaixo estão as requisições e as respostas que a API devolveu.

## POST: criar tarefa (201)

```
POST /api/tarefas
{"titulo":"Pagar conta de luz","descricao":"Vence dia 10","prazo":"2026-12-10"}
```

```
HTTP/1.1 201 
Location: http://localhost:8080/api/tarefas/1
Content-Type: application/json

{"id":1,"titulo":"Pagar conta de luz","descricao":"Vence dia 10","status":"PENDENTE","prazo":"2026-12-10","dataCriacao":"2026-11-20T21:43:55.276674479","dataAtualizacao":null}

```

## POST: sem título (400)

```
POST /api/tarefas
{"descricao":"faltou o título"}
```

```
HTTP/1.1 400 
Content-Type: application/json

{"timestamp":"2026-11-20T21:43:55.363083897","status":400,"erro":"Bad Request","mensagem":"Dados inválidos","caminho":"/api/tarefas","campos":{"titulo":"O título é obrigatório"}}

```

## POST: status inválido (400)

```
POST /api/tarefas
{"titulo":"Teste","status":"FEITA"}
```

```
HTTP/1.1 400 
Content-Type: application/json

{"timestamp":"2026-11-20T21:43:55.394644577","status":400,"erro":"Bad Request","mensagem":"Corpo da requisição inválido","caminho":"/api/tarefas"}

```

## GET: listar todas (200)

```
GET /api/tarefas
```

```
HTTP/1.1 200 
Content-Type: application/json

[{"id":1,"titulo":"Pagar conta de luz","descricao":"Vence dia 10","status":"PENDENTE","prazo":"2026-12-10","dataCriacao":"2026-11-20T21:43:55.276674","dataAtualizacao":null},{"id":2,"titulo":"Ir ao mercado","descricao":null,"status":"EM_ANDAMENTO","prazo":null,"dataCriacao":"2026-11-20T21:43:55.341987","dataAtualizacao":null}]

```

## GET: filtrar por status (200)

```
GET /api/tarefas?status=EM_ANDAMENTO
```

```
HTTP/1.1 200 
Content-Type: application/json

[{"id":2,"titulo":"Ir ao mercado","descricao":null,"status":"EM_ANDAMENTO","prazo":null,"dataCriacao":"2026-11-20T21:43:55.341987","dataAtualizacao":null}]

```

## GET: buscar por id (200)

```
GET /api/tarefas/1
```

```
HTTP/1.1 200 
Content-Type: application/json

{"id":1,"titulo":"Pagar conta de luz","descricao":"Vence dia 10","status":"PENDENTE","prazo":"2026-12-10","dataCriacao":"2026-11-20T21:43:55.276674","dataAtualizacao":null}

```

## GET: id que não existe (404)

```
GET /api/tarefas/99
```

```
HTTP/1.1 404 
Content-Type: application/json

{"timestamp":"2026-11-20T21:43:55.498249412","status":404,"erro":"Not Found","mensagem":"Tarefa 99 não encontrada","caminho":"/api/tarefas/99"}

```

## PUT: atualizar (200)

```
PUT /api/tarefas/1
{"titulo":"Pagar conta de luz","descricao":"Pago","status":"CONCLUIDA","prazo":"2026-12-10"}
```

```
HTTP/1.1 200 
Content-Type: application/json

{"id":1,"titulo":"Pagar conta de luz","descricao":"Pago","status":"CONCLUIDA","prazo":"2026-12-10","dataCriacao":"2026-11-20T21:43:55.276674","dataAtualizacao":"2026-11-20T21:43:55.515092137"}

```

## PUT: id que não existe (404)

```
PUT /api/tarefas/99
{"titulo":"Não existe"}
```

```
HTTP/1.1 404 
Content-Type: application/json

{"timestamp":"2026-11-20T21:43:55.538075753","status":404,"erro":"Not Found","mensagem":"Tarefa 99 não encontrada","caminho":"/api/tarefas/99"}

```

## DELETE: excluir (204)

```
DELETE /api/tarefas/2
```

```
HTTP/1.1 204 


```

## DELETE: id que não existe (404)

```
DELETE /api/tarefas/2
```

```
HTTP/1.1 404 
Content-Type: application/json

{"timestamp":"2026-11-20T21:43:55.79910712","status":404,"erro":"Not Found","mensagem":"Tarefa 2 não encontrada","caminho":"/api/tarefas/2"}

```

