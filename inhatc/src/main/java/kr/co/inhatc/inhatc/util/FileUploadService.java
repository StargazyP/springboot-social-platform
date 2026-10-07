package kr.co.inhatc.inhatc.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import kr.co.inhatc.inhatc.constants.AppConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 업로드 공통 서비스 유틸리티
 * 중복된 파일 업로드 로직을 통합
 */
@Slf4j
public class FileUploadService {

    /**
     * 게시물 이미지 업로드
     * 
     * @param file 업로드할 파일
     * @param email 사용자 이메일
     * @param uploadDir 업로드 디렉토리 경로
     * @return DB에 저장할 URL 경로
     * @throws IOException 파일 저장 실패 시
     */
    public static String uploadPostImage(MultipartFile file, String email, String uploadDir) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일 보안 검증
        try {
            FileUploadValidator.validateFile(file);
        } catch (IllegalArgumentException e) {
            log.warn(AppConstants.LogMessage.FILE_VALIDATION_FAILED, e.getMessage());
            throw new IOException(String.format(AppConstants.ErrorMessage.FILE_VALIDATION_FAILED, e.getMessage()), e);
        }

        // 사용자 폴더 생성
        File userDir = new File(uploadDir, email);
        if (!userDir.exists()) {
            if (!userDir.mkdirs()) {
                throw new IOException(String.format(AppConstants.ErrorMessage.DIRECTORY_CREATION_FAILED, userDir.getAbsolutePath()));
            }
        }

        // 안전한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String timestamp = new SimpleDateFormat(AppConstants.FilePath.TIMESTAMP_FORMAT).format(new Date());
        String newFilename = FileUploadValidator.generateSafeFilename(originalFilename, timestamp);

        // 파일 저장
        File dest = new File(userDir, newFilename);
        file.transferTo(dest);

        // DB에 저장할 URL 경로
        String urlPath = AppConstants.FilePath.POSTS_URL_PREFIX + email + "/" + newFilename;
        log.debug(AppConstants.LogMessage.FILE_SAVED, email, urlPath);

        return urlPath;
    }

    /**
     * 프로필 이미지 업로드
     * 
     * @param file 업로드할 파일
     * @param email 사용자 이메일
     * @param uploadDir 업로드 디렉토리 경로
     * @return 저장된 파일의 전체 경로
     * @throws IOException 파일 저장 실패 시
     */
    public static Path uploadProfileImage(MultipartFile file, String email, String uploadDir) throws IOException {
        return uploadFixedNameImage(file, email, uploadDir, "profile");
    }

    /**
     * 프로필 배너 이미지 업로드
     */
    public static Path uploadBannerImage(MultipartFile file, String email, String uploadDir) throws IOException {
        return uploadFixedNameImage(file, email, uploadDir, "banner");
    }

    private static Path uploadFixedNameImage(MultipartFile file, String email, String uploadDir, String baseName) throws IOException {
        try {
            FileUploadValidator.validateFile(file);
        } catch (IllegalArgumentException e) {
            log.error(AppConstants.LogMessage.FILE_VALIDATION_FAILED, e.getMessage());
            throw new IOException(String.format(AppConstants.ErrorMessage.FILE_VALIDATION_FAILED, e.getMessage()), e);
        }

        Path userDir = Paths.get(uploadDir, email);
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = FileUploadValidator.getFileExtension(originalFilename);
        if (extension == null) {
            extension = "png";
        }

        String filename = baseName + "." + extension;
        Path filePath = userDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return filePath;
    }

    /**
     * 메시지 이미지 업로드
     * 
     * @param file 업로드할 파일
     * @param email 사용자 이메일
     * @param uploadDir 업로드 디렉토리 경로
     * @return DB에 저장할 URL 경로
     * @throws IOException 파일 저장 실패 시
     */
    public static String uploadMessageImage(MultipartFile file, String email, String uploadDir) throws IOException {
        // 파일 보안 검증
        try {
            FileUploadValidator.validateFile(file);
        } catch (IllegalArgumentException e) {
            log.error(AppConstants.LogMessage.FILE_VALIDATION_FAILED, e.getMessage());
            throw new IOException(String.format(AppConstants.ErrorMessage.FILE_VALIDATION_FAILED, e.getMessage()), e);
        }

        // 사용자 디렉토리 생성
        Path userDir = Paths.get(uploadDir, email, AppConstants.FilePath.MESSAGES_SUBDIR);
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
            log.info(AppConstants.LogMessage.DIRECTORY_CREATED, userDir);
        }

        // 안전한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String timestamp = new SimpleDateFormat(AppConstants.FilePath.TIMESTAMP_FORMAT).format(new Date());
        String newFilename = FileUploadValidator.generateSafeFilename(originalFilename, timestamp);
        Path filePath = userDir.resolve(newFilename);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        log.info("파일 저장 완료: {}", filePath);

        // DB에 저장할 상대 경로
        String imagePath = AppConstants.FilePath.MESSAGES_URL_PREFIX + email + "/" + 
                          AppConstants.FilePath.MESSAGES_SUBDIR + "/" + newFilename;
        log.info("이미지 경로: {}", imagePath);

        return imagePath;
    }
}

