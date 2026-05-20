package com.airesume.screening.controller;

import com.airesume.screening.dto.CandidateDto;
import com.airesume.screening.dto.CandidateExportRequest;
import com.airesume.screening.entity.CandidateStatus;
import com.airesume.screening.exception.ApiException;
import com.airesume.screening.service.CandidateService;
import com.airesume.screening.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/reports")
@Slf4j
public class ReportController {

    private final CandidateService candidateService;
    private final ReportExportService reportExportService;

    public ReportController(CandidateService candidateService,
                            ReportExportService reportExportService) {
        this.candidateService = candidateService;
        this.reportExportService = reportExportService;
    }

    @GetMapping("/candidates.xlsx")
    @Operation(summary = "Export filtered candidates to one Excel file")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) Long jobId,
                                              @RequestParam(required = false) CandidateStatus status,
                                              @RequestParam(required = false) String q) throws IOException {
        log.info("Export filtered Excel jobId={} status={} q={}", jobId, status, q);
        List<CandidateDto> rows = candidateService.search(jobId, status, q);
        log.info("Export filtered Excel: {} row(s)", rows.size());
        byte[] bytes = reportExportService.exportCandidatesExcel(rows);
        return fileResponse(bytes, "candidates.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/candidates.pdf")
    @Operation(summary = "Export filtered candidates to one PDF file")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) Long jobId,
                                            @RequestParam(required = false) CandidateStatus status,
                                            @RequestParam(required = false) String q) throws IOException {
        log.info("Export filtered PDF jobId={} status={} q={}", jobId, status, q);
        List<CandidateDto> rows = candidateService.search(jobId, status, q);
        log.info("Export filtered PDF: {} row(s)", rows.size());
        byte[] bytes = reportExportService.exportCandidatesPdf(rows);
        return fileResponse(bytes, "candidates.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/candidates/{id}.xlsx")
    @Operation(summary = "Export one candidate to Excel")
    public ResponseEntity<byte[]> exportCandidateExcel(@PathVariable Long id) throws IOException {
        log.info("Export single Excel candidateId={}", id);
        CandidateDto dto = candidateService.get(id);
        byte[] bytes = reportExportService.exportSingleCandidateExcel(dto);
        return fileResponse(bytes, fileName(dto, "xlsx"), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/candidates/{id}.pdf")
    @Operation(summary = "Export one candidate to PDF")
    public ResponseEntity<byte[]> exportCandidatePdf(@PathVariable Long id) throws IOException {
        log.info("Export single PDF candidateId={}", id);
        CandidateDto dto = candidateService.get(id);
        byte[] bytes = reportExportService.exportSingleCandidatePdf(dto);
        return fileResponse(bytes, fileName(dto, "pdf"), MediaType.APPLICATION_PDF_VALUE);
    }

    @PostMapping({"/candidates/bulk-export", "/candidates/export.zip"})
    @Operation(summary = "Bulk export selected candidates as ZIP (per-candidate files + summary)")
    public ResponseEntity<byte[]> exportBulkZip(@Valid @RequestBody CandidateExportRequest request) throws IOException {
        List<Long> ids = request.getIds();
        List<String> formats = request.getFormats();
        log.info("Bulk ZIP export requested: ids={}, formats={}", ids, formats);

        if (ids == null || ids.isEmpty()) {
            log.warn("Bulk export rejected: empty ids");
            throw new ApiException(HttpStatus.BAD_REQUEST, "Select at least one candidate to export");
        }

        List<CandidateDto> rows = new ArrayList<>();
        for (Long id : ids) {
            try {
                rows.add(candidateService.get(id));
            } catch (ApiException ex) {
                log.error("Bulk export failed loading candidate id={}: {}", id, ex.getMessage());
                throw new ApiException(HttpStatus.NOT_FOUND,
                        "Candidate not found (id=" + id + "). Refresh the list and try again.");
            }
        }

        try {
            byte[] bytes = reportExportService.exportCandidatesZip(rows, formats);
            log.info("Bulk ZIP export success: {} bytes for ids={}", bytes.length, ids);
            return fileResponse(bytes, "candidates-export.zip", "application/zip");
        } catch (IOException ex) {
            log.error("Bulk ZIP export IO error for ids={}: {}", ids, ex.getMessage(), ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Bulk ZIP export failed for ids={}", ids, ex);
            throw ex;
        }
    }

    private static ResponseEntity<byte[]> fileResponse(byte[] bytes, String filename, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }

    private static String fileName(CandidateDto dto, String ext) {
        String name = dto.getFullName() != null ? dto.getFullName() : "candidate";
        String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (slug.isBlank()) {
            slug = "candidate";
        }
        return "candidate-" + dto.getId() + "-" + slug + "." + ext;
    }
}
