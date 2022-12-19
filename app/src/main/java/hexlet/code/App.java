package hexlet.code;

import io.javalin.Javalin;


public class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5000");
        return Integer.valueOf(port);
    }

    private static void addRoutes(Javalin app) {
        app.get("/", ctx -> ctx.result("улыбнись, красотка"));
    }

    private static Javalin getApp() {
        Javalin app = Javalin.create();

        return app;
    }

    public static void main(String[] args) {
        var app = Javalin.create(/*config*/)
                .get("/", ctx -> ctx.result("Hello World"))
                .start(getPort());
    }
}
