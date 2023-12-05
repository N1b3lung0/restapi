package n1b3lung0.restapi.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "USERS")
public record User(@Id Long id, String name, String owner) {
}
