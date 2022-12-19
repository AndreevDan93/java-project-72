package hexlet.code;

import io.javalin.Javalin;


public class App {
    private static void addRoutes(Javalin app) {
        app.get("/", ctx -> ctx.result("hello world"));
    }

    private static Javalin getApp() {
        Javalin app = Javalin.create();

        return app;
    }

    public static void main(String[] args) {
        var app = Javalin.create(/*config*/)
                .get("/", ctx -> ctx.result("Hello World"))
                .start(5000);
    }
}
