package fx.wallet.core.strategy;

import fx.wallet.core.domain.dto.RemittanceRequestDTO;

public interface RemittanceStrategy {
    void execute(RemittanceRequestDTO remittanceRequest);
}
