package fx.wallet.core.domain.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DepositResponseDTO(
    String id, 
    BigDecimal balanceBrl, 
    BigDecimal balanceUsd
) {
}