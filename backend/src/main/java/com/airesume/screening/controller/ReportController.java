package com.airesume.screening.controller;

import com.airesume.screening.dto.CandidateDto;
import com.airesume.screening.entity.CandidateStatus;
import com.airesume.screening.service.CandidateService;
import com.airesume.screening.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final CandidateService candidateService;
    private final ReportExportService reportExportService;

    public ReportController(CandidateService candidateService,
                            ReportExportService reportExportService) {
        this.candidateService = candidateService;
        this.reportExportService = reportExportService;
    }

    @GetMapping("/candidates.xlsx")
    @Operation(summary = "Export filtered candidates to Excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) Long jobId,
                                              @RequestParam(required = false) CandidateStatus status,
                                              @RequestParam(required = false) String q) throws IOException {
        List<CandidateDto> rows = candidateService.search(jobId, status, q);
        byte[] bytes = reportExportService.exportCandidatesExcel(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=candidates.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/candidates.pdf")
    @Operation(summary = "Export filtered candidates to PDF")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) Long jobId,
                                            @RequestParam(required = false) CandidateStatus status,
                                            @RequestParam(required = false) String q) throws IOException {
        List<CandidateDto> rows = candidateService.search(jobId, status, q);
        byte[] bytes = reportExportService.exportCandidatesPdf(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=candidates.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
