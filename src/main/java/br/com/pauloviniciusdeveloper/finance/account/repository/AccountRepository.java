package br.com.pauloviniciusdeveloper.finance.account.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.pauloviniciusdeveloper.finance.account.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserId(UUID userId);
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
}
