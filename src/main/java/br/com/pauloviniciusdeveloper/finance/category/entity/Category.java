package br.com.pauloviniciusdeveloper.finance.category.entity;

import java.util.UUID;

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
@Table(name = "categories", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"name", "user_id"})
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}