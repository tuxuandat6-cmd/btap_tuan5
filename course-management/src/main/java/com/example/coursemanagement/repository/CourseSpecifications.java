package com.example.coursemanagement.repository;

import com.example.coursemanagement.dto.CourseFilter;
import com.example.coursemanagement.entity.Course;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng điều kiện WHERE động. Toàn bộ việc lọc chạy trong database,
 * không lấy hết dữ liệu rồi dùng vòng lặp để lọc.
 */
public final class CourseSpecifications {

    private CourseSpecifications() {
    }

    public static Specification<Course> fromFilter(CourseFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm theo một phần tên khóa học: Name LIKE %keyword%
            if (f.hasKeyword()) {
                String pattern = "%" + escapeLike(f.getKeyword().trim().toLowerCase()) + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern, '\\'));
            }
            // Lọc theo loại khóa học
            if (f.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), f.getCategoryId()));
            }
            // Lọc theo trạng thái
            if (CourseFilter.STATUS_ACTIVE.equals(f.getStatus())) {
                predicates.add(cb.isTrue(root.get("active")));
            } else if (CourseFilter.STATUS_INACTIVE.equals(f.getStatus())) {
                predicates.add(cb.isFalse(root.get("active")));
            }
            // Khoảng học phí
            if (f.getMinFee() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("tuitionFee"), f.getMinFee()));
            }
            if (f.getMaxFee() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("tuitionFee"), f.getMaxFee()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_").replace("[", "\\[");
    }
}
