package fx.wallet.infra.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    private UUID id;

    @Column(name = "balance_brl", nullable = false)
    private BigDecimal balanceBrl;

    @Column(name = "balance_usd", nullable = false)
    private BigDecimal balanceUsd;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
} 