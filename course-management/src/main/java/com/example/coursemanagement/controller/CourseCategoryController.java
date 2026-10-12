package com.example.coursemanagement.controller;

import com.example.coursemanagement.entity.CourseCategory;
import com.example.coursemanagement.exception.BusinessException;
import com.example.coursemanagement.service.CourseCategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CourseCategoryController {

    private final CourseCategoryService categoryService;

    public CourseCategoryController(CourseCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAllWithCourseCount());
        return "categories/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new CourseCategory());
        return "categories/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("category") CourseCategory category, BindingResult binding,
                         RedirectAttributes ra) {
        if (binding.hasErrors()) return "categories/form";
        CourseCategory saved = categoryService.create(category);
        ra.addFlashAttribute("success", "Đã thêm loại khóa học \"" + saved.getName() + "\".");
        return "redirect:/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getById(id));
        return "categories/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("category") CourseCategory category,
                       BindingResult binding, RedirectAttributes ra) {
        categoryService.getById(id); // CourseCategoryId không tồn tại -> trang lỗi 404
        category.setId(id);
        if (binding.hasErrors()) return "categories/form";
        categoryService.update(id, category);
        ra.addFlashAttribute("success", "Đã cập nhật loại khóa học.");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa loại khóa học.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/categories";
    }
}
