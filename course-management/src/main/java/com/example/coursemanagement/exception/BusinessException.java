package com.example.coursemanagement.exception;

/** Vi phạm ràng buộc nghiệp vụ (vd: xóa loại khóa học đang được sử dụng) */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
