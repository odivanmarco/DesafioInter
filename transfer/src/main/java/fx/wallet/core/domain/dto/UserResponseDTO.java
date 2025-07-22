package fx.wallet.core.domain.dto;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String firstName,
        String middleName,
        String lastName,
        String email,
        String document,
        String userType
) {
} 