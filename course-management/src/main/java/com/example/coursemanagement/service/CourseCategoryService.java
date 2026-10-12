package com.example.coursemanagement.service;

import com.example.coursemanagement.dto.CategoryRow;
import com.example.coursemanagement.entity.CourseCategory;
import com.example.coursemanagement.exception.BusinessException;
import com.example.coursemanagement.exception.NotFoundException;
import com.example.coursemanagement.repository.CourseCategoryRepository;
import com.example.coursemanagement.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseCategoryService {

    private final CourseCategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public CourseCategoryService(CourseCategoryRepository categoryRepository, CourseRepository courseRepository) {
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryRow> findAllWithCourseCount() {
        return categoryRepository.findAllWithCourseCount();
    }

    @Transactional(readOnly = true)
    public List<CourseCategory> findAllForSelect() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return id != null && categoryRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public CourseCategory getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy loại khóa học có Id = " + id));
    }

    @Transactional
    public CourseCategory create(CourseCategory input) {
        CourseCategory c = new CourseCategory(input.getName().trim(), trimToNull(input.getDescription()));
        return categoryRepository.save(c);
    }

    @Transactional
    public CourseCategory update(Long id, CourseCategory input) {
        CourseCategory c = getById(id);
        c.setName(input.getName().trim());
        c.setDescription(trimToNull(input.getDescription()));
        return c;
    }

    /** Ràng buộc 1: không cho xóa loại khóa học nếu vẫn còn khóa học thuộc loại đó */
    @Transactional
    public void delete(Long id) {
        CourseCategory c = getById(id);
        long count = courseRepository.countByCategory_Id(id);
        if (count > 0) {
            throw new BusinessException("Không thể xóa loại khóa học \"" + c.getName()
                    + "\" vì vẫn còn " + count + " khóa học thuộc loại này.");
        }
        categoryRepository.delete(c);
    }

    private static String trimToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
