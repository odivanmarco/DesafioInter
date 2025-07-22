package fx.wallet.core.domain.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;

@Builder(toBuilder = true)
public record UserResponseDTO(
        UUID id,
        String firstName,
        String middleName,
        String lastName,
        String email,
        String document,
        String userType,
        BigDecimal dailyLimit,
        BigDecimal balanceBrl,
        BigDecimal balanceUsd
) {
} 