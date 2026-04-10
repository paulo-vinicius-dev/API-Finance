package br.com.pauloviniciusdeveloper.finance.alert.service;

import java.util.List;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.alert.dto.AlertResponse;

public interface AlertService {
    List<AlertResponse> getAlerts(UUID userId, int month, int year);
}
