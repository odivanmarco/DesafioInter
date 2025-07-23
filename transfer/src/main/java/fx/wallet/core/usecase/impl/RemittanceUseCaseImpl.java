package fx.wallet.core.usecase.impl;

import fx.wallet.core.domain.dto.RemittanceRequestDTO;
import fx.wallet.core.strategy.factory.RemittanceFactory;
import fx.wallet.core.usecase.RemittanceUseCase;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
public class RemittanceUseCaseImpl implements RemittanceUseCase {

    private final RemittanceFactory remittanceFactory;

    public RemittanceUseCaseImpl(RemittanceFactory remittanceFactory) {
        this.remittanceFactory = remittanceFactory;
    }

    @Override
    @Transactional
    public void execute(RemittanceRequestDTO remittanceRequest) {
        remittanceFactory.getStrategy(remittanceRequest.transferType()).execute(remittanceRequest);
    }
}
