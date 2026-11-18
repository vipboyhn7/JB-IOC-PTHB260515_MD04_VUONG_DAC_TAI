package vn.edu.course.controller;

import vn.edu.course.entity.Course;
import vn.edu.course.service.CourseService;
import vn.edu.course.dto.*;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService service;

    @PostMapping
    public ResponseEntity<Course> create(@Valid @RequestBody CreateCourseRequest request) {
        Course saved = service.create(request);
        return ResponseEntity.created(URI.create("/api/courses/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<Course> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Course get(@PathVariable Long id) {
        return service.get(id);
    }

}
