package fx.wallet.core.mapper;

import fx.wallet.core.domain.dto.UserRequestDTO;
import fx.wallet.core.domain.dto.UserResponseDTO;
import fx.wallet.infra.repository.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.UUID;

import static fx.wallet.ApplicationConstants.PJ_USER_TYPE;

@Mapper(componentModel = "jsr330")
public interface UserMapper {

    @Mapping(target = "id", expression = "java(generateId())")
    @Mapping(target = "dailyLimit", expression = "java(setDailyLimit(userDTO.userType()))")
    User toEntity(UserRequestDTO userDTO);

    UserRequestDTO toRequestDTO(User user);

    @Mapping(target = "balanceBrl", ignore = true)
    @Mapping(target = "balanceUsd", ignore = true)
    UserResponseDTO toResponseDTO(User user);

    default BigDecimal setDailyLimit(String userType) {
        return PJ_USER_TYPE.equals(userType) ? BigDecimal.valueOf(50000) : BigDecimal.valueOf(10000);
    }

    default UUID generateId() {
        return UUID.randomUUID();
    }
}
