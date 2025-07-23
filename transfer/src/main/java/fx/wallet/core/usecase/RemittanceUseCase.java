package fx.wallet.core.usecase;

import fx.wallet.core.domain.dto.RemittanceRequestDTO;

public interface RemittanceUseCase {
    void execute(RemittanceRequestDTO remittanceRequest);
}
