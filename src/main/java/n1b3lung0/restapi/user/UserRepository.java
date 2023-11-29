package n1b3lung0.restapi.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface UserRepository extends CrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {
}
