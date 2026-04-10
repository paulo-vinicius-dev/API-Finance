package br.com.pauloviniciusdeveloper.finance.alert.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.pauloviniciusdeveloper.finance.alert.dto.AlertResponse;
import br.com.pauloviniciusdeveloper.finance.alert.service.AlertService;
import br.com.pauloviniciusdeveloper.finance.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Alertas financeiros automáticos")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Obter alertas financeiros do período (padrão: mês e ano atual)")
    public ResponseEntity<List<AlertResponse>> getAlerts(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year
    ) {
        UUID userId = currentUser.getId();
        LocalDate now = LocalDate.now();
        int targetMonth = month != null ? month : now.getMonthValue();
        int targetYear = year != null ? year : now.getYear();
        return ResponseEntity.ok(alertService.getAlerts(userId, targetMonth, targetYear));
    }
}
