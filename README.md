# CP5 - Compliance, QA - Automação de Testes Funcionais (Login)

**Aluno:** Raphael Gomes Mancera (RM562279)
**Disciplina:** Java Advanced (2º semestre)

Automação, em Java + JUnit 5 + Selenium WebDriver, dos cenários de teste
funcional de Login definidos no plano de testes do CP1/CP4 (Atividade 2 —
matriz de status code de Login), executados contra o site de treino
[saucedemo.com](https://www.saucedemo.com/).

## Cenários automatizados

| Caso | Status code do plano | Cenário |
|------|----------------------|---------|
| CT1  | 200 (Sucesso)          | Login com usuário e senha válidos → redireciona para `inventory.html` |
| CT2  | 401 (Não autenticado)  | Login com senha incorreta → mensagem genérica de erro |
| CT3  | 400 (Erro de sintaxe)  | Login com campo obrigatório (senha) vazio → mensagem de campo obrigatório |
| CT4  | 403 (Sem permissão)    | Login com usuário bloqueado (`locked_out_user`) → acesso negado |

Os demais status codes do plano original (201, 204, 302, 404, 408, 409, 422,
429, 499, 500, 502, 503, 504) foram marcados como "Não se aplica" na matriz
ou dependem de comportamento de backend (timeout, erro de servidor, rate
limit) que o saucedemo.com — um site estático de treino — não permite
simular via interface. Por isso não foram automatizados.

## Pré-requisitos

- JDK 17+
- Maven 3.8+
- Google Chrome atualizado (o Selenium Manager baixa o driver automaticamente)

## Como rodar

Na pasta do projeto (`cp5-login-tests` que contém o `pom.xml`):

```bash
mvn test
```

Isso abre o Chrome, executa os 4 testes contra o saucedemo.com e gera o
relatório em `target/surefire-reports/`.

Para o **print da entrega**, rode `mvn test` no terminal e capture a tela
quando aparecer `Tests run: 4, Failures: 0, Errors: 0`.

Sem abrir a janela do navegador:

```bash
mvn test -Dheadless=true
```

## Estrutura

```
cp5-login-tests/
├── pom.xml
├── README.md
└── src/test/java/Login.java
```

## Entrega

- Repositório público no GitHub com este código.
- Print da execução local (`mvn test`) com os 4 testes passando.
