package vn.edu.student.service;

import feign.FeignException;

import java.util.List;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.student.client.CourseClient;
import vn.edu.student.dto.*;
import vn.edu.student.entity.Student;
import vn.edu.student.exception.BusinessException;
import vn.edu.student.repository.StudentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository students;
    private final CourseClient courses;

    @Transactional
    public Student create(CreateStudentRequest request) {
        Student saved = students.saveAndFlush(new Student(request.name().strip(), request.email().strip().toLowerCase(Locale.ROOT)));
        log.info("Student created studentId={}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Student> list() {
        List<Student> result = students.findAll();
        log.debug("Students fetched count={}", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public Student get(Long id) {
        log.debug("Fetching student studentId={}", id);
        return students.findById(id).orElseThrow(() -> missingStudent(id));
    }

    @Transactional
    public Student enroll(Long studentId, Long courseId) {
        log.info("Enrollment started studentId={} courseId={}", studentId, courseId);
        Student student = students.findForUpdate(studentId).orElseThrow(() -> missingStudent(studentId));
        if (student.isEnrolled(courseId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "ALREADY_ENROLLED",
                    "Sinh viên " + studentId + " đã đăng ký khóa học " + courseId);
        }
        try {
            log.info("Checking course via OpenFeign courseId={}", courseId);
            courses.getCourse(courseId);
            // Reserve checks capacity under a DB lock. Idempotent keys make timeout retries safe.
            log.info("Reserving seat via OpenFeign studentId={} courseId={}", studentId, courseId);
            courses.reserve(courseId, new ReservationRequest(studentId));
        } catch (FeignException ex) {
            log.warn("Course request failed studentId={} courseId={} upstreamStatus={}", studentId, courseId, ex.status());
            if (ex.status() == 404)
                throw new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học " + courseId);
            if (ex.status() == 409)
                throw new BusinessException(HttpStatus.CONFLICT, "COURSE_FULL", "Khóa học " + courseId + " đã hết chỗ");
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "COURSE_SERVICE_UNAVAILABLE",
                    "Không thể liên lạc Course Service. Hãy thử lại cùng studentId và courseId.", ex);
        }
        student.enroll(courseId);
        Student saved = students.saveAndFlush(student);
        // Transaction commits after method return; this log records persistence, not cross-service atomicity.
        log.info("Enrollment persisted; awaiting transaction commit studentId={} courseId={}", studentId, courseId);
        return saved;
    }

    private BusinessException missingStudent(Long id) {
        return new BusinessException(HttpStatus.NOT_FOUND, "STUDENT_NOT_FOUND", "Không tìm thấy sinh viên " + id);
    }
}
