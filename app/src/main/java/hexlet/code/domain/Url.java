package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;


@Entity
public class Url extends Model {
    @Getter
    @Id
    private Long id;
    @Getter
    private String name;
    @Getter
    @WhenCreated
    private Instant createdAt;
}
