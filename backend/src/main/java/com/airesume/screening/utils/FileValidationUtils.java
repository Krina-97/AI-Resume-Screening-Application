package com.airesume.screening.utils;

import com.airesume.screening.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

public final class FileValidationUtils {

    private static final Set<String> ALLOWED = Set.of("application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private FileValidationUtils() {
    }

    public static void validateResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "File is required");
        }
        String contentType = file.getContentType();
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        boolean byExt = name.endsWith(".pdf") || name.endsWith(".docx");
        boolean byType = contentType != null && ALLOWED.stream().anyMatch(contentType::equalsIgnoreCase);
        if (!byExt && !byType) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PDF and DOCX files are allowed");
        }
    }
}
