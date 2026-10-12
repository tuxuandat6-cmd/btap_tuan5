package com.example.coursemanagement.service;

import com.example.coursemanagement.entity.CourseImage;
import com.example.coursemanagement.exception.BusinessException;
import com.example.coursemanagement.exception.NotFoundException;
import com.example.coursemanagement.repository.CourseImageRepository;
import com.example.coursemanagement.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class CourseImageService {

    private static final Pattern URL_PATTERN = Pattern.compile("^https?://\\S+$", Pattern.CASE_INSENSITIVE);

    private final CourseImageRepository imageRepository;
    private final CourseRepository courseRepository;

    public CourseImageService(CourseImageRepository imageRepository, CourseRepository courseRepository) {
        this.imageRepository = imageRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Thêm ảnh bằng URL. Nếu chọn làm ảnh đại diện (hoặc khóa học chưa có ảnh đại diện)
     * thì ảnh mới thành ảnh đại diện và ảnh đại diện cũ tự chuyển thành ảnh thường.
     */
    @Transactional
    public void addImage(Long courseId, String imageUrl, boolean makeThumbnail) {
        if (!courseRepository.existsById(courseId)) {
            throw new NotFoundException("Không tìm thấy khóa học có Id = " + courseId);
        }
        String url = imageUrl == null ? "" : imageUrl.trim();
        if (url.isEmpty()) {
            throw new BusinessException("Đường dẫn ảnh không được để trống.");
        }
        if (url.length() > 1000 || !URL_PATTERN.matcher(url).matches()) {
            throw new BusinessException("Đường dẫn ảnh không hợp lệ (phải bắt đầu bằng http:// hoặc https://).");
        }

        boolean hasThumbnail = imageRepository.countByCourse_IdAndThumbnailTrue(courseId) > 0;
        boolean thumbnail = makeThumbnail || !hasThumbnail;
        if (thumbnail && hasThumbnail) {
            imageRepository.clearThumbnails(courseId);
        }
        imageRepository.save(new CourseImage(url, thumbnail, courseRepository.getReferenceById(courseId)));
    }

    /** Đổi ảnh đại diện: mỗi khóa học chỉ có tối đa một ảnh đại diện */
    @Transactional
    public void setThumbnail(Long courseId, Long imageId) {
        if (!courseRepository.existsById(courseId)) {
            throw new NotFoundException("Không tìm thấy khóa học có Id = " + courseId);
        }
        // Kiểm tra ảnh tồn tại và thuộc đúng khóa học trước khi thay đổi
        findImage(courseId, imageId);
        imageRepository.clearThumbnails(courseId);       // ảnh đại diện cũ -> ảnh thường
        CourseImage image = findImage(courseId, imageId); // nạp lại sau khi clear persistence context
        image.setThumbnail(true);
    }

    @Transactional
    public void deleteImage(Long courseId, Long imageId) {
        CourseImage image = findImage(courseId, imageId);
        boolean wasThumbnail = image.isThumbnail();
        imageRepository.delete(image);
        imageRepository.flush();
        if (wasThumbnail) {
            // Tự chọn ảnh còn lại đầu tiên làm ảnh đại diện
            imageRepository.findFirstByCourse_IdOrderByIdAsc(courseId).ifPresent(i -> i.setThumbnail(true));
        }
    }

    private CourseImage findImage(Long courseId, Long imageId) {
        return imageRepository.findByIdAndCourse_Id(imageId, courseId)
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy ảnh có Id = " + imageId + " của khóa học Id = " + courseId));
    }
}
