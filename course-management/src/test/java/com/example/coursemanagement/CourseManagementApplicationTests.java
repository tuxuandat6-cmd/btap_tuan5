package com.example.coursemanagement;

import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.entity.CourseCategory;
import com.example.coursemanagement.entity.CourseImage;
import com.example.coursemanagement.repository.CourseCategoryRepository;
import com.example.coursemanagement.repository.CourseImageRepository;
import com.example.coursemanagement.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CourseManagementApplicationTests {

    @Autowired MockMvc mvc;
    @Autowired CourseRepository courseRepo;
    @Autowired CourseCategoryRepository categoryRepo;
    @Autowired CourseImageRepository imageRepo;

    private Course course(String name) {
        return courseRepo.findAll().stream().filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
    }

    private CourseCategory category(String name) {
        return categoryRepo.findAll().stream().filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
    }

    @Test
    void seedDataHasMinimumRows() {
        assertThat(categoryRepo.count()).isGreaterThanOrEqualTo(3);
        assertThat(courseRepo.count()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void listShowsCoursesWithCategoryDurationTypeAndStatus() throws Exception {
        mvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ASP.NET Core")))
                .andExpect(content().string(containsString("Lập trình Web")))
                .andExpect(content().string(containsString("Ngắn hạn")))
                .andExpect(content().string(containsString("Dài hạn")))
                .andExpect(content().string(containsString("Ngừng tuyển sinh")));
    }

    @Test
    void searchByPartialNameKeepsKeywordInForm() throws Exception {
        mvc.perform(get("/courses").param("keyword", "reac"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ReactJS")))
                .andExpect(content().string(not(containsString("ASP.NET Core"))))
                .andExpect(content().string(containsString("value=\"reac\"")));
    }

    @Test
    void combinedFilterCategoryStatusFeeRangeAndSort() throws Exception {
        Long webId = category("Lập trình Web").getId();
        String html = mvc.perform(get("/courses")
                        .param("categoryId", webId.toString())
                        .param("status", "active")
                        .param("minFee", "4000000")
                        .param("maxFee", "6000000")
                        .param("sort", "fee_desc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Spring Boot", "ASP.NET Core").doesNotContain("ReactJS");
        assertThat(html.indexOf("Spring Boot")).isLessThan(html.indexOf("ASP.NET Core")); // 5.2tr trước 4.5tr
        assertThat(html).contains("value=\"4000000\"", "value=\"6000000\"");
    }

    @Test
    void activeFilterHidesInactiveCourses() throws Exception {
        mvc.perform(get("/courses").param("status", "active"))
                .andExpect(content().string(not(containsString("Thiết kế UI/UX với Figma"))))
                .andExpect(content().string(not(containsString("IELTS cấp tốc"))));
    }

    @Test
    void sortByDurationAscending() throws Exception {
        String html = mvc.perform(get("/courses").param("sort", "duration_asc"))
                .andReturn().getResponse().getContentAsString();
        assertThat(html.indexOf("Photoshop cơ bản")).isLessThan(html.indexOf("Tiếng Anh giao tiếp"));
    }

    @Test
    void minFeeGreaterThanMaxFeeShowsMessage() throws Exception {
        mvc.perform(get("/courses").param("minFee", "5000000").param("maxFee", "1000000"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Học phí tối thiểu không được lớn hơn học phí tối đa")));
    }

    @Test
    void invalidFeeTextAndUnknownCategoryAreHandled() throws Exception {
        mvc.perform(get("/courses").param("minFee", "abc"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("phải là số hợp lệ")));
        mvc.perform(get("/courses").param("categoryId", "99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("không tồn tại")));
    }

    @Test
    void noResultMessage() throws Exception {
        mvc.perform(get("/courses").param("keyword", "khong-co-khoa-nay"))
                .andExpect(content().string(containsString("Không tìm thấy khóa học nào")));
    }

    @Test
    void unknownCourseIdShowsFriendly404() throws Exception {
        mvc.perform(get("/courses/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Không tìm thấy khóa học có Id = 99999")));
        mvc.perform(get("/courses/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Id phải là một số nguyên")));
        mvc.perform(get("/courses/99999/edit")).andExpect(status().isNotFound());
        mvc.perform(get("/categories/99999/edit")).andExpect(status().isNotFound());
        mvc.perform(get("/khong-ton-tai")).andExpect(status().isNotFound());
    }

    @Test
    void detailShowsImagesAndNoImageMessage() throws Exception {
        Course react = course("ReactJS");
        mvc.perform(get("/courses/" + react.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("react3")));
        Course ielts = course("IELTS cấp tốc");
        mvc.perform(get("/courses/" + ielts.getId()))
                .andExpect(content().string(containsString("Khóa học chưa có ảnh")));
    }

    @Test
    void createValidCourse() throws Exception {
        Long catId = category("Ngoại ngữ").getId();
        mvc.perform(post("/courses/create")
                        .param("name", "TOEIC 750+").param("tuitionFee", "3000000")
                        .param("duration", "32").param("categoryId", catId.toString()).param("active", "true"))
                .andExpect(status().is3xxRedirection());
        assertThat(courseRepo.existsByNameIgnoreCaseAndCategory_Id("TOEIC 750+", catId)).isTrue();
    }

    @Test
    void createInvalidCourseShowsErrors() throws Exception {
        mvc.perform(post("/courses/create")
                        .param("name", " ").param("tuitionFee", "0").param("duration", "-5").param("categoryId", "99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tên khóa học không được để trống")))
                .andExpect(content().string(containsString("Học phí phải lớn hơn 0")))
                .andExpect(content().string(containsString("Thời lượng phải lớn hơn 0")))
                .andExpect(content().string(containsString("Loại khóa học không tồn tại")));
    }

    @Test
    void duplicateNameInSameCategoryRejectedButAllowedInOther() throws Exception {
        Long webId = category("Lập trình Web").getId();
        Long langId = category("Ngoại ngữ").getId();
        mvc.perform(post("/courses/create")
                        .param("name", "reactjs").param("tuitionFee", "100").param("duration", "10")
                        .param("categoryId", webId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("đã tồn tại trong loại khóa học này")));
        mvc.perform(post("/courses/create")
                        .param("name", "ReactJS").param("tuitionFee", "100").param("duration", "10")
                        .param("categoryId", langId.toString()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void editInvalidAndEditValid() throws Exception {
        Course c = course("Spring Boot");
        String catId = c.getCategory().getId().toString();
        mvc.perform(post("/courses/" + c.getId() + "/edit")
                        .param("name", "Spring Boot").param("tuitionFee", "abc").param("duration", "45")
                        .param("categoryId", catId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is-invalid")));
        mvc.perform(post("/courses/" + c.getId() + "/edit")
                        .param("name", "Spring Boot nâng cao").param("tuitionFee", "6000000").param("duration", "50")
                        .param("categoryId", catId))
                .andExpect(redirectedUrl("/courses/" + c.getId()));
        mvc.perform(post("/courses/99999/edit")
                        .param("name", "X").param("tuitionFee", "1").param("duration", "1").param("categoryId", catId))
                .andExpect(status().isNotFound());
    }

    @Test
    void addImageAndChangeThumbnailKeepsOnlyOne() throws Exception {
        Course c = course("ReactJS");
        mvc.perform(post("/courses/" + c.getId() + "/images")
                        .param("imageUrl", "https://picsum.photos/seed/new/640/400").param("thumbnail", "true"))
                .andExpect(status().is3xxRedirection());
        List<CourseImage> thumbs = imageRepo.findByCourse_IdInAndThumbnailTrue(List.of(c.getId()));
        assertThat(thumbs).hasSize(1);
        assertThat(thumbs.get(0).getImageUrl()).contains("seed/new");

        CourseImage other = imageRepo.findAll().stream()
                .filter(i -> i.getCourse().getId().equals(c.getId()) && !i.isThumbnail()).findFirst().orElseThrow();
        mvc.perform(post("/courses/" + c.getId() + "/images/" + other.getId() + "/thumbnail"))
                .andExpect(status().is3xxRedirection());
        thumbs = imageRepo.findByCourse_IdInAndThumbnailTrue(List.of(c.getId()));
        assertThat(thumbs).hasSize(1);
        assertThat(thumbs.get(0).getId()).isEqualTo(other.getId());
    }

    @Test
    void invalidImageUrlAndUnknownImageId() throws Exception {
        Course c = course("ReactJS");
        mvc.perform(post("/courses/" + c.getId() + "/images").param("imageUrl", "not-a-url"))
                .andExpect(flash().attribute("error", containsString("không hợp lệ")));
        mvc.perform(post("/courses/" + c.getId() + "/images/99999/thumbnail"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Không tìm thấy ảnh")));
        mvc.perform(post("/courses/99999/images").param("imageUrl", "https://a.com/x.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cannotDeleteCategoryInUse() throws Exception {
        CourseCategory web = category("Lập trình Web");
        mvc.perform(post("/categories/" + web.getId() + "/delete"))
                .andExpect(redirectedUrl("/categories"))
                .andExpect(flash().attribute("error", containsString("Không thể xóa loại khóa học")));
        assertThat(categoryRepo.existsById(web.getId())).isTrue();
    }

    @Test
    void canDeleteEmptyCategory() throws Exception {
        CourseCategory empty = categoryRepo.save(new CourseCategory("Tạm thời", null));
        mvc.perform(post("/categories/" + empty.getId() + "/delete"))
                .andExpect(flash().attribute("success", notNullValue()));
        assertThat(categoryRepo.existsById(empty.getId())).isFalse();
    }
}
