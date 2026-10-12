package com.example.coursemanagement.dto;

import com.example.coursemanagement.entity.Course;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Dữ liệu form Create/Edit khóa học */
public class CourseForm {

    private Long id;

    @NotBlank(message = "Tên khóa học không được để trống")
    @Size(max = 200, message = "Tên khóa học tối đa 200 ký tự")
    private String name;

    @NotNull(message = "Học phí không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Học phí phải lớn hơn 0")
    @Digits(integer = 15, fraction = 0, message = "Học phí phải là số nguyên (VNĐ)")
    private BigDecimal tuitionFee;

    @NotNull(message = "Thời lượng không được để trống")
    @Positive(message = "Thời lượng phải lớn hơn 0")
    @Max(value = 10000, message = "Thời lượng tối đa 10000 giờ")
    private Integer duration;

    private boolean active = true;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    @NotNull(message = "Vui lòng chọn loại khóa học")
    private Long categoryId;

    public static CourseForm from(Course c) {
        CourseForm f = new CourseForm();
        f.id = c.getId();
        f.name = c.getName();
        f.tuitionFee = c.getTuitionFee();
        f.duration = c.getDuration();
        f.active = c.isActive();
        f.description = c.getDescription();
        f.categoryId = c.getCategory().getId();
        return f;
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
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
