package com.example.coursemanagement.config;

import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.entity.CourseCategory;
import com.example.coursemanagement.entity.CourseImage;
import com.example.coursemanagement.repository.CourseCategoryRepository;
import com.example.coursemanagement.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Tạo dữ liệu mẫu khi database còn trống: 3 loại khóa học, 7 khóa học, nhiều ảnh */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CourseCategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public DataSeeder(CourseCategoryRepository categoryRepository, CourseRepository courseRepository) {
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) return;

        CourseCategory web = categoryRepository.save(
                new CourseCategory("Lập trình Web", "Các khóa học phát triển ứng dụng web front-end và back-end"));
        CourseCategory design = categoryRepository.save(
                new CourseCategory("Thiết kế đồ họa", "Thiết kế hình ảnh, giao diện người dùng"));
        CourseCategory lang = categoryRepository.save(
                new CourseCategory("Ngoại ngữ", "Tiếng Anh giao tiếp và luyện thi chứng chỉ"));

        course("ASP.NET Core", 4_500_000, 40, true,
                "Xây dựng ứng dụng web với ASP.NET Core MVC và Entity Framework Core.", web,
                "https://picsum.photos/seed/aspnetcore/640/400", "https://picsum.photos/seed/aspnet2/640/400");
        course("ReactJS", 3_800_000, 24, true,
                "Xây dựng giao diện web hiện đại với React, Hooks và React Router.", web,
                "https://picsum.photos/seed/reactjs/640/400", "https://picsum.photos/seed/react2/640/400",
                "https://picsum.photos/seed/react3/640/400");
        course("Spring Boot", 5_200_000, 45, true,
                "Phát triển REST API và web MVC với Java Spring Boot.", web,
                "https://picsum.photos/seed/springboot/640/400");
        course("Photoshop cơ bản", 2_000_000, 20, true,
                "Làm quen với công cụ, chỉnh sửa và ghép ảnh cơ bản.", design,
                "https://picsum.photos/seed/photoshop/640/400");
        course("Thiết kế UI/UX với Figma", 3_500_000, 36, false,
                "Thiết kế wireframe, prototype và design system với Figma.", design,
                "https://picsum.photos/seed/figma/640/400", "https://picsum.photos/seed/figma2/640/400");
        course("Tiếng Anh giao tiếp", 6_000_000, 60, true,
                "Luyện phản xạ nghe nói trong các tình huống hằng ngày.", lang,
                "https://picsum.photos/seed/english/640/400");
        // Khóa học chưa có ảnh -> dùng để demo trường hợp "Khóa học chưa có ảnh"
        course("IELTS cấp tốc", 7_500_000, 30, false,
                "Ôn luyện 4 kỹ năng IELTS mục tiêu 6.5 trong thời gian ngắn.", lang);

        log.info("Đã tạo dữ liệu mẫu: 3 loại khóa học, 7 khóa học.");
    }

    private void course(String name, long fee, int hours, boolean active, String desc,
                        CourseCategory category, String... imageUrls) {
        Course c = new Course();
        c.setName(name);
        c.setTuitionFee(BigDecimal.valueOf(fee));
        c.setDuration(hours);
        c.setActive(active);
        c.setDescription(desc);
        c.setCategory(category);
        for (int i = 0; i < imageUrls.length; i++) {
            c.getImages().add(new CourseImage(imageUrls[i], i == 0, c)); // ảnh đầu tiên là ảnh đại diện
        }
        courseRepository.save(c);
    }
}
