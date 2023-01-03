package hexlet.code.config;

import hexlet.code.controllers.UrlController;
import hexlet.code.controllers.WelcomeController;
import io.javalin.Javalin;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Router {
    public static void addRoutes(Javalin app) {
        app.get("/", WelcomeController.welcome);

        app.routes(() -> {
            path("urls", () -> {
                post(UrlController.createUrl);
                get(UrlController.showUrls);
                path("{id}", () -> {
                    get(UrlController.showUrl);
                    post("/checks", UrlController.checkUrl);
                });
            });
        });
    }
}
