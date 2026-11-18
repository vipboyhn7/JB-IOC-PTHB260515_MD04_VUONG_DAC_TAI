package vn.edu.course.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor()
@Entity
@Table(name = "course_reservations", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    public Reservation(Long studentId, Long courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }
}
