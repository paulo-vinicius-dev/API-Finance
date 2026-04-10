package br.com.pauloviniciusdeveloper.finance.report.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import br.com.pauloviniciusdeveloper.finance.report.dto.CategoryBreakdownResponse;
import br.com.pauloviniciusdeveloper.finance.report.dto.MonthlyEvolutionResponse;
import br.com.pauloviniciusdeveloper.finance.report.dto.SummaryResponse;
import br.com.pauloviniciusdeveloper.finance.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Relatórios financeiros")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "Resumo financeiro: total de receitas, despesas, saldo e taxa de economia")
    public ResponseEntity<SummaryResponse> getSummary(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.ok(reportService.getSummary(userId, startDate, endDate));
    }

    @GetMapping("/by-category")
    @Operation(summary = "Despesas agrupadas por categoria com percentual")
    public ResponseEntity<List<CategoryBreakdownResponse>> getExpensesByCategory(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        UUID userId = currentUser.getId();
        return ResponseEntity.ok(reportService.getExpensesByCategory(userId, startDate, endDate));
    }

    @GetMapping("/monthly-evolution")
    @Operation(summary = "Evolução mensal de receitas e despesas para o ano informado")
    public ResponseEntity<List<MonthlyEvolutionResponse>> getMonthlyEvolution(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @RequestParam(required = false) Integer year
    ) {
        UUID userId = currentUser.getId();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(reportService.getMonthlyEvolution(userId, targetYear));
    }
}
