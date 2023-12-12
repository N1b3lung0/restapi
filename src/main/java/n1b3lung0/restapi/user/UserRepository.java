package n1b3lung0.restapi.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface UserRepository extends CrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {
    User findByIdAndOwner(Long id, String owner);
    Page<User> findByOwner(String owner, PageRequest pageRequest);
}
