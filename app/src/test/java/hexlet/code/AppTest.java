package hexlet.code;

import hexlet.code.domain.Url;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;

import static hexlet.code.App.getApp;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public final class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static final String EXPECTED_URL = "https://www.example.com";
    private static final String CORRECT_URL = "https://ru.hexlet.io";
    private static final String INCORRECT_URL = "www.rock.ru";

    @BeforeAll
    public static void beforeAll() {
        app = getApp();
        app.start();
        baseUrl = "http://localhost:" + app.port();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    public void beforeEach() {
        Database db = DB.getDefault();
        db.script().run("/truncate.sql");
        Url url = new Url(EXPECTED_URL);
        url.save();
    }

    @Test
    public void testWelcome() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testListUrls() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains(EXPECTED_URL);
    }

    @Test
    public void testShowUrlId() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();

        String body = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains(EXPECTED_URL);

        response = Unirest.get(baseUrl + "/urls/2").asString();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testNewUrl() {
        HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                .field("url", CORRECT_URL)
                .asString();
        assertThat(response.getStatus()).isEqualTo(302);

        response = Unirest.get(baseUrl + "/urls").asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Страница успешно добавлена");
        assertThat(response.getBody()).contains(CORRECT_URL);
    }

    @Test
    public void testNewIncorrectUrl() {
        HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                .field("url", INCORRECT_URL)
                .asString();
        assertThat(response.getStatus()).isEqualTo(302);

        response = Unirest.get(baseUrl).asString();
        assertThat(response.getBody()).contains("Некорректный URL");

        response = Unirest.get(baseUrl + "/urls").asString();
        assertThat(response.getBody()).doesNotContain(INCORRECT_URL);
    }

    @Test
    public void testUrlCheck() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains(EXPECTED_URL);

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls/1/checks")
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);

        response = Unirest.get(baseUrl + "/urls/1").asString();
        assertThat(response.getBody()).contains("Страница успешно проверена");
        assertThat(response.getBody())
                .contains("Example Domain");

    }

}
