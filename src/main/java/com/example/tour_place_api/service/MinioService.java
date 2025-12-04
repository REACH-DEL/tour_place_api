package com.example.tour_place_api.service;

import com.example.tour_place_api.model.enums.FileType;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {
    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name:tour-place}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    public void initializeBucket() {
        try {
            boolean isExist = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize bucket: " + e.getMessage());
        }
    }

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or null");
        }
        
        try {
            initializeBucket();
            String objectName = generateObjectName(file.getOriginalFilename());
            InputStream inputStream = file.getInputStream();

            // Upload file to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            inputStream.close();

            // Get permanent URL (1 year expiry for practical purposes)
            String url = getFileUrl(objectName);
            
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO: " + e.getMessage(), e);
        }
    }

    public String uploadFileAndGetFileName(MultipartFile file, FileType fileType, String userId) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or null");
        }
        
        try {
            initializeBucket();
            String objectName = generateObjectName(file.getOriginalFilename(), fileType, userId);
            InputStream inputStream = file.getInputStream();

            // Upload file to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            inputStream.close();
            
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO: " + e.getMessage(), e);
        }
    }

    public boolean isFileOwnedByUser(String fileName, String userId) {
        // For profile_image, check if file path contains userId
        // Format: profile_image/{userId}/{filename}
        if (fileName.startsWith("profile_image/")) {
            String[] parts = fileName.split("/");
            if (parts.length >= 2) {
                return parts[1].equals(userId);
            }
        }
        return false;
    }

    public Resource viewFileByFileName(String fileName) {
        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            return new InputStreamResource(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file from MinIO: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO: " + e.getMessage(), e);
        }
    }

    public String getFileUrl(String objectName) {
        try {
            // Generate presigned URL with 1 year expiry (practical permanent URL)
            // For truly permanent URLs, you would need to make the bucket public
            // or implement a proxy endpoint that generates URLs on-demand
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(365, TimeUnit.DAYS) // 1 year expiry
                            .build());
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file URL from MinIO: " + e.getMessage(), e);
        }
    }

    private String generateObjectName(String originalFilename, FileType fileType, String userId) {
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueId = UUID.randomUUID().toString();
        
        switch (fileType) {
            case MAIN_IMAGE:
                return "main_image/" + uniqueId + fileExtension;
            case DETAIL_IMAGE:
                return "detail_image/" + uniqueId + fileExtension;
            case PROFILE_IMAGE:
                // Store with userId for ownership tracking
                return "profile_image/" + userId + "/" + uniqueId + fileExtension;
            default:
                return "places/" + uniqueId + fileExtension;
        }
    }

    private String generateObjectName(String originalFilename) {
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return "places/" + UUID.randomUUID() + fileExtension;
    }
}
