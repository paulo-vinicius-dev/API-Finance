package br.com.pauloviniciusdeveloper.finance.transaction.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.pauloviniciusdeveloper.finance.transaction.entity.Transaction;
import br.com.pauloviniciusdeveloper.finance.transaction.entity.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

	List<Transaction> findByAccountUserId(UUID userId);

	Optional<Transaction> findByIdAndAccountUserId(UUID id, UUID userId);

	void deleteByAccountId(UUID accountId);

	void deleteByAccountUserId(UUID userId);

	void deleteByCategoryId(UUID categoryId);

	@Query("""
		SELECT t FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.date >= COALESCE(:startDate, t.date)
		  AND t.date <= COALESCE(:endDate, t.date)
		  AND t.category.id = COALESCE(:categoryId, t.category.id)
		  AND t.type = COALESCE(:type, t.type)
		  AND t.account.id = COALESCE(:accountId, t.account.id)
		ORDER BY t.date DESC
		""")
	List<Transaction> findByUserIdWithFilters(
		@Param("userId") UUID userId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("categoryId") UUID categoryId,
		@Param("type") TransactionType type,
		@Param("accountId") UUID accountId
	);

	@Query("""
		SELECT COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		""")
	BigDecimal sumAmountByUserIdAndType(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type
	);

	@Query("""
		SELECT COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		  AND t.date >= :startDate
		""")
	BigDecimal sumAmountByUserIdAndTypeAndDateFrom(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type,
		@Param("startDate") LocalDate startDate
	);

	@Query("""
		SELECT COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		  AND t.date <= :endDate
		""")
	BigDecimal sumAmountByUserIdAndTypeAndDateTo(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type,
		@Param("endDate") LocalDate endDate
	);

	@Query("""
		SELECT COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		  AND t.date BETWEEN :startDate AND :endDate
		""")
	BigDecimal sumAmountByUserIdAndTypeAndDateBetween(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	default BigDecimal sumAmountByUserIdAndType(
		UUID userId,
		TransactionType type,
		LocalDate startDate,
		LocalDate endDate
	) {
		if (startDate == null && endDate == null) {
			return sumAmountByUserIdAndType(userId, type);
		}
		if (startDate == null) {
			return sumAmountByUserIdAndTypeAndDateTo(userId, type, endDate);
		}
		if (endDate == null) {
			return sumAmountByUserIdAndTypeAndDateFrom(userId, type, startDate);
		}
		return sumAmountByUserIdAndTypeAndDateBetween(userId, type, startDate, endDate);
	}

	@Query("""
		SELECT t.category.id, t.category.name, COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		GROUP BY t.category.id, t.category.name
		ORDER BY SUM(t.amount) DESC
		""")
	List<Object[]> sumAmountByCategoryAndUserIdAndType(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type
	);

	@Query("""
		SELECT t.category.id, t.category.name, COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		  AND t.date >= :startDate
		GROUP BY t.category.id, t.category.name
		ORDER BY SUM(t.amount) DESC
		""")
	List<Object[]> sumAmountByCategoryAndUserIdAndTypeAndDateFrom(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type,
		@Param("startDate") LocalDate startDate
	);

	@Query("""
		SELECT t.category.id, t.category.name, COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		  AND t.date <= :endDate
		GROUP BY t.category.id, t.category.name
		ORDER BY SUM(t.amount) DESC
		""")
	List<Object[]> sumAmountByCategoryAndUserIdAndTypeAndDateTo(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type,
		@Param("endDate") LocalDate endDate
	);

	@Query("""
		SELECT t.category.id, t.category.name, COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		  AND t.date BETWEEN :startDate AND :endDate
		GROUP BY t.category.id, t.category.name
		ORDER BY SUM(t.amount) DESC
		""")
	List<Object[]> sumAmountByCategoryAndUserIdAndTypeAndDateBetween(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	default List<Object[]> sumAmountByCategoryAndUserIdAndType(
		UUID userId,
		TransactionType type,
		LocalDate startDate,
		LocalDate endDate
	) {
		if (startDate == null && endDate == null) {
			return sumAmountByCategoryAndUserIdAndType(userId, type);
		}
		if (startDate == null) {
			return sumAmountByCategoryAndUserIdAndTypeAndDateTo(userId, type, endDate);
		}
		if (endDate == null) {
			return sumAmountByCategoryAndUserIdAndTypeAndDateFrom(userId, type, startDate);
		}
		return sumAmountByCategoryAndUserIdAndTypeAndDateBetween(userId, type, startDate, endDate);
	}

	@Query("""
		SELECT MONTH(t.date), COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		  AND YEAR(t.date) = :year
		GROUP BY MONTH(t.date)
		ORDER BY MONTH(t.date)
		""")
	List<Object[]> sumAmountByMonthAndUserIdAndType(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type,
		@Param("year") int year
	);

	@Query("""
		SELECT COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		  AND YEAR(t.date) = :year
		  AND MONTH(t.date) = :month
		""")
	BigDecimal sumAmountByUserIdAndTypeAndYearAndMonth(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type,
		@Param("year") int year,
		@Param("month") int month
	);

	@Query("""
		SELECT COALESCE(SUM(t.amount), 0)
		FROM Transaction t
		WHERE t.account.user.id = :userId
		  AND t.type = :type
		  AND t.category.id = :categoryId
		  AND YEAR(t.date) = :year
		  AND MONTH(t.date) = :month
		""")
	BigDecimal sumAmountByUserIdAndTypeAndCategoryAndYearAndMonth(
		@Param("userId") UUID userId,
		@Param("type") TransactionType type,
		@Param("categoryId") UUID categoryId,
		@Param("year") int year,
		@Param("month") int month
	);
}
