package com.example.coursemanagement.service;

import com.example.coursemanagement.dto.CourseFilter;
import com.example.coursemanagement.dto.CourseForm;
import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.entity.CourseImage;
import com.example.coursemanagement.exception.NotFoundException;
import com.example.coursemanagement.repository.CourseCategoryRepository;
import com.example.coursemanagement.repository.CourseImageRepository;
import com.example.coursemanagement.repository.CourseRepository;
import com.example.coursemanagement.repository.CourseSpecifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseCategoryRepository categoryRepository;
    private final CourseImageRepository imageRepository;

    public CourseService(CourseRepository courseRepository,
                         CourseCategoryRepository categoryRepository,
                         CourseImageRepository imageRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
    }

    /** Tìm kiếm + lọc + sắp xếp, tất cả trong một câu SQL */
    @Transactional(readOnly = true)
    public List<Course> search(CourseFilter filter) {
        return courseRepository.findAll(CourseSpecifications.fromFilter(filter), filter.toSort());
    }

    /** Lấy URL ảnh đại diện cho các khóa học trên trang danh sách (1 câu SQL) */
    @Transactional(readOnly = true)
    public Map<Long, String> thumbnailsOf(List<Course> courses) {
        if (courses.isEmpty()) return Map.of();
        List<Long> ids = courses.stream().map(Course::getId).toList();
        return imageRepository.findByCourse_IdInAndThumbnailTrue(ids).stream()
                .collect(Collectors.toMap(i -> i.getCourse().getId(), CourseImage::getImageUrl, (a, b) -> a));
    }

    @Transactional(readOnly = true)
    public Course getDetail(Long id) {
        return courseRepository.findWithDetailsById(id)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public CourseForm getForm(Long id) {
        return CourseForm.from(getDetail(id));
    }

    @Transactional(readOnly = true)
    public void ensureExists(Long id) {
        if (!courseRepository.existsById(id)) throw notFound(id);
    }

    /** Kiểm tra các ràng buộc cần truy vấn database: loại khóa học tồn tại, tên không trùng trong cùng loại */
    @Transactional(readOnly = true)
    public void validateBusinessRules(CourseForm form, Errors errors) {
        Long categoryId = form.getCategoryId();
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            errors.rejectValue("categoryId", "category.notFound", "Loại khóa học không tồn tại");
            return;
        }
        if (categoryId == null || form.getName() == null || form.getName().isBlank()) return;

        String name = form.getName().trim();
        boolean duplicated = form.getId() == null
                ? courseRepository.existsByNameIgnoreCaseAndCategory_Id(name, categoryId)
                : courseRepository.existsByNameIgnoreCaseAndCategory_IdAndIdNot(name, categoryId, form.getId());
        if (duplicated) {
            errors.rejectValue("name", "name.duplicated",
                    "Tên khóa học \"" + name + "\" đã tồn tại trong loại khóa học này");
        }
    }

    @Transactional
    public Course create(CourseForm form) {
        Course c = new Course();
        apply(form, c);
        return courseRepository.save(c);
    }

    @Transactional
    public Course update(Long id, CourseForm form) {
        Course c = courseRepository.findById(id).orElseThrow(() -> notFound(id));
        apply(form, c);
        return c;
    }

    @Transactional
    public String delete(Long id) {
        Course c = courseRepository.findById(id).orElseThrow(() -> notFound(id));
        String name = c.getName();
        courseRepository.delete(c); // ảnh của khóa học bị xóa theo (cascade)
        return name;
    }

    private void apply(CourseForm f, Course c) {
        c.setName(f.getName().trim());
        c.setTuitionFee(f.getTuitionFee());
        c.setDuration(f.getDuration());
        c.setActive(f.isActive());
        c.setDescription(f.getDescription() == null || f.getDescription().isBlank() ? null : f.getDescription().trim());
        c.setCategory(categoryRepository.getReferenceById(f.getCategoryId()));
    }

    private static NotFoundException notFound(Long id) {
        return new NotFoundException("Không tìm thấy khóa học có Id = " + id);
    }
}
