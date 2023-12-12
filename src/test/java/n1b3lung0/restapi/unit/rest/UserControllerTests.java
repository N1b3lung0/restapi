package n1b3lung0.restapi.unit.rest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import n1b3lung0.restapi.user.User;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnAUserWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "abc123")
                .getForEntity("/users/100", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(100);

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("Carlos");
    }

    @Test
    void shouldNotReturnAUserWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "abc123")
                .getForEntity("/users/99", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    @DirtiesContext
    void shouldCreateANewUser() {
        User newUSer = new User(null, "Carlos", null);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("admin", "abc123")
                .postForEntity("/users", newUSer, Void.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewUser = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("admin", "abc123")
                .getForEntity(locationOfNewUser, String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String name = documentContext.read("$.name");

        assertThat(id).isNotNull();
        assertThat(name).isEqualTo("Carlos");
    }

    @Test
    void shouldReturnAllUsersWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "abc123")
                .getForEntity("/users", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int userCount = documentContext.read("$.length()");
        assertThat(userCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(100, 101, 102);

        JSONArray names = documentContext.read("$..name");
        assertThat(names).containsExactlyInAnyOrder("Carlos", "Luis", "Andrés");
    }

    @Test
    void shouldReturnAPageOfUsers() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "abc123")
                .getForEntity("/users?page=0&size=1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfUsers() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "abc123")
                .getForEntity("/users?page=0&size=1&sort=name,asc", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);

        String name = documentContext.read("$[0].name");
        assertThat(name).isEqualTo("Andrés");
    }

    @Test
    void shouldReturnASortedPageOfUsersWithNoParametersAndUseDefaultValues() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "abc123")
                .getForEntity("/users", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(3);

        JSONArray names = documentContext.read("$..name");
        assertThat(names).containsExactly("Andrés", "Carlos", "Luis");
    }
    @Test
    void shouldNotReturnAUserWhenUsingBadCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("BAD-USER", "abc123")
                .getForEntity("/users/100", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .withBasicAuth("admin", "BAD-PASSWORD")
                .getForEntity("/users/100", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUsersWhoAreNotAdminOwns() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("non-admin", "password")
                .getForEntity("/users/100", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldNotAllowAccessToUsersTheyDoNotOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "abc123")
                .getForEntity("/users/103", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldUpdateAnExistingUser() {
        User userUpdate = new User(null, "Tomás", null);
        HttpEntity<User> request = new HttpEntity<>(userUpdate);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("admin", "abc123")
                .exchange("/users/100", HttpMethod.PUT, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("admin", "abc123")
                .getForEntity("/users/100", String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String name = documentContext.read("$.name");

        assertThat(id).isEqualTo(100);
        assertThat(name).isEqualTo("Tomás");
    }

    @Test
    void shouldNotUpdateAUserThatDoesNotExist() {
        User unknownUser = new User(null, "Tomás", null);
        HttpEntity<User> request = new HttpEntity<>(unknownUser);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("admin", "abc123")
                .exchange("/users/99999", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotUpdateAUserThatIsOwnedBySomeoneElse() {
        User adminUser = new User(null, "Tomás", null);
        HttpEntity<User> request = new HttpEntity<>(adminUser);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("admin", "abc123")
                .exchange("/users/103", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
