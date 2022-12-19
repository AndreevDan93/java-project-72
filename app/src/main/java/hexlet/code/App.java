package hexlet.code;

import hexlet.code.controllers.WelcomeController;
import io.javalin.Javalin;


public class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5000");
        return Integer.valueOf(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static void addRoutes(Javalin app) {
        app.get("/", ctx -> ctx.result("Welcome"));
    }

    private static Javalin getApp() {
        Javalin app = Javalin.create();

        return app;
    }

    public static void main(String[] args) {
        var app = getApp();
        addRoutes(app);
        app.start(getPort());
    }
}
