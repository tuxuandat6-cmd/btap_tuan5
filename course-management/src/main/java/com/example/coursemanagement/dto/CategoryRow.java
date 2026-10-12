package com.example.coursemanagement.dto;

/** Một dòng trên trang danh sách loại khóa học (kèm số khóa học) */
public class CategoryRow {

    private final Long id;
    private final String name;
    private final String description;
    private final Long courseCount;

    public CategoryRow(Long id, String name, String description, Long courseCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.courseCount = courseCount == null ? 0L : courseCount;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getCourseCount() { return courseCount; }
}
