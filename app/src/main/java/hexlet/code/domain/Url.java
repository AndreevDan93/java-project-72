package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.Getter;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;


@Entity
public final class Url extends Model {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Getter
    private String name;
    @Getter
    @WhenCreated
    private Instant createdAt;
    @Getter
    @OneToMany(cascade = CascadeType.ALL)
    private List<UrlCheck> urlChecks;

    public Url(String name) {
        this.name = name;
    }

}
