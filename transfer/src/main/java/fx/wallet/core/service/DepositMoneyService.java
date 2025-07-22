package fx.wallet.core.service;

import fx.wallet.core.domain.dto.DepositRequestDTO;
import fx.wallet.core.domain.dto.DepositResponseDTO;


public interface DepositMoneyService {
    DepositResponseDTO depositMoney(DepositRequestDTO depositRequestDTO);
} 