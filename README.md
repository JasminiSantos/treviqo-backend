# Treviqo — API de viagens e despesas

Backend REST inspirado no app **Treviqo**: **viagens** (`Trip`), **despesas** (`Expense`), **destinos** (`Destination`) e **documentos** (`TripDocument`/enum `DocumentType`).

Projeto acadêmico em **Kotlin**, **Spring Boot 4.0.5** e **Java 21**, com **Gradle** (Kotlin DSL).

Aluna: Jasmini Rebecca Gomes dos Santos

## Requisitos

- **JDK 21** no `PATH` (`java -version`)

## Como executar

```bash
./gradlew bootRun
```

Para gerar o JAR e rodar:

```bash
./gradlew build
java -jar build/libs/authserver-0.0.1-SNAPSHOT.jar
```

## Stack principal

| Tecnologia | Uso |
|------------|-----|
| Spring Web MVC | REST |
| Spring Data JPA | Persistência |
| H2 (em memória) | Banco; dados reiniciados a cada execução (`ddl-auto: create-drop`) |
| Spring Security | HTTP Basic em **POST** / **PUT** / **DELETE** |
| SpringDoc OpenAPI | Swagger UI |
| Jakarta Validation | Validação de DTOs |

## Documentação interativa (Swagger)

Com o servidor em execução:

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

No Swagger, use **Authorize** com `admin` / `admin` antes de **POST**, **PUT** ou **DELETE**.

---

## Endpoints da API

**Base:** `/api/trips`

**Legenda:** *Público* = sem credenciais. *Autenticado* = HTTP Basic (`admin` / `admin`).

### Viagens (`Trip`)

| Método | Caminho | Acesso |
|--------|---------|--------|
| `GET` | `/api/trips` | Público — consulta com filtros e ordenação (query, ver abaixo) |
| `GET` | `/api/trips/{id}` | Público — detalhe com despesas, destinos e documentos |
| `POST` | `/api/trips` | Autenticado — cria viagem (`201`) |
| `PUT` | `/api/trips/{id}` | Autenticado — atualiza viagem |
| `DELETE` | `/api/trips/{id}` | Autenticado — remove viagem (`204`) |

**Query params em** `GET /api/trips` *(todos opcionais, exceto defaults de ordenação):* `titleContains`, `cityContains`, `countryContains`, `startAfter`, `startBefore`, `minBudget`, `maxBudget`, `sortBy` (default `tripStart`), `sortDirection` (default `desc`).

---

### Despesas (`Expense`)

Prefixo: `/api/trips/{tripId}/expenses`

| Método | Caminho | Acesso |
|--------|---------|--------|
| `GET` | `…/expenses` | Público — lista |
| `GET` | `…/expenses/{expenseId}` | Público — uma despesa |
| `POST` | `…/expenses` | Autenticado — adiciona à viagem (`201`, corpo `TripResponse`) |
| `PUT` | `…/expenses/{expenseId}` | Autenticado — atualiza (mesmo corpo que o POST) |
| `DELETE` | `…/expenses/{expenseId}` | Autenticado — remove (corpo `TripResponse`) |

---

### Destinos (`Destination`)

Prefixo: `/api/trips/{tripId}/destinations`

| Método | Caminho | Acesso |
|--------|---------|--------|
| `GET` | `…/destinations` | Público — lista |
| `GET` | `…/destinations/{destinationId}` | Público — um destino |
| `POST` | `…/destinations` | Autenticado — cria (`201`) |
| `PUT` | `…/destinations/{destinationId}` | Autenticado — atualiza |
| `DELETE` | `…/destinations/{destinationId}` | Autenticado — remove (`204`) |

**Regra:** o período do destino deve ficar **dentro** do intervalo da viagem.

---

### Documentos (`TripDocument`)

Prefixo: `/api/trips/{tripId}/documents` — enum **`DocumentType`:** Ticket, Hotel, Reservation, Insurance, Visa, Other.

| Método | Caminho | Acesso |
|--------|---------|--------|
| `GET` | `…/documents` | Público — lista |
| `GET` | `…/documents/{documentId}` | Público — um documento |
| `POST` | `…/documents` | Autenticado — cria (`201`) |
| `PUT` | `…/documents/{documentId}` | Autenticado — atualiza |
| `DELETE` | `…/documents/{documentId}` | Autenticado — remove (`204`) |

---

### Comportamento de `TripResponse`

Em **`GET /api/trips`**, os arrays `expenses`, `destinations` e `documents` vêm vazios e são **omitidos** no JSON quando não há itens (`JsonInclude.NON_EMPTY`). Em **`GET /api/trips/{id}`** e nas respostas que devolvem a viagem completa, esses blocos aparecem quando existirem registros.

## Testes

```bash
./gradlew test
```

Inclui testes unitários dos serviços (`TripService`, `ExpenseService`) e regras de mapeamento/validação (`TripMapperTest`), além do carregamento de contexto Spring.

## Estrutura de pacotes (principal)

- `br.pucpr.authserver.treviqo` — domínio, repositórios, serviços, controller e carga inicial de exemplo  
- `br.pucpr.authserver.security` — configuração de segurança  
- `br.pucpr.authserver.exception` — exceções e `ApiExceptionHandler`  
- `br.pucpr.authserver.config` — OpenAPI (`OpenApiConfig`)
