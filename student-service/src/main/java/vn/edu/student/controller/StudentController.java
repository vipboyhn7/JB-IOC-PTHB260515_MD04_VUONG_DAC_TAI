package vn.edu.student.controller;

import vn.edu.student.entity.Student;
import vn.edu.student.service.StudentService;
import vn.edu.student.dto.*;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService service;

    @PostMapping
    public ResponseEntity<Student> create(@Valid @RequestBody CreateStudentRequest request) {
        Student saved = service.create(request);
        return ResponseEntity.created(URI.create("/api/students/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<Student> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Student get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/enroll")
    public Student enroll(@Valid @RequestBody EnrollmentRequest request) {
        return service.enroll(request.studentId(), request.courseId());
    }
}
