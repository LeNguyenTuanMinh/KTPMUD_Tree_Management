package com.beepollen.controller;

import com.beepollen.dto.HarvestReportDTO;
import com.beepollen.service.CollectionTrackingService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@Controller
@RequestMapping("/collections/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final CollectionTrackingService trackingService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String showReport(@RequestParam(required = false) LocalDate fromDate,
                             @RequestParam(required = false) LocalDate toDate,
                             Model model) {
        if (toDate == null) {
            toDate = LocalDate.now();
        }
        if (fromDate == null) {
            fromDate = toDate.minusDays(30);
        }

        HarvestReportDTO reportData = trackingService.generateReport(fromDate, toDate);
        
        model.addAttribute("reportData", reportData);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("activeMenu", "collections");

        return "collections/report";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/export/csv")
    public void exportReportCsv(@RequestParam(required = false) LocalDate fromDate,
                                @RequestParam(required = false) LocalDate toDate,
                                HttpServletResponse response) throws IOException {
        if (toDate == null) {
            toDate = LocalDate.now();
        }
        if (fromDate == null) {
            fromDate = toDate.minusDays(30);
        }

        HarvestReportDTO report = trackingService.generateReport(fromDate, toDate);

        String filename = "harvest_report_" + fromDate + "_" + toDate + ".csv";
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            // Section 1 - SUMMARY
            writer.println("SUMMARY");
            writer.printf("Total Weight (kg),%.3f%n", report.getTotalWeightKg());
            writer.printf("Total Records,%d%n", report.getTotalRecords());
            writer.printf("Top Colony,%s%n", escapeCsv(report.getTopColonyCode()));
            writer.printf("Top Pollen,%s%n", escapeCsv(report.getTopPollenName()));
            writer.println();

            // Section 2 - BY COLONY
            writer.println("BY COLONY");
            writer.println("Colony Code,Location,Total Weight (kg),Records,% of Total");
            for (HarvestReportDTO.ColonySummary cs : report.getColonySummaries()) {
                double pct = report.getTotalWeightKg() > 0 ? (cs.getTotalWeight() / report.getTotalWeightKg()) * 100 : 0;
                writer.printf("\"%s\",\"%s\",%.3f,%d,%.1f%%%n",
                        escapeCsv(cs.getColonyCode()),
                        escapeCsv(cs.getLocation()),
                        cs.getTotalWeight(),
                        cs.getRecordCount(),
                        pct);
            }
            writer.println();

            // Section 3 - BY POLLEN
            writer.println("BY POLLEN");
            writer.println("Pollen Name,Color Code,Total Weight (kg),Records,% of Total");
            for (HarvestReportDTO.PollenSummary ps : report.getPollenSummaries()) {
                double pct = report.getTotalWeightKg() > 0 ? (ps.getTotalWeight() / report.getTotalWeightKg()) * 100 : 0;
                writer.printf("\"%s\",\"%s\",%.3f,%d,%.1f%%%n",
                        escapeCsv(ps.getPollenName()),
                        escapeCsv(ps.getColorCode()),
                        ps.getTotalWeight(),
                        ps.getRecordCount(),
                        pct);
            }
        }
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        return data.replace("\"", "\"\"");
    }
}
