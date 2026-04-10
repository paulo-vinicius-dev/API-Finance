package br.com.pauloviniciusdeveloper.finance.budget.entity;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.pauloviniciusdeveloper.finance.category.entity.Category;
import br.com.pauloviniciusdeveloper.finance.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "budgets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_id", "month", "year"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;
}
