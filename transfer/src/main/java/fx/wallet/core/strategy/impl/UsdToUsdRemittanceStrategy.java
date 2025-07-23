package fx.wallet.core.strategy.impl;

import fx.wallet.core.domain.dto.RemittanceRequestDTO;
import fx.wallet.core.service.QuotationService;
import fx.wallet.core.strategy.RemittanceStrategy;
import fx.wallet.infra.repository.RemittanceRepository;
import fx.wallet.infra.repository.UserRepository;
import fx.wallet.infra.repository.WalletRepository;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Singleton
public class UsdToUsdRemittanceStrategy extends RemittanceProcessor implements RemittanceStrategy {

    public UsdToUsdRemittanceStrategy(UserRepository userRepository, WalletRepository walletRepository, RemittanceRepository remittanceRepository, QuotationService quotationService) {
        super(userRepository, walletRepository, remittanceRepository, quotationService);
    }

    @Override
    public void execute(RemittanceRequestDTO remittanceRequest) {
        final var sender = getUser(remittanceRequest.senderId());
        final var receiver = getUser(remittanceRequest.receiverId());
        final var senderWallet = getWallet(remittanceRequest.senderId());
        final var receiverWallet = getWallet(remittanceRequest.receiverId());

        validateUsdBalance(senderWallet, remittanceRequest.amount());

        final var exchangeRate = getExchangeRate();
        final var amountBrl = getAmountBrl(remittanceRequest.amount(), exchangeRate);

        validateDailyLimit(sender, amountBrl);


        senderWallet.setBalanceUsd(senderWallet.getBalanceUsd().subtract(remittanceRequest.amount()));
        receiverWallet.setBalanceUsd(receiverWallet.getBalanceUsd().add(remittanceRequest.amount()));

        walletRepository.update(senderWallet);
        walletRepository.update(receiverWallet);

        final var remittance = buildRemittance(sender, receiver, amountBrl, remittanceRequest.amount(), exchangeRate);
        remittanceRepository.save(remittance);
    }


    private BigDecimal getAmountBrl(BigDecimal amountUsd, BigDecimal exchangeRate) {
        return amountUsd.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getExchangeRate() {
        final var quotationResponse = quotationService.getTodayQuotation();
        final var quotation = quotationResponse.quotations().getFirst();
        return quotation.sellingRate();
    }
}
