package fx.wallet.core.domain.dto;

import java.math.BigDecimal;

import fx.wallet.core.enums.TransferType;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;

@Builder
@Introspected
public record RemittanceRequestDTO(
    String senderId, 
    String receiverId, 
    BigDecimal amount,
    TransferType transferType
) { } 