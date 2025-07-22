package fx.wallet.core.service;

import fx.wallet.core.domain.dto.RemittanceRequestDTO;

public interface RemittanceService {
    void send(RemittanceRequestDTO remittanceRequest);
} 