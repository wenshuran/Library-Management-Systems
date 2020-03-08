package LMS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtifactRepository extends JpaRepository<Artifact, Long> {
    List<Artifact> findByNameLike(String name);
    Optional<Artifact> findByName(String name);
    @Query(value = "select * from artifacts where type = ?1", nativeQuery = true)
    List<Artifact> findByType(String type);

    @Query(value = "select * from artifacts", nativeQuery = true)
    List<Artifact> findAll();

    @Transactional
    @Modifying
    @Query(value = "update artifacts set is_copy=true where id=?1",nativeQuery = true) //TODO
    void makeCopy(long id);

    @Transactional
    @Modifying
    @Query(value = "delete from artifacts where id=?1", nativeQuery = true)
    void deleteById(long id);
}
