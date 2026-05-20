package com.airesume.screening.service;

import com.airesume.screening.dto.CandidateDto;
import com.airesume.screening.dto.CandidateOverviewDto;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class ReportExportService {

    private static final int EXCEL_CELL_MAX = 32_000;

    public byte[] exportCandidatesExcel(List<CandidateDto> rows) throws IOException {
        return buildWorkbookBytes(rows);
    }

    public byte[] exportCandidatesPdf(List<CandidateDto> rows) throws IOException {
        return buildPdfBytes(rows);
    }

    public byte[] exportSingleCandidateExcel(CandidateDto dto) throws IOException {
        return buildWorkbookBytes(List.of(dto));
    }

    public byte[] exportSingleCandidatePdf(CandidateDto dto) throws IOException {
        return buildPdfBytes(List.of(dto));
    }

    public byte[] exportCandidatesZip(List<CandidateDto> rows, List<String> formats) throws IOException {
        Set<String> wanted = normalizeFormats(formats);
        log.debug("Building ZIP for {} candidate(s), formats={}", rows.size(), wanted);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bos)) {
            for (CandidateDto dto : rows) {
                String base = fileBaseName(dto);
                if (wanted.contains("xlsx")) {
                    String entryName = base + ".xlsx";
                    log.debug("ZIP entry: {}", entryName);
                    addZipEntry(zip, entryName, buildWorkbookBytes(List.of(dto)));
                }
                if (wanted.contains("pdf")) {
                    String entryName = base + ".pdf";
                    log.debug("ZIP entry: {}", entryName);
                    addZipEntry(zip, entryName, buildPdfBytes(List.of(dto)));
                }
            }
            if (rows.size() > 1) {
                if (wanted.contains("xlsx")) {
                    addZipEntry(zip, "all-candidates-summary.xlsx", buildWorkbookBytes(rows));
                }
                if (wanted.contains("pdf")) {
                    addZipEntry(zip, "all-candidates-summary.pdf", buildPdfBytes(rows));
                }
            }
        }
        byte[] result = bos.toByteArray();
        log.info("ZIP export complete: {} bytes, {} candidates", result.length, rows.size());
        return result;
    }

    private static Set<String> normalizeFormats(List<String> formats) {
        Set<String> wanted = new LinkedHashSet<>();
        if (formats == null || formats.isEmpty()) {
            wanted.add("xlsx");
            wanted.add("pdf");
            return wanted;
        }
        for (String f : formats) {
            if (f != null) {
                String lower = f.trim().toLowerCase();
                if ("xlsx".equals(lower) || "pdf".equals(lower)) {
                    wanted.add(lower);
                }
            }
        }
        if (wanted.isEmpty()) {
            wanted.add("xlsx");
            wanted.add("pdf");
        }
        return wanted;
    }

    private static void addZipEntry(ZipOutputStream zip, String name, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(sanitizeZipEntryName(name));
        zip.putNextEntry(entry);
        zip.write(data);
        zip.closeEntry();
    }

    /** ZIP entry names must not contain path separators or invalid chars. */
    private static String sanitizeZipEntryName(String name) {
        return name.replace('\\', '-').replace('/', '-');
    }

    private static String fileBaseName(CandidateDto dto) {
        String name = dto.getFullName() != null ? dto.getFullName() : "candidate";
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (!StringUtils.hasText(slug)) {
            slug = "candidate";
        }
        return "candidate-" + dto.getId() + "-" + slug;
    }

    private byte[] buildWorkbookBytes(List<CandidateDto> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Candidates");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Email");
            header.createCell(3).setCellValue("Status");
            header.createCell(4).setCellValue("Match Score");
            header.createCell(5).setCellValue("Job");
            header.createCell(6).setCellValue("Matching Skills");
            header.createCell(7).setCellValue("Missing Skills");
            header.createCell(8).setCellValue("Executive Summary");
            header.createCell(9).setCellValue("Interview Verdict");

            int r = 1;
            for (CandidateDto dto : rows) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(dto.getId() != null ? dto.getId().doubleValue() : 0);
                row.createCell(1).setCellValue(truncate(nullToEmpty(dto.getFullName())));
                row.createCell(2).setCellValue(truncate(nullToEmpty(dto.getEmail())));
                row.createCell(3).setCellValue(dto.getStatus() != null ? dto.getStatus().name() : "");
                if (dto.getLatestMatchScore() != null) {
                    row.createCell(4).setCellValue(dto.getLatestMatchScore().doubleValue());
                }
                CandidateOverviewDto ov = dto.getOverview();
                row.createCell(5).setCellValue(truncate(ov != null ? nullToEmpty(ov.getJobTitle()) : ""));
                row.createCell(6).setCellValue(truncate(nullToEmpty(dto.getMatchingSkills())));
                row.createCell(7).setCellValue(truncate(nullToEmpty(dto.getMissingSkills())));
                row.createCell(8).setCellValue(truncate(overviewSummary(dto)));
                row.createCell(9).setCellValue(truncate(ov != null ? nullToEmpty(ov.getInterviewVerdict()) : ""));
            }
            safeAutoSizeColumns(sheet, 10);
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private static void safeAutoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            try {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) > 20000) {
                    sheet.setColumnWidth(i, 20000);
                }
            } catch (Exception ex) {
                log.warn("autoSizeColumn failed for column {}: {}", i, ex.getMessage());
                sheet.setColumnWidth(i, 8000);
            }
        }
    }

    private byte[] buildPdfBytes(List<CandidateDto> rows) throws IOException {
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
            document.add(new Paragraph(
                    truncate(nullToEmpty(dto.getFullName())) + " (" + (dto.getStatus() != null ? dto.getStatus() : "") + ")")
                    .setBold());
            if (dto.getLatestMatchScore() != null) {
                document.add(new Paragraph("Match score: " + dto.getLatestMatchScore() + "%"));
            }
            CandidateOverviewDto ov = dto.getOverview();
            if (ov != null && StringUtils.hasText(ov.getJobTitle())) {
                document.add(new Paragraph("Job: " + truncate(ov.getJobTitle())));
            }
            if (StringUtils.hasText(dto.getMatchingSkills())) {
                document.add(new Paragraph("Aligned: " + truncate(dto.getMatchingSkills())));
            }
            if (StringUtils.hasText(dto.getMissingSkills())) {
                document.add(new Paragraph("Gaps: " + truncate(dto.getMissingSkills())));
            }
            document.add(new Paragraph(truncate(overviewSummary(dto))));
            document.add(new Paragraph(" "));
        }
        document.close();
        return bos.toByteArray();
    }

    private static String overviewSummary(CandidateDto dto) {
        if (dto.getOverview() != null && StringUtils.hasText(dto.getOverview().getExecutiveSummary())) {
            return dto.getOverview().getExecutiveSummary();
        }
        return nullToEmpty(dto.getAiSummary());
    }

    private static String truncate(String value) {
        if (value == null) {
            return "";
        }
        if (value.length() <= EXCEL_CELL_MAX) {
            return value;
        }
        return value.substring(0, EXCEL_CELL_MAX) + "…";
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
