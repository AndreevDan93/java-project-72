package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class Url extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @WhenCreated
    private Instant createdAt;
    @OneToMany(cascade = CascadeType.ALL)
    private List<UrlCheck> urlChecks;
    public Url(String name) {
        this.name = name;
    }
}
