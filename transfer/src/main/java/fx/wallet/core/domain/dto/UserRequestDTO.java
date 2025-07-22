package fx.wallet.core.domain.dto;

import java.math.BigDecimal;

public record UserRequestDTO(
    String firstName,
    String middleName,
    String lastName,
    String email,
    String password,
    String document,
    String userType,
    BigDecimal dailyLimit
) {}
