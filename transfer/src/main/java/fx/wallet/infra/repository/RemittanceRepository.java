package fx.wallet.infra.repository;

import fx.wallet.infra.repository.entity.Remittance;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.UUID;

@Repository
public interface RemittanceRepository extends JpaRepository<Remittance, UUID> {
} 