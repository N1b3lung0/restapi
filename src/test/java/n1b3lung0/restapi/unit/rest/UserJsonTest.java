package n1b3lung0.restapi.unit.rest;

import n1b3lung0.restapi.user.User;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserJsonTest {

    @Autowired
    private JacksonTester<User> json;

    @Autowired
    private JacksonTester<User[]> jsonList;

    private User[] users;

    @BeforeEach
    void setUp() {
        users = Arrays.array(
                new User(100L, "Carlos", "admin"),
                new User(101L, "Luis", "admin"),
                new User(102L, "Andrés", "admin")
        );
    }

    @Test
    void userSerializationTest() throws IOException {
        User user = users[0];
        assertThat(json.write(user)).isStrictlyEqualToJson("singleUser.json");
        assertThat(json.write(user)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(user)).extractingJsonPathNumberValue("@.id").isEqualTo(100);
        assertThat(json.write(user)).hasJsonPathStringValue("@.name");
        assertThat(json.write(user)).extractingJsonPathStringValue("@.name").isEqualTo("Carlos");
    }

    @Test
    void userDeserializationTest() throws IOException {
        String expected = """
                {
                    "id": 100,
                    "name": "Carlos",
                    "owner": "admin"
                }
                """;
        assertThat(json.parse(expected)).isEqualTo(new User(100L, "Carlos", "admin"));
        assertThat(json.parseObject(expected).id()).isEqualTo(100);
        assertThat(json.parseObject(expected).name()).isEqualTo("Carlos");
        assertThat(json.parseObject(expected).owner()).isEqualTo("admin");
    }

    @Test
    void userListSerializationTest() throws IOException {
        assertThat(jsonList.write(users)).isStrictlyEqualToJson("listUsers.json");
    }

    @Test
    void userListDeserializationTest() throws IOException {
        String expected="""
         [
            { "id": 100, "name": "Carlos", "owner": "admin" },
            { "id": 101, "name": "Luis", "owner": "admin" },
            { "id": 102, "name": "Andrés", "owner": "admin" }
         ]
         """;
        assertThat(jsonList.parse(expected)).isEqualTo(users);
    }

}
