package vn.edu.course.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import vn.edu.course.dto.CreateCourseRequest;
import vn.edu.course.entity.*;
import vn.edu.course.repository.*;
import vn.edu.course.exception.BusinessException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courses;
    private final ReservationRepository reservations;

    @Transactional
    public Course create(CreateCourseRequest request) {
        Course saved = courses.saveAndFlush(new Course(request.courseName().strip(), request.capacity(), request.credits()));
        log.info("Course created courseId={} capacity={} credits={}", saved.getId(), saved.getCapacity(), saved.getCredits());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Course> list() {
        List<Course> result = courses.findAll();
        log.debug("Courses fetched count={}", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public Course get(Long id) {
        log.debug("Fetching course courseId={}", id);
        return courses.findById(id).orElseThrow(() -> missingCourse(id));
    }

    @Transactional
    public Course reserve(Long courseId, Long studentId) {
        log.info("Reservation started studentId={} courseId={}", studentId, courseId);
        Course course = courses.findForUpdate(courseId).orElseThrow(() -> missingCourse(courseId));
        if (reservations.existsByStudentIdAndCourseId(studentId, courseId)) {
            log.info("Existing reservation reused studentId={} courseId={}", studentId, courseId);
            return course;
        }
        if (course.getCapacity() <= 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "COURSE_FULL", "Khóa học " + courseId + " đã hết chỗ");
        }
        course.reserveSeat();
        reservations.save(new Reservation(studentId, courseId));
        Course saved = courses.saveAndFlush(course);
        log.info("Reservation persisted; awaiting transaction commit studentId={} courseId={} remainingCapacity={}", studentId, courseId, saved.getCapacity());
        return saved;
    }

    private BusinessException missingCourse(Long id) {
        return new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học " + id);
    }
}
