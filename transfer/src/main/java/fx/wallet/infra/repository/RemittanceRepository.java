package fx.wallet.infra.repository;

import fx.wallet.infra.repository.entity.Remittance;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.annotation.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface RemittanceRepository extends JpaRepository<Remittance, UUID> {
    @Query("SELECT COALESCE(SUM(r.amountBrl), 0) FROM Remittance r WHERE r.sender.id = :senderId AND r.createdAt BETWEEN :startOfDay AND :endOfDay")
    BigDecimal findTotalAmountBySenderAndCreatedAtBetween(UUID senderId, LocalDateTime startOfDay, LocalDateTime endOfDay);
} 