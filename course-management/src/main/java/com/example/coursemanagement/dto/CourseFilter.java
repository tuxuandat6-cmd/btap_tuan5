package com.example.coursemanagement.dto;

import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

/** Các điều kiện tìm kiếm/lọc trên trang danh sách (được giữ lại trên form sau khi lọc) */
public class CourseFilter {

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";

    public static final String SORT_FEE_ASC = "fee_asc";
    public static final String SORT_FEE_DESC = "fee_desc";
    public static final String SORT_DURATION_ASC = "duration_asc";

    private String keyword;
    private Long categoryId;
    private String status;
    private BigDecimal minFee;
    private BigDecimal maxFee;
    private String sort;

    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }

    public Sort toSort() {
        if (SORT_FEE_ASC.equals(sort)) return Sort.by(Sort.Order.asc("tuitionFee"), Sort.Order.asc("id"));
        if (SORT_FEE_DESC.equals(sort)) return Sort.by(Sort.Order.desc("tuitionFee"), Sort.Order.asc("id"));
        if (SORT_DURATION_ASC.equals(sort)) return Sort.by(Sort.Order.asc("duration"), Sort.Order.asc("id"));
        return Sort.by(Sort.Order.asc("id"));
    }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getMinFee() { return minFee; }
    public void setMinFee(BigDecimal minFee) { this.minFee = minFee; }
    public BigDecimal getMaxFee() { return maxFee; }
    public void setMaxFee(BigDecimal maxFee) { this.maxFee = maxFee; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
}
