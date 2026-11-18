package vn.edu.course.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.course.dto.ReserveCourseRequest;
import vn.edu.course.entity.Course;
import vn.edu.course.service.CourseService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/courses")
public class ReservationController {
    private final CourseService service;

    @PostMapping("/{id}/reservations")
    public Course reserve(@PathVariable Long id, @Valid @RequestBody ReserveCourseRequest request) {
        return service.reserve(id, request.studentId());
    }
}
