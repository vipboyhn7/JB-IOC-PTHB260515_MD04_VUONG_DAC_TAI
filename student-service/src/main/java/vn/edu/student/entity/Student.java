package vn.edu.student.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Arrays;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "enrolled_courses", nullable = false, columnDefinition = "bigint[]")
    private Long[] enrolledCourses = new Long[0];

    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long[] getEnrolledCourses() {
        return enrolledCourses.clone();
    }

    public boolean isEnrolled(Long courseId) {
        return Arrays.asList(enrolledCourses).contains(courseId);
    }

    public void enroll(Long courseId) {
        Long[] next = Arrays.copyOf(enrolledCourses, enrolledCourses.length + 1);
        next[next.length - 1] = courseId;
        enrolledCourses = next;
    }
}
