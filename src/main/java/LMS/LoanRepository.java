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
public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Query(value = "select * from loans where user_id = ?1", nativeQuery = true)
    List<Loan> findByUser_id(Long user_id);
    Optional<Loan> findById(Long id);
    @Query(value = "select * from loans where artifact_id = ?1", nativeQuery = true)
    List<Loan> findByArtifact_id(Long artifact_id);

    @Transactional
    @Modifying
    @Query(value = "update loans set due_date=?2 where id=?1",nativeQuery = true) //TODO
    void updateDueDate(long id, Date date);

}
