package LMS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query(value = "select * from feedback", nativeQuery = true)
    List<Feedback> findAll();
}
