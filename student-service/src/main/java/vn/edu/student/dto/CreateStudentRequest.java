package vn.edu.student.dto;

import jakarta.validation.constraints.*;

public record CreateStudentRequest(
        @NotBlank(message = "Tên sinh viên không được để trống") @Size(max = 255) String name,
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng") @Size(max = 255) String email) {
}
