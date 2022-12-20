package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.Getter;

import javax.persistence.*;
import java.time.Instant;

@Entity
public final class UrlCheck extends Model {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Getter
    private int statusCode;
    @Getter
    private String title;
    @Getter
    private String h1;
    @Getter
    @Lob
    private String description;
    @Getter
    @ManyToOne
    private Url url;
    @Getter
    @WhenCreated
    private Instant createdAt;

    public UrlCheck(int statusCode, String title, String h1, String description, Url url) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.url = url;
    }
}
