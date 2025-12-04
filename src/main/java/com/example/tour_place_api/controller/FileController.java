package com.example.tour_place_api.controller;

import com.example.tour_place_api.model.enums.FileType;
import com.example.tour_place_api.model.response.ApiResponse;
import com.example.tour_place_api.model.response.FileResponse;
import com.example.tour_place_api.security.JwtAuthenticationDetails;
import com.example.tour_place_api.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {
    @Autowired
    private MinioService minioService;

    @Operation(summary = "Upload file", description = "Upload a file to MinIO storage. MAIN_IMAGE and DETAIL_IMAGE require ROLE_ADMIN. PROFILE_IMAGE can be uploaded by users for their own profile.", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") @Valid MultipartFile file,
            @RequestParam("fileType") FileType fileType,
            Authentication authentication) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<FileResponse>builder()
                                .success(false)
                                .message("File is required and cannot be empty")
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }

            // Get user ID from authentication
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            String userId = details.getUserId();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // Check permissions
            if (fileType == FileType.MAIN_IMAGE || fileType == FileType.DETAIL_IMAGE) {
                if (!isAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.<FileResponse>builder()
                                    .success(false)
                                    .message("Only ADMIN can upload " + fileType.name() + " files")
                                    .status(HttpStatus.FORBIDDEN)
                                    .build());
                }
            }
            // PROFILE_IMAGE can be uploaded by both ADMIN and USER (no additional check needed)

            // Upload file to MinIO and get file name
            String fileName = minioService.uploadFileAndGetFileName(file, fileType, userId);
            
            // Construct file URL using ServletUriComponentsBuilder with query parameter
            // queryParam automatically URL-encodes the value
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/file/view")
                    .queryParam("fileName", fileName)
                    .toUriString();

            // Build file response
            FileResponse fileResponse = FileResponse.builder()
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

            ApiResponse<FileResponse> response = ApiResponse.<FileResponse>builder()
                    .success(true)
                    .message("Upload file successfully")
                    .status(HttpStatus.CREATED)
                    .payload(fileResponse)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<FileResponse>builder()
                            .success(false)
                            .message("Error uploading file: " + e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "View file by file name", description = "View/download a file by its file name (Public endpoint). File name can contain slashes (e.g., main_image/uuid.jpg).", security = {})
    @GetMapping("/view")
    public ResponseEntity<?> viewFileByFileName(
            @RequestParam("fileName") String fileName) {
        try {
            if (fileName == null || fileName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("File name is required")
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }

            // URL decode the file name in case it's encoded
            try {
                fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            } catch (Exception e) {
                // If decoding fails, use original
            }

            Resource resource = minioService.viewFileByFileName(fileName);
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

            // Determine media type based on file extension
            String lowerFileName = fileName.toLowerCase();
            if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (lowerFileName.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (lowerFileName.endsWith(".gif")) {
                mediaType = MediaType.IMAGE_GIF;
            } else if (lowerFileName.endsWith(".mp4")) {
                mediaType = MediaType.valueOf("video/mp4");
            } else if (lowerFileName.endsWith(".pdf")) {
                mediaType = MediaType.APPLICATION_PDF;
            } else if (lowerFileName.endsWith(".glb")) {
                mediaType = MediaType.parseMediaType("model/gltf-binary");
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("File not found: " + e.getMessage())
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }
    }

    @Operation(summary = "Delete file by file name", description = "Delete a file by its file name. MAIN_IMAGE and DETAIL_IMAGE require ROLE_ADMIN. PROFILE_IMAGE can be deleted by users for their own files.", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable String fileName,
            Authentication authentication) {
        try {
            // Get user ID from authentication
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            String userId = details.getUserId();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // Check permissions based on file type
            if (fileName.startsWith("main_image/") || fileName.startsWith("detail_image/")) {
                // Only ADMIN can delete main_image and detail_image
                if (!isAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.<Void>builder()
                                    .success(false)
                                    .message("Only ADMIN can delete this file type")
                                    .status(HttpStatus.FORBIDDEN)
                                    .build());
                }
            } else if (fileName.startsWith("profile_image/")) {
                // Users can only delete their own profile images
                if (!isAdmin && !minioService.isFileOwnedByUser(fileName, userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.<Void>builder()
                                    .success(false)
                                    .message("You can only delete your own profile images")
                                    .status(HttpStatus.FORBIDDEN)
                                    .build());
                }
            }

            minioService.deleteFile(fileName);
            
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(true)
                    .message("File has been deleted successfully")
                    .status(HttpStatus.OK)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Error deleting file: " + e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }
}

