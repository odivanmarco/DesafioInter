package fx.wallet.core.domain.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record DepositRequestDTO(
    String userId, 
    String currency, 
    BigDecimal amount,
    String password
) {}

