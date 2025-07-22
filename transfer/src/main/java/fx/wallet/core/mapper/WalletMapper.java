package fx.wallet.core.mapper;

import fx.wallet.core.domain.dto.DepositResponseDTO;
import fx.wallet.infra.repository.entity.Wallet;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;   
@Mapper(componentModel = "jsr330")
public interface WalletMapper {

    @Mapping(target = "id", source = "userId")
    @Mapping(target = "balanceBrl", source = "wallet.balanceBrl")
    @Mapping(target = "balanceUsd", source = "wallet.balanceUsd")
    DepositResponseDTO toResponseDTO(Wallet wallet, String userId);
}
