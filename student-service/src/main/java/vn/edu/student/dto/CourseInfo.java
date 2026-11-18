package vn.edu.student.dto;

import jakarta.validation.constraints.*;

public record CourseInfo(Long id, String courseName, Integer capacity, Integer credits) {
}
