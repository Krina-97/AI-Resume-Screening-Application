package com.airesume.screening.service;

import com.airesume.screening.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class ResumeTextExtractorService {

    public String extractText(MultipartFile file, Path storedFile) {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        try {
            if (name.endsWith(".pdf")) {
                return extractPdf(storedFile);
            }
            if (name.endsWith(".docx")) {
                return extractDocx(storedFile);
            }
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported resume format");
        } catch (IOException ex) {
            log.error("Failed to extract resume text", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse resume file");
        }
    }

    private String extractPdf(Path path) throws IOException {
        try (org.apache.pdfbox.pdmodel.PDDocument document =
                     org.apache.pdfbox.Loader.loadPDF(path.toFile())) {
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractDocx(Path path) throws IOException {
        try (java.io.InputStream is = Files.newInputStream(path);
             org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (org.apache.poi.xwpf.usermodel.XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append('\n');
            }
            return sb.toString();
        }
    }
}
