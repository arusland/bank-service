package io.arusland.money.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import io.arusland.money.service.AccountService;
import io.javalin.Javalin;
import org.junit.After;
import org.junit.Before;

public abstract class BaseTest {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static int currentPort = 7000;
    private Javalin app;
    private String origin;

    @Before
    public void beforeTest() {
        AccountService accountService = new AccountService();
        ServiceController controller = new ServiceController(accountService);
        controller.setPort(currentPort++);
        controller.run();

        app = controller.getServer();
        origin = "http://localhost:" + app.port();
    }

    @After
    public void afterTest() {
        app.stop();
    }

    protected GetRequest apiGet(String path) {
        return Unirest.get(origin + path);
    }

    protected <T> T apiPost(String path, Class<T> clazz) throws UnirestException {
        JsonNode resp = Unirest.post(origin + path).asJson().getBody();

        return gson.fromJson(resp.toString(), clazz);
    }

    protected HttpResponse<JsonNode> apiPost(String path, String body) throws UnirestException {
        return Unirest.post(origin + path)
                .header("accept", "application/json")
                .body(body)
                .asJson();
    }

    protected <T> T apiPostAsObject(String path, String body, Class<T> clazz) throws UnirestException {
        JsonNode resp = apiPost(path, body).getBody();

        return gson.fromJson(resp.toString(), clazz);
    }

    protected <T> T apiGetAsObject(String path, Class<T> clazz) throws UnirestException {
        HttpResponse<String> resp = apiGet(path).asString();

        return gson.fromJson(resp.getBody(), clazz);
    }

    protected String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
