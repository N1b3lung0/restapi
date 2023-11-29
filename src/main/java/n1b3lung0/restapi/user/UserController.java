package n1b3lung0.restapi.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
class UserController {

    private final UserRepository repository;

    private UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<User> findById(@PathVariable Long requestedId) {
        Optional<User> userOptional = repository.findById(requestedId);
        if(userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping()
    private ResponseEntity<List<User>> findAll(Pageable pageable) {
        Page<User> page = repository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "name"))
                )
        );
        return ResponseEntity.ok(page.getContent());
    }

    @PostMapping
    private ResponseEntity<Void> createUser(@RequestBody User newUserRequest, UriComponentsBuilder uriComponentsBuilder) {
        User savedUser = repository.save(newUserRequest);
        URI locationOfNewUser = uriComponentsBuilder
                .path("users/{id}")
                .buildAndExpand(savedUser.id())
                .toUri();
        return ResponseEntity.created(locationOfNewUser).build();
    }
}
