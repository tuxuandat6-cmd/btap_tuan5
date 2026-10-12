-- Chạy script này trong SQL Server Management Studio TRƯỚC khi chạy ứng dụng.
-- Ứng dụng sẽ tự tạo 3 bảng (Courses, CourseCategories, CourseImages) và dữ liệu mẫu khi khởi động.
IF DB_ID(N'CourseManagementDB') IS NULL
    CREATE DATABASE CourseManagementDB;
GO

-- (Tùy chọn) Sau lần chạy ứng dụng đầu tiên, có thể thêm index lọc để database
-- cũng đảm bảo "mỗi khóa học tối đa một ảnh đại diện":
-- USE CourseManagementDB;
-- CREATE UNIQUE INDEX UX_CourseImages_OneThumbnail
--     ON CourseImages(CourseId) WHERE IsThumbnail = 1;

-- Câu lệnh xem dữ liệu khi demo:
-- USE CourseManagementDB;
-- SELECT * FROM CourseCategories;
-- SELECT * FROM Courses;
-- SELECT * FROM CourseImages;
