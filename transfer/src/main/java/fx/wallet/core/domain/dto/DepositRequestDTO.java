package fx.wallet.core.domain.dto;

import java.math.BigDecimal;

public record DepositRequestDTO(String userId, String currency, BigDecimal amount) {
} 