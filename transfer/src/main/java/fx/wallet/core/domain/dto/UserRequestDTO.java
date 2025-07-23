package fx.wallet.core.domain.dto;

import lombok.Builder;

@Builder
public record UserRequestDTO(
    String firstName,
    String middleName,
    String lastName,
    String email,
    String password,
    String document,
    String userType
) {}
