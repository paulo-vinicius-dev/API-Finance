package br.com.pauloviniciusdeveloper.finance.insight.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import br.com.pauloviniciusdeveloper.finance.insight.dto.InsightResponse;
import br.com.pauloviniciusdeveloper.finance.insight.service.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
@Tag(name = "Insights", description = "Análises automáticas sobre o comportamento financeiro")
public class InsightController {

    private final InsightService insightService;

    @GetMapping
    @Operation(summary = "Obter insights financeiros do período (padrão: mês e ano atual)")
    public ResponseEntity<List<InsightResponse>> getInsights(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year
    ) {
        UUID userId = currentUser.getId();
        LocalDate now = LocalDate.now();
        int targetMonth = month != null ? month : now.getMonthValue();
        int targetYear = year != null ? year : now.getYear();
        return ResponseEntity.ok(insightService.getInsights(userId, targetMonth, targetYear));
    }
}
