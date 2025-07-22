package fx.wallet.core.mapper;

import fx.wallet.core.domain.dto.UserRequestDTO;
import fx.wallet.core.domain.dto.UserResponseDTO;
import fx.wallet.infra.repository.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "jsr330", imports = UUID.class)
public interface UserMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    User toEntity(UserRequestDTO userDTO);

    UserRequestDTO toRequestDTO(User user);

    UserResponseDTO toResponseDTO(User user);
}
