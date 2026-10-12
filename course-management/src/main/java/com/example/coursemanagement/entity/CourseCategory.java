package com.example.coursemanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CourseCategories")
public class CourseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @NotBlank(message = "Tên loại khóa học không được để trống")
    @Size(max = 100, message = "Tên loại khóa học tối đa 100 ký tự")
    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    @Column(name = "Description", length = 500)
    private String description;

    /** Một CourseCategory có nhiều Course */
    @OneToMany(mappedBy = "category")
    private List<Course> courses = new ArrayList<>();

    public CourseCategory() {
    }

    public CourseCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }
}
