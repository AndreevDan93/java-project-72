package hexlet.code.config;

import hexlet.code.controllers.UrlController;
import hexlet.code.controllers.WelcomeController;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import lombok.extern.log4j.Log4j2;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

@Log4j2
public class JavalinConfig {
    private static final String DEFAULT_PORT = "3000";
    private static final String ENV_DEV = "development";
    private static final String ENV_PROD = "production";

    public static int getPort() {
        String port = System.getenv().getOrDefault("PORT", DEFAULT_PORT);
        log.info("server port is" + port);
        return Integer.parseInt(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", ENV_DEV);
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateEngine.addTemplateResolver(templateResolver);

        return templateEngine;
    }

    private static boolean isProduction() {
        return getMode().equals(ENV_PROD);
    }

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


    public static Javalin setup() {
        log.info("{}", System.getenv().getOrDefault("APP_ENV", "development"));

        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.enableDevLogging();
            }
            config.enableWebjars();
            JavalinThymeleaf.configure(getTemplateEngine());
        });


        addRoutes(app);

        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }
}
