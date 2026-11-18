package vn.edu.student.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import vn.edu.student.dto.CourseInfo;
import vn.edu.student.dto.ReservationRequest;

@FeignClient(name = "course-service")
public interface CourseClient {
    @GetMapping("/api/courses/{id}")
    CourseInfo getCourse(@PathVariable("id") Long id);

    @PostMapping("/internal/courses/{id}/reservations")
    CourseInfo reserve(@PathVariable("id") Long id, @RequestBody ReservationRequest request);
}
