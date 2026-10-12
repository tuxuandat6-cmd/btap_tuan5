package com.example.coursemanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Courses",
        // Tên khóa học không được trùng trong cùng một loại khóa học
        uniqueConstraints = @UniqueConstraint(name = "UQ_Courses_Name_Category",
                columnNames = {"Name", "CourseCategoryId"}))
public class Course {

    public static final int SHORT_TERM_MAX_HOURS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    @Column(name = "TuitionFee", nullable = false, precision = 18, scale = 0)
    private BigDecimal tuitionFee;

    /** Thời lượng tính theo số giờ */
    @Column(name = "Duration", nullable = false)
    private Integer duration;

    @Column(name = "IsActive", nullable = false)
    private boolean active = true;

    @Column(name = "Description", length = 2000)
    private String description;

    /** Nhiều Course thuộc một CourseCategory (khóa ngoại CourseCategoryId) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CourseCategoryId", nullable = false,
            foreignKey = @ForeignKey(name = "FK_Courses_CourseCategories"))
    private CourseCategory category;

    /** Một Course có nhiều CourseImage; ảnh đại diện luôn đứng đầu */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("thumbnail DESC, id ASC")
    private List<CourseImage> images = new ArrayList<>();

    public Course() {
    }

    /**
     * Phân loại theo thời lượng - chỉ tính khi hiển thị, KHÔNG lưu thành cột trong database
     * (không có field tương ứng nên Hibernate không tạo cột).
     */
    public String getDurationType() {
        if (duration == null) return "";
        return duration <= SHORT_TERM_MAX_HOURS ? "Ngắn hạn" : "Dài hạn";
    }

    public String getStatusText() {
        return active ? "Đang tuyển sinh" : "Ngừng tuyển sinh";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getTuitionFee() { return tuitionFee; }
    public void setTuitionFee(BigDecimal tuitionFee) { this.tuitionFee = tuitionFee; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public CourseCategory getCategory() { return category; }
    public void setCategory(CourseCategory category) { this.category = category; }
    public List<CourseImage> getImages() { return images; }
    public void setImages(List<CourseImage> images) { this.images = images; }
}
