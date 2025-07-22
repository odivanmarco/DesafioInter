package fx.wallet.core.domain.dto;

import java.math.BigDecimal;

public record DepositResponseDTO(String id, BigDecimal balanceBrl, BigDecimal balanceUsd) {
}