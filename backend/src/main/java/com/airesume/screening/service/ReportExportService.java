package com.airesume.screening.service;

import com.airesume.screening.dto.CandidateDto;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReportExportService {

    public byte[] exportCandidatesExcel(List<CandidateDto> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Candidates");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Email");
            header.createCell(2).setCellValue("Status");
            header.createCell(3).setCellValue("Match Score");
            header.createCell(4).setCellValue("Candidate Overview");

            int r = 1;
            for (CandidateDto dto : rows) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(dto.getFullName());
                row.createCell(1).setCellValue(dto.getEmail());
                row.createCell(2).setCellValue(dto.getStatus().name());
                if (dto.getLatestMatchScore() != null) {
                    row.createCell(3).setCellValue(dto.getLatestMatchScore().doubleValue());
                }
                row.createCell(4).setCellValue(dto.getAiSummary());
            }
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public byte[] exportCandidatesPdf(List<CandidateDto> rows) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(bos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.add(new Paragraph("AI Resume Screening - Candidate Report")
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(" "));

        for (CandidateDto dto : rows) {
            document.add(new Paragraph(dto.getFullName() + " (" + dto.getStatus() + ")").setBold());
            if (dto.getLatestMatchScore() != null) {
                document.add(new Paragraph("Match score: " + dto.getLatestMatchScore()));
            }
            document.add(new Paragraph(dto.getAiSummary() != null ? dto.getAiSummary() : ""));
            document.add(new Paragraph(" "));
        }
        document.close();
        return bos.toByteArray();
    }
}
