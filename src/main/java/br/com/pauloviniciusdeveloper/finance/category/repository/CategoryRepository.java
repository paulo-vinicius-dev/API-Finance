package br.com.pauloviniciusdeveloper.finance.category.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import br.com.pauloviniciusdeveloper.finance.category.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

	@Query("""
		SELECT c FROM Category c
		WHERE c.user.id = :userId OR c.isDefault = true
	""")
	List<Category> findByUserIdOrDefault(@Param("userId") UUID userId);

	@Query("""
		SELECT c FROM Category c
		WHERE c.id = :id
		  AND (c.user.id = :userId OR c.isDefault = true)
	""")
	Optional<Category> findByIdAndUserIdOrDefault(
		@Param("id") UUID id,
		@Param("userId") UUID userId
	);

	Optional<Category> findByIdAndUserId(UUID id, UUID userId);
}
