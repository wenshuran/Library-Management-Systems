package LMS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndPassword(String email, String password);
    List<User> findByNameLike(String name);
    Optional<User> findById(long id);

    @Query(value = "select * from users", nativeQuery = true)
    List<User> findAll();

    @Query(value = "select * from users where email = ?1 and password = ?2",nativeQuery = true)
    List<User> findUser(String email, String password);

    @Transactional
    @Modifying
    @Query(value = "update users set name=?2 where id=?1",nativeQuery = true) //TODO
    void updateUser(long id, String name);
}
