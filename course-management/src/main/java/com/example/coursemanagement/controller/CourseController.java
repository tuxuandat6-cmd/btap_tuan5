package com.example.coursemanagement.controller;

import com.example.coursemanagement.dto.CourseFilter;
import com.example.coursemanagement.dto.CourseForm;
import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.exception.BusinessException;
import com.example.coursemanagement.service.CourseCategoryService;
import com.example.coursemanagement.service.CourseImageService;
import com.example.coursemanagement.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CourseController {

    private final CourseService courseService;
    private final CourseCategoryService categoryService;
    private final CourseImageService imageService;

    public CourseController(CourseService courseService, CourseCategoryService categoryService,
                            CourseImageService imageService) {
        this.courseService = courseService;
        this.categoryService = categoryService;
        this.imageService = imageService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/courses";
    }

    // ======================= DANH SÁCH + TRA CỨU =======================

    @GetMapping("/courses")
    public String list(@ModelAttribute("filter") CourseFilter filter, BindingResult binding, Model model) {
        List<String> errors = new ArrayList<>();

        if (binding.hasFieldErrors("minFee") || binding.hasFieldErrors("maxFee")) {
            errors.add("Học phí tối thiểu / tối đa phải là số hợp lệ.");
        }
        if (binding.hasFieldErrors("categoryId")) {
            errors.add("Loại khóa học không hợp lệ.");
        }
        if (isNegative(filter.getMinFee()) || isNegative(filter.getMaxFee())) {
            errors.add("Học phí tìm kiếm không được là số âm.");
        }
        if (filter.getMinFee() != null && filter.getMaxFee() != null
                && filter.getMinFee().compareTo(filter.getMaxFee()) > 0) {
            errors.add("Học phí tối thiểu không được lớn hơn học phí tối đa.");
        }
        if (filter.getCategoryId() != null && !categoryService.exists(filter.getCategoryId())) {
            errors.add("Loại khóa học có Id = " + filter.getCategoryId() + " không tồn tại.");
        }

        List<Course> courses = errors.isEmpty() ? courseService.search(filter) : List.of();

        model.addAttribute("courses", courses);
        model.addAttribute("thumbnails", courseService.thumbnailsOf(courses));
        model.addAttribute("categories", categoryService.findAllForSelect());
        model.addAttribute("filterErrors", errors);
        return "courses/list";
    }

    // ======================= CHI TIẾT =======================

    @GetMapping("/courses/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.getDetail(id));
        return "courses/detail";
    }

    // ======================= CREATE =======================

    @GetMapping("/courses/create")
    public String createForm(Model model) {
        model.addAttribute("courseForm", new CourseForm());
        return formView(model);
    }

    @PostMapping("/courses/create")
    public String create(@Valid @ModelAttribute("courseForm") CourseForm form, BindingResult binding,
                         Model model, RedirectAttributes ra) {
        form.setId(null);
        courseService.validateBusinessRules(form, binding);
        if (binding.hasErrors()) {
            return formView(model);
        }
        try {
            Course saved = courseService.create(form);
            ra.addFlashAttribute("success", "Đã thêm khóa học \"" + saved.getName() + "\".");
            return "redirect:/courses/" + saved.getId();
        } catch (DataIntegrityViolationException ex) {
            binding.reject("db", "Dữ liệu không hợp lệ hoặc tên khóa học đã tồn tại trong loại này.");
            return formView(model);
        }
    }

    // ======================= EDIT =======================

    @GetMapping("/courses/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("courseForm", courseService.getForm(id));
        return formView(model);
    }

    @PostMapping("/courses/{id}/edit")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("courseForm") CourseForm form,
                       BindingResult binding, Model model, RedirectAttributes ra) {
        courseService.ensureExists(id); // CourseId không tồn tại -> trang lỗi 404
        form.setId(id);
        courseService.validateBusinessRules(form, binding);
        if (binding.hasErrors()) {
            return formView(model);
        }
        try {
            courseService.update(id, form);
            ra.addFlashAttribute("success", "Đã cập nhật khóa học.");
            return "redirect:/courses/" + id;
        } catch (DataIntegrityViolationException ex) {
            binding.reject("db", "Dữ liệu không hợp lệ hoặc tên khóa học đã tồn tại trong loại này.");
            return formView(model);
        }
    }

    // ======================= DELETE =======================

    @PostMapping("/courses/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        String name = courseService.delete(id);
        ra.addFlashAttribute("success", "Đã xóa khóa học \"" + name + "\".");
        return "redirect:/courses";
    }

    // ======================= ẢNH KHÓA HỌC =======================

    @PostMapping("/courses/{courseId}/images")
    public String addImage(@PathVariable Long courseId,
                           @RequestParam(value = "imageUrl", required = false) String imageUrl,
                           @RequestParam(value = "thumbnail", defaultValue = "false") boolean thumbnail,
                           RedirectAttributes ra) {
        try {
            imageService.addImage(courseId, imageUrl, thumbnail);
            ra.addFlashAttribute("success", "Đã thêm ảnh cho khóa học.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("imageUrl", imageUrl);
        }
        return "redirect:/courses/" + courseId;
    }

    @PostMapping("/courses/{courseId}/images/{imageId}/thumbnail")
    public String setThumbnail(@PathVariable Long courseId, @PathVariable Long imageId, RedirectAttributes ra) {
        imageService.setThumbnail(courseId, imageId);
        ra.addFlashAttribute("success", "Đã đổi ảnh đại diện.");
        return "redirect:/courses/" + courseId;
    }

    @PostMapping("/courses/{courseId}/images/{imageId}/delete")
    public String deleteImage(@PathVariable Long courseId, @PathVariable Long imageId, RedirectAttributes ra) {
        imageService.deleteImage(courseId, imageId);
        ra.addFlashAttribute("success", "Đã xóa ảnh.");
        return "redirect:/courses/" + courseId;
    }

    private String formView(Model model) {
        model.addAttribute("categories", categoryService.findAllForSelect());
        return "courses/form";
    }

    private static boolean isNegative(BigDecimal v) {
        return v != null && v.signum() < 0;
    }
}
