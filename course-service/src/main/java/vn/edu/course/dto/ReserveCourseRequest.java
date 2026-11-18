package vn.edu.course.dto;

import jakarta.validation.constraints.*;

public record ReserveCourseRequest(
        @NotNull(message = "studentId là bắt buộc") @Positive(message = "studentId phải lớn hơn 0") Long studentId) {
}
