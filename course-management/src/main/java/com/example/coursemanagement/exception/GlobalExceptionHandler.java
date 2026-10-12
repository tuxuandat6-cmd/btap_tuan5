package com.example.coursemanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Bắt mọi lỗi và hiển thị trang thông báo thân thiện,
 * không để lỗi hệ thống (stack trace) hiện ra màn hình.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView notFound(NotFoundException ex, HttpServletRequest req) {
        return page(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu", ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView business(BusinessException ex, HttpServletRequest req) {
        return page(HttpStatus.BAD_REQUEST, "Thao tác không hợp lệ", ex.getMessage(), req);
    }

    /** Ví dụ: /courses/abc (Id không phải số) */
    @ExceptionHandler(TypeMismatchException.class)
    public ModelAndView typeMismatch(TypeMismatchException ex, HttpServletRequest req) {
        return page(HttpStatus.BAD_REQUEST, "Tham số không hợp lệ",
                "Giá trị \"" + ex.getValue() + "\" không hợp lệ. Id phải là một số nguyên.", req);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView other(Exception ex, HttpServletRequest req) {
        if (ex instanceof ErrorResponse er) { // 404 đường dẫn sai, 405 sai phương thức, ...
            HttpStatusCode code = er.getStatusCode();
            String msg = code.value() == 404 ? "Trang bạn yêu cầu không tồn tại."
                    : code.value() == 405 ? "Phương thức truy cập không được hỗ trợ cho đường dẫn này."
                    : "Yêu cầu không hợp lệ.";
            return page(code, "Lỗi " + code.value(), msg, req);
        }
        log.error("Lỗi không mong muốn tại {}", req.getRequestURI(), ex);
        return page(HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi xảy ra",
                "Hệ thống gặp sự cố khi xử lý yêu cầu. Vui lòng thử lại sau.", req);
    }

    private ModelAndView page(HttpStatusCode status, String title, String message, HttpServletRequest req) {
        ModelAndView mv = new ModelAndView("error");
        mv.setStatus(status);
        mv.addObject("status", status.value());
        mv.addObject("errorTitle", title);
        mv.addObject("errorMessage", message);
        mv.addObject("path", req.getRequestURI());
        return mv;
    }
}
