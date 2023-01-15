package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Log4j2
public class UrlController {
    private static final int URL_PER_PAGE = 10;


    public static Handler showUrls = ctx -> {
        log.debug("Попытка загрузить Urls");
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
        log.debug("Urls выведены на экран");
    };


    public static Handler createUrl = ctx -> {
        String normalizedUrl = ctx.formParam("url");

        try {
            log.debug("Попытка нормализовать полученный URL {}", normalizedUrl);
            URL inputUrl = new URL(Objects.requireNonNull(normalizedUrl));

            normalizedUrl = inputUrl.getProtocol() + "://" + inputUrl.getAuthority();

            log.debug("Проверка что такого URL {} еще нет в БД", normalizedUrl);

            Url databaseUrl = new QUrl()
                    .name.equalTo(normalizedUrl)
                    .findOne();

            if (Objects.nonNull(databaseUrl)) {
                log.debug("Такой URL {} уже существует в БД", normalizedUrl);
                ctx.sessionAttribute("flash", "Ссылка уже существует");
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect("/urls");
                return;
            }

            Url url = new Url(normalizedUrl);
            url.save();

            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
            log.debug("URL {} добавлен в DB", normalizedUrl);

        } catch (MalformedURLException e) {
            log.debug("Не удалось нормализовать URL");
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }


        ctx.redirect("/urls");

    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).get();

        log.debug("Поиск URL по id {}", id);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (Objects.isNull(url)) {
            log.debug("URL не найден{}", id);
            throw new NotFoundResponse(String.format("Url with id=%d is not found", id));

        }

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
        log.debug("URL c id {} выведен на экран", id);

    };

    public static Handler checkUrl = ctx -> {
        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        log.debug("Поиск URL по id {}", id);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (Objects.isNull(url)) {
            throw new NotFoundResponse(String.format("Url with id=%d is not found", id));
        }

        log.debug("Попытка провести проверку URL {}", url.getName());
        try {
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();
            String content = response.getBody();

            Document body = Jsoup.parse(content);
            int statusCode = response.getStatus();
            String title = body.title();

//            String h1 = body.selectFirst("h1") != null
//                    ? Objects.requireNonNull(body.selectFirst("h1")).text()
//                    : "";

            String description = body.selectFirst("meta[name=description]") != null
                    ? Objects.requireNonNull(body.selectFirst("meta[name=description]")).attr("content")
                    : "";

//            UrlCheck checkedUrl = new UrlCheck(statusCode, title, h1, description, url);
            UrlCheck checkedUrl = new UrlCheck(statusCode, title, description, url);
            checkedUrl.save();

            log.debug("URL {} был проверен", url);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");

        } catch (UnirestException e) {
            log.debug("Не удалось проверить URL {}", url.getName());
            ctx.sessionAttribute("flash", "Не удалось проверить страницу");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + id);
    };

}
