package br.com.pauloviniciusdeveloper.finance.recurring.service;

import java.util.List;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.recurring.dto.RecurringRequest;
import br.com.pauloviniciusdeveloper.finance.recurring.dto.RecurringResponse;

public interface RecurringService {
    RecurringResponse create(RecurringRequest request, UUID userId);
    List<RecurringResponse> findByUserId(UUID userId);
    RecurringResponse findByIdAndUserId(UUID id, UUID userId);
    RecurringResponse update(UUID id, UUID userId, RecurringRequest request);
    void delete(UUID id, UUID userId);
}
