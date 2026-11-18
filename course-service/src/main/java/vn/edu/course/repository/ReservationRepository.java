package vn.edu.course.repository;

import vn.edu.course.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
