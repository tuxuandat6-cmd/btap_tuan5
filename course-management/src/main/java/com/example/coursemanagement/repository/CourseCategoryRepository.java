package com.example.coursemanagement.repository;

import com.example.coursemanagement.dto.CategoryRow;
import com.example.coursemanagement.entity.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseCategoryRepository extends JpaRepository<CourseCategory, Long> {

    /** Danh sách loại khóa học kèm số khóa học (đếm bằng GROUP BY trong database) */
    @Query("""
            select new com.example.coursemanagement.dto.CategoryRow(c.id, c.name, c.description, count(co.id))
            from CourseCategory c left join c.courses co
            group by c.id, c.name, c.description
            order by c.id
            """)
    List<CategoryRow> findAllWithCourseCount();

    List<CourseCategory> findAllByOrderByNameAsc();
}
