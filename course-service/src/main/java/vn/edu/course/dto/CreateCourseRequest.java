package vn.edu.course.dto;

import jakarta.validation.constraints.*;

public record CreateCourseRequest(
        @NotBlank(message = "Tên khóa học không được để trống") @Size(max = 255) String courseName,
        @NotNull(message = "capacity là bắt buộc") @PositiveOrZero(message = "capacity không được âm") Integer capacity,
        @NotNull(message = "credits là bắt buộc") @Positive(message = "credits phải lớn hơn 0") Integer credits) {
}
