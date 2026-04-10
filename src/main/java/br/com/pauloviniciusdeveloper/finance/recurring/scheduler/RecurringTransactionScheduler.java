package br.com.pauloviniciusdeveloper.finance.recurring.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.pauloviniciusdeveloper.finance.recurring.entity.RecurringTransaction;
import br.com.pauloviniciusdeveloper.finance.recurring.repository.RecurringRepository;
import br.com.pauloviniciusdeveloper.finance.recurring.service.RecurringServiceImpl;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.Transaction;
import br.com.pauloviniciusdeveloper.finance.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final RecurringRepository recurringRepository;
    private final TransactionRepository transactionRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void processOnStartup() {
        log.info("Running recurring transactions processing on startup");
        processRecurringTransactions();
    }

    @Scheduled(cron = "0 0 1 * * *") // Runs daily at 01:00
    @Transactional
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> due = recurringRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(today);

        log.info("Processing {} recurring transactions due by {}", due.size(), today);

        for (RecurringTransaction recurring : due) {
            try {
                // Loop to catch up all missed occurrences (e.g. daily recurring 3 days overdue)
                while (!recurring.getNextDueDate().isAfter(today)) {
                    Transaction transaction = Transaction.builder()
                        .account(recurring.getAccount())
                        .category(recurring.getCategory())
                        .type(recurring.getType())
                        .amount(recurring.getAmount())
                        .description(recurring.getDescription())
                        .date(recurring.getNextDueDate())
                        .build();

                    transactionRepository.save(transaction);

                    log.info("Generated transaction for recurring id={}, date={}", recurring.getId(), recurring.getNextDueDate());

                    recurring.setNextDueDate(
                        RecurringServiceImpl.computeNextDueDate(recurring.getNextDueDate(), recurring.getFrequency()));
                }

                recurringRepository.save(recurring);
                log.info("Recurring id={} updated, nextDueDate={}", recurring.getId(), recurring.getNextDueDate());
            } catch (Exception e) {
                log.error("Failed to process recurring transaction id={}", recurring.getId(), e);
            }
        }
    }
}
