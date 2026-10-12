package com.example.coursemanagement.repository;

import com.example.coursemanagement.entity.Course;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    /** Tìm kiếm/lọc/sắp xếp được dịch thành câu SQL WHERE ... ORDER BY, lấy luôn loại khóa học (JOIN) */
    @Override
    @EntityGraph(attributePaths = "category")
    List<Course> findAll(Specification<Course> spec, Sort sort);

    @EntityGraph(attributePaths = {"category", "images"})
    Optional<Course> findWithDetailsById(Long id);

    long countByCategory_Id(Long categoryId);

    boolean existsByNameIgnoreCaseAndCategory_Id(String name, Long categoryId);

    boolean existsByNameIgnoreCaseAndCategory_IdAndIdNot(String name, Long categoryId, Long id);
}
