import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CheckPoint 5 - Compliance, QA - Automacao de Testes Funcionais
 * Aluno: Raphael Gomes Mancera (RM562279)
 *
 * Automatiza, em saucedemo.com, os cenarios ( check ) definidos no plano de
 * testes de Login do CP1/CP4 (Atividade 2 - matriz de status code):
 *   CT1 -> 200 (Sucesso)          | credenciais validas
 *   CT2 -> 401 (Nao autenticado)  | senha incorreta
 *   CT3 -> 400 (Erro de sintaxe)  | campo obrigatorio vazio
 *   CT4 -> 403 (Sem permissao)    | usuario bloqueado (locked_out_user)
 *
 * Os demais status codes do plano (201, 204, 302, 404, 408, 409, 422, 429,
 * 499, 500, 502, 503, 504) foram marcados como "Nao se aplica" na matriz
 * original ou dependem de um backend real (timeouts, erros de servidor,
 * rate limit), o que o saucedemo.com - um site estatico de treino - nao
 * permite simular via UI. Por isso nao sao automatizados aqui.
 */
@DisplayName("CheckPoint 5 - Login")
public class Login {

    private static final String BASE_URL = "https://www.saucedemo.com/";

    // Massa de dados
    private static final String USUARIO_VALIDO = "standard_user";
    private static final String USUARIO_BLOQUEADO = "locked_out_user";
    private static final String SENHA_VALIDA = "secret_sauce";
    private static final String SENHA_INVALIDA = "senha_incorreta_123";

    // Locators (identificados via DevTools - id e o atributo mais estavel)
    private static final By CAMPO_USUARIO = By.id("user-name");
    private static final By CAMPO_SENHA = By.id("password");
    private static final By BOTAO_LOGIN = By.id("login-button");
    private static final By ICONE_CARRINHO = By.cssSelector("#shopping_cart_container a");
    private static final By MENSAGEM_ERRO = By.cssSelector("h3[data-test='error']");

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void abrirNavegador() {
        ChromeOptions options = new ChromeOptions();
        // Headless se -Dheadless=true (ou CI). Para o print da entrega, rode sem essa flag.
        if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void fecharNavegador() {
        if (driver != null) driver.quit();
    }

    // ------------------------------------------------------------------
    // CT1 - 200 (Sucesso)
    // Dado que o usuario informou e-mail/senha validos e cadastrados,
    // Quando ele solicita o login,
    // Entao o sistema autentica e redireciona para a area logada.
    // ------------------------------------------------------------------
    @Test
    @DisplayName("CT1 - Login com sucesso (200)")
    void deveLogarComCredenciaisValidas() {
        // Dado: que esteja na pagina saucedemo.com
        driver.get(BASE_URL);
        assertEquals(BASE_URL, driver.getCurrentUrl());
        assertEquals("Swag Labs", driver.getTitle());

        // Quando: inserir usuario e senha validos
        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);

        // E: clicar no botao "Login"
        driver.findElement(BOTAO_LOGIN).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        // Entao: redirecionado para inventory.html, com sessao ativa (icone do carrinho visivel)
        assertEquals(BASE_URL + "inventory.html", driver.getCurrentUrl());
        assertTrue(driver.findElement(ICONE_CARRINHO).isDisplayed());
    }

    // ------------------------------------------------------------------
    // CT2 - 401 (Nao autenticado)
    // Dado que o usuario informou e-mail ou senha incorretos,
    // Quando ele solicita o login,
    // Entao o sistema nao autentica e retorna mensagem generica de erro
    // (sem revelar qual dos dois campos esta errado).
    // ------------------------------------------------------------------
    @Test
    @DisplayName("CT2 - Login com senha incorreta (401)")
    void naoDeveLogarComSenhaIncorreta() {
        // Dado: que esteja na pagina saucedemo.com
        driver.get(BASE_URL);

        // Quando: inserir usuario valido e senha invalida
        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_INVALIDA);

        // E: clicar no botao "Login"
        driver.findElement(BOTAO_LOGIN).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(MENSAGEM_ERRO));

        // Entao: permanece na tela de login e exibe mensagem generica de credenciais invalidas
        assertEquals(BASE_URL, driver.getCurrentUrl());
        String mensagem = driver.findElement(MENSAGEM_ERRO).getText();
        assertTrue(mensagem.contains("do not match any user"),
                "Mensagem de erro esperada nao encontrada: " + mensagem);
    }

    // ------------------------------------------------------------------
    // CT3 - 400 (Erro de sintaxe)
    // Dado que o usuario nao enviou um campo obrigatorio (senha em branco),
    // Quando ele solicita o login,
    // Entao o sistema retorna mensagem indicando o campo invalido/obrigatorio.
    // ------------------------------------------------------------------
    @Test
    @DisplayName("CT3 - Login com campo obrigatorio vazio (400)")
    void naoDeveLogarComCampoObrigatorioVazio() {
        // Dado: que esteja na pagina saucedemo.com
        driver.get(BASE_URL);

        // Quando: inserir apenas o usuario, deixando a senha em branco
        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);

        // E: clicar no botao "Login" sem preencher a senha
        driver.findElement(BOTAO_LOGIN).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(MENSAGEM_ERRO));

        // Entao: sistema indica o campo obrigatorio ausente
        String mensagem = driver.findElement(MENSAGEM_ERRO).getText();
        assertTrue(mensagem.contains("Password is required"),
                "Mensagem de erro esperada nao encontrada: " + mensagem);
    }

    // ------------------------------------------------------------------
    // CT4 - 403 (Sem permissao)
    // Dado que o usuario informou credenciais corretas, porem sua conta
    // esta bloqueada/suspensa,
    // Quando ele solicita o login,
    // Entao o sistema retorna 403 informando que o acesso nao e permitido.
    // ------------------------------------------------------------------
    @Test
    @DisplayName("CT4 - Login com usuario bloqueado (403)")
    void naoDeveLogarComUsuarioBloqueado() {
        // Dado: que esteja na pagina saucedemo.com
        driver.get(BASE_URL);

        // Quando: inserir um usuario com conta bloqueada e senha valida
        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_BLOQUEADO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);

        // E: clicar no botao "Login"
        driver.findElement(BOTAO_LOGIN).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(MENSAGEM_ERRO));

        // Entao: acesso negado, com mensagem informando o bloqueio
        String mensagem = driver.findElement(MENSAGEM_ERRO).getText();
        assertTrue(mensagem.contains("has been locked out"),
                "Mensagem de erro esperada nao encontrada: " + mensagem);
    }
}
