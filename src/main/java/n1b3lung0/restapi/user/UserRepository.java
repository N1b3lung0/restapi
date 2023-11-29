package n1b3lung0.restapi.user;

import org.springframework.data.repository.CrudRepository;

interface UserRepository extends CrudRepository<User, Long> {
}
