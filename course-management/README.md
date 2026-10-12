# Quản lý khóa học — Spring Boot + HTML (Thymeleaf) + SQL Server

## 1. Yêu cầu cài đặt
- JDK 17 trở lên
- Maven 3.8+ (hoặc mở bằng IntelliJ IDEA / Eclipse / VS Code – IDE tự tải thư viện)
- SQL Server (Express/Developer) + SQL Server Management Studio, bật đăng nhập SQL (user `sa`) và TCP/IP cổng 1433

## 2. Chạy ứng dụng
1. Mở SSMS, chạy file `database/create-database.sql` để tạo database `CourseManagementDB`.
2. Mở `src/main/resources/application.properties`, sửa `spring.datasource.username` / `password` cho đúng máy.
3. Chạy:
   ```bash
   mvn spring-boot:run
   ```
   (hoặc chạy hàm `main` trong `CourseManagementApplication.java` từ IDE)
4. Mở trình duyệt: http://localhost:8080

Lần chạy đầu, Hibernate tự tạo 3 bảng **Courses, CourseCategories, CourseImages** (có khóa ngoại),
và `DataSeeder` tự thêm dữ liệu mẫu: 3 loại khóa học, 7 khóa học, nhiều ảnh
(có 2 khóa "Ngừng tuyển sinh" và 1 khóa chưa có ảnh để demo).

Chạy test tự động (dùng H2 giả lập SQL Server, không cần SQL Server): `mvn test`

## 3. Cấu trúc thư mục
```
src/main/java/com/example/coursemanagement
├── entity/        CourseCategory, Course, CourseImage (quan hệ 1-n)
├── repository/    JPA repository + CourseSpecifications (lọc bằng câu WHERE trong DB)
├── dto/           CourseFilter (điều kiện tìm kiếm), CourseForm (form Create/Edit), CategoryRow
├── service/       Nghiệp vụ: validate, ảnh đại diện, ràng buộc xóa loại khóa học
├── controller/    CourseController, CourseCategoryController
├── exception/     NotFoundException, BusinessException, GlobalExceptionHandler
└── config/        DataSeeder (dữ liệu mẫu)
src/main/resources/templates   Giao diện HTML (Thymeleaf + Bootstrap 5)
```

## 4. Đáp ứng yêu cầu đề bài
| Yêu cầu | Cách làm |
|---|---|
| Quan hệ 1 Category – n Course, 1 Course – n Image | `@OneToMany` / `@ManyToOne`, khóa ngoại `CourseCategoryId`, `CourseId` |
| Chọn loại khóa học ở Create/Edit | Dropdown lấy từ bảng `CourseCategories` |
| Validation | `@NotBlank`, `@DecimalMin`, `@Positive` trên `CourseForm`; kiểm tra Category tồn tại và tên không trùng trong cùng loại ở `CourseService.validateBusinessRules` + unique constraint `(Name, CourseCategoryId)` |
| Tìm kiếm + lọc + sắp xếp đồng thời | `CourseSpecifications` sinh câu `WHERE ... ORDER BY` (xem SQL in ở console nhờ `show-sql=true`) — **không** lấy hết rồi lọc bằng vòng lặp |
| Giữ điều kiện trên form | Form dùng `th:object="${filter}"` + `th:field` |
| Ngắn hạn / Dài hạn | `Course.getDurationType()` — chỉ tính khi hiển thị, không có cột trong DB |
| Ảnh: thêm URL, nhiều ảnh, đổi ảnh đại diện | `CourseImageService`: bỏ đánh dấu ảnh đại diện cũ (`UPDATE`) rồi đặt ảnh mới → luôn tối đa 1 ảnh đại diện |
| Không xóa Category còn khóa học | `CourseCategoryService.delete` đếm khóa học, báo lỗi thân thiện |
| IsActive = false → "Ngừng tuyển sinh" | Vẫn hiện trong danh sách với nhãn đỏ |
| Bộ lọc "Đang tuyển sinh" | `WHERE IsActive = 1` |
| Xử lý lỗi | `GlobalExceptionHandler` + `error.html`: Id không tồn tại (404), Id sai kiểu, học phí min > max, chữ trong ô số, khóa học chưa có ảnh, không có kết quả, form không hợp lệ — không bao giờ hiện stack trace |

## 5. Kịch bản demo nhanh
1–2. SSMS: `SELECT * FROM CourseCategories / Courses / CourseImages`; mở Database Diagrams để xem quan hệ.
3. http://localhost:8080/courses — danh sách có cột Loại khóa học.
4. Tên: nhập `react` → Tìm kiếm.
5. Chọn loại "Lập trình Web".
6. Trạng thái "Đang tuyển sinh" → Figma, IELTS biến mất.
7. Học phí 3000000 – 5000000. Thử min 5000000, max 1000000 → báo lỗi.
8. Sắp xếp "Học phí giảm dần" / "Thời lượng tăng dần".
9. Cột Phân loại: Ngắn hạn / Dài hạn.
10. "+ Thêm khóa học" (thử bỏ trống tên, học phí 0, tên trùng "ReactJS" trong Lập trình Web để thấy lỗi) hoặc "Sửa".
11. Vào Chi tiết → dán URL ảnh, ví dụ `https://picsum.photos/seed/demo/640/400` → Thêm ảnh.
12. Bấm "Đặt làm ảnh đại diện" trên ảnh khác → ảnh cũ tự thành ảnh thường.
13. Menu "Loại khóa học" → Xóa "Lập trình Web" → báo không thể xóa.
14. Truy cập http://localhost:8080/courses/9999 → trang "Không tìm thấy khóa học có Id = 9999".

## 6. Lỗi thường gặp
- **Login failed for user 'sa'**: sai mật khẩu, hoặc SQL Server chưa bật "SQL Server and Windows Authentication mode".
- **Connection refused 1433**: mở SQL Server Configuration Manager → bật TCP/IP, đặt port 1433, restart service.
- **Cannot open database "CourseManagementDB"**: chưa chạy `database/create-database.sql`.
