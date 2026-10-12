package com.example.coursemanagement.repository;

import com.example.coursemanagement.entity.CourseImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseImageRepository extends JpaRepository<CourseImage, Long> {

    Optional<CourseImage> findByIdAndCourse_Id(Long id, Long courseId);

    Optional<CourseImage> findFirstByCourse_IdOrderByIdAsc(Long courseId);

    List<CourseImage> findByCourse_IdInAndThumbnailTrue(Collection<Long> courseIds);

    long countByCourse_IdAndThumbnailTrue(Long courseId);

    /** Chuyển toàn bộ ảnh đại diện hiện tại của khóa học thành ảnh thường */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update CourseImage i set i.thumbnail = false where i.course.id = :courseId and i.thumbnail = true")
    int clearThumbnails(@Param("courseId") Long courseId);
}
