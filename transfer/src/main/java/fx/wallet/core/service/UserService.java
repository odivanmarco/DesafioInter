package fx.wallet.core.service;

import fx.wallet.core.domain.dto.UserRequestDTO;
import fx.wallet.core.domain.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO userDTO);
} 