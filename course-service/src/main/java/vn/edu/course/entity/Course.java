package vn.edu.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "course_name", nullable = false)
    private String courseName;
    @Column(nullable = false)
    private Integer capacity;
    @Column(nullable = false)
    private Integer credits;

    public Course(String courseName, Integer capacity, Integer credits) {
        this.courseName = courseName;
        this.capacity = capacity;
        this.credits = credits;
    }

    public void reserveSeat() {
        capacity--;
    }
}
