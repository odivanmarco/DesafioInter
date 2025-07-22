package fx.wallet.infra.repository;

import fx.wallet.infra.repository.entity.Wallet;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends CrudRepository<Wallet, UUID> {
    Optional<Wallet> findByUserId(UUID userId);
} 