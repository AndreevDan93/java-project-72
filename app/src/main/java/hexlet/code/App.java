package hexlet.code;

import hexlet.code.config.JavalinConfig;
import io.javalin.Javalin;


public class App {


    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(JavalinConfig.getPort());
    }

    public static Javalin getApp() {
        return JavalinConfig.setup();
    }

}
