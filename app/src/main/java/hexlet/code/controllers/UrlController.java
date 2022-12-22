package hexlet.code.controllers;

import hexlet.code.App;
import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;


public class UrlController {
    private static final Logger URL_CONTROLLER_LOGGER = LoggerFactory.getLogger(App.class);
    private static final int URL_PER_PAGE = 10;

    @Getter
    private static Handler showUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int offset = page * URL_PER_PAGE - URL_PER_PAGE;

        PagedList<Url> pagedUrl = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(URL_PER_PAGE)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrl.getList();
        int currentPage = pagedUrl.getPageIndex() + 1;
        int lastPage = pagedUrl.getTotalPageCount() + 1;

        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .toList();

        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.attribute("urls", urls);

        ctx.render("urls/showUrls.html");
    };

    @Getter
    private static Handler createUrl = ctx -> {
        String normalizedUrl = getNormalizedUrl(ctx.formParam("url"));

        URL_CONTROLLER_LOGGER.info("Попытка нормализовать URL {} получить корретный URL", normalizedUrl);

        if (Objects.isNull(normalizedUrl)) {
            URL_CONTROLLER_LOGGER.info("Не удалось нормализовать URL");
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        URL_CONTROLLER_LOGGER.info("Удалось нормализовать URL {}", normalizedUrl);

        URL_CONTROLLER_LOGGER.info("Проверка что такого URL {} еще нет в БД", normalizedUrl);

        Url databaseUrl = new QUrl()
                .name.equalTo(normalizedUrl)
                .findOne();

        if (Objects.nonNull(databaseUrl)) {
            URL_CONTROLLER_LOGGER.info("Такой URL {} уже существует в БД", normalizedUrl);
            ctx.sessionAttribute("flash", "Ссылка уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        URL_CONTROLLER_LOGGER.info("URL {} добавлен", normalizedUrl);

        Url url = new Url(normalizedUrl);
        url.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };
    @Getter
    private static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        URL_CONTROLLER_LOGGER.info("Поиск URL по id {}", id);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (Objects.isNull(url)) {
            URL_CONTROLLER_LOGGER.info("URL не найден{}", id);
            throw new NotFoundResponse();

        }

        URL_CONTROLLER_LOGGER.info("URL найден {}", id);

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };
    @Getter
    private static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        URL_CONTROLLER_LOGGER.info("Поиск URL по id {}", id);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (Objects.isNull(url)) {
            URL_CONTROLLER_LOGGER.info("URL не найден {}", id);
            throw new NotFoundResponse();
        }

        URL_CONTROLLER_LOGGER.info("URL найден {}", id);

        URL_CONTROLLER_LOGGER.info("Попытка провести проверку URL {}", url.getName());
        try {
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();

            int statusCode = response.getStatus();
            Document body = Jsoup.parse(response.getBody());
            String title = body.title();
            Element h1FromBody = body.selectFirst("h1");
            String h1 = Objects.nonNull(h1FromBody) ? h1FromBody.text() : null;
            Element descriptionFromBody = body.selectFirst("meta[name=description]");
            String description = Objects.nonNull(descriptionFromBody)
                    ? descriptionFromBody.attr("content") : null;

            UrlCheck checkedUrl = new UrlCheck(statusCode, title, h1, description, url);
            URL_CONTROLLER_LOGGER.info("URL {} был проверен", url);

            checkedUrl.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");

        } catch (UnirestException e) {
            URL_CONTROLLER_LOGGER.info("Не удалось проверить URL {}", url.getName());
            ctx.sessionAttribute("flash", "Не удалось проверить страницу");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + id);
    };

    private static String getNormalizedUrl(String url) {
        try {
            URL_CONTROLLER_LOGGER.info("Попытка нормализовать полученный URL {}", url);
            URL inputURL = new URL(url);

            String normalizedUrl = String.format("%s://%s", inputURL.getProtocol(), inputURL.getHost());

            if (inputURL.getPort() > 0) {
                normalizedUrl = normalizedUrl + ":" + inputURL.getPort();
            }

            URL_CONTROLLER_LOGGER.info("Получен URL {}", normalizedUrl);

            return normalizedUrl;

        } catch (MalformedURLException e) {
            return null;
        }
    }
}
