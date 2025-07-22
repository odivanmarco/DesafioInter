package fx.wallet.infra.repository;

import fx.wallet.infra.repository.entity.User;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
    boolean existsByDocument(String document);
    boolean existsByEmail(String email);
} 