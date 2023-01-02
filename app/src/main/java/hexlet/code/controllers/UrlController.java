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
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final int URL_PER_PAGE = 10;


    public static Handler showUrls = ctx -> {
        LOGGER.debug("Попытка загрузить Urls");
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
        LOGGER.info("Urls выведены на экран");
    };


    public static Handler createUrl = ctx -> {
        String normalizedUrl = ctx.formParam("url");

        try {
            LOGGER.debug("Попытка нормализовать полученный URL {}", normalizedUrl);
            URL inputUrl = new URL(Objects.requireNonNull(normalizedUrl));

            normalizedUrl = inputUrl.getProtocol() + "://" + inputUrl.getAuthority();

            if (inputUrl.getPort() > 0) {
                normalizedUrl = normalizedUrl + ":" + inputUrl.getPort();
            }


            LOGGER.debug("Проверка что такого URL {} еще нет в БД", normalizedUrl);

            Url databaseUrl = new QUrl()
                    .name.equalTo(normalizedUrl)
                    .findOne();

            if (Objects.nonNull(databaseUrl)) {
                LOGGER.debug("Такой URL {} уже существует в БД", normalizedUrl);
                ctx.sessionAttribute("flash", "Ссылка уже существует");
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect("/urls");
                return;
            }

            Url url = new Url(normalizedUrl);
            url.save();

        } catch (MalformedURLException e) {
            LOGGER.debug("Не удалось нормализовать URL");
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
        LOGGER.info("URL {} добавлен в DB", normalizedUrl);
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        LOGGER.debug("Поиск URL по id {}", id);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (Objects.isNull(url)) {
            LOGGER.debug("URL не найден{}", id);
            throw new NotFoundResponse(String.format("Url with id=%d is not found", id));

        }

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
        LOGGER.info("URL c id {} выведен на экран", id);

    };

    public static Handler checkUrl = ctx -> {
        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        LOGGER.debug("Поиск URL по id {}", id);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (Objects.isNull(url)) {
            throw new NotFoundResponse(String.format("Url with id=%d is not found", id));
        }

        LOGGER.debug("Попытка провести проверку URL {}", url.getName());
        try {
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();

            int statusCode = response.getStatus();
            Document body = Jsoup.parse(response.getBody());
            String title = body.title();
            Element h1FromBody = body.selectFirst("h1");
            String h1 = Objects.nonNull(h1FromBody) ? h1FromBody.text() : "";
            Element descriptionFromBody = body.selectFirst("meta[name=description]");
            String description = Objects.nonNull(descriptionFromBody)
                    ? descriptionFromBody.attr("content") : "";

            UrlCheck checkedUrl = new UrlCheck(statusCode, title, h1, description, url);
            checkedUrl.save();

            LOGGER.info("URL {} был проверен", url);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");

        } catch (UnirestException e) {
            LOGGER.debug("Не удалось проверить URL {}", url.getName());
            ctx.sessionAttribute("flash", "Не удалось проверить страницу");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + id);
    };

}
