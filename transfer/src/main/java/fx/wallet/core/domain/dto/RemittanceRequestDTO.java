package fx.wallet.core.domain.dto;

import java.math.BigDecimal;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record RemittanceRequestDTO(
    String senderId, 
    String receiverId, 
    BigDecimal amount
) { } 