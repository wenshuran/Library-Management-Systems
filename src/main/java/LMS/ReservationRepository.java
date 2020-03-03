package LMS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query(value = "select * from reservations where artifact_id = ?1", nativeQuery = true)
    List<Reservation> findByArtifact_id(Long artifact_id);
}
