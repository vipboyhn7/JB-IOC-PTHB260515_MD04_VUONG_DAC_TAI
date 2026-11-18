package vn.edu.student.dto;

import jakarta.validation.constraints.*;

public record EnrollmentRequest(
        @NotNull(message = "studentId là bắt buộc") @Positive(message = "studentId phải lớn hơn 0") Long studentId,
        @NotNull(message = "courseId là bắt buộc") @Positive(message = "courseId phải lớn hơn 0") Long courseId) {
}
