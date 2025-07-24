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
public class BrlToUsdRemittanceStrategy extends RemittanceProcessor implements RemittanceStrategy {

    public BrlToUsdRemittanceStrategy(UserRepository userRepository, WalletRepository walletRepository, RemittanceRepository remittanceRepository, QuotationService quotationService) {
        super(userRepository, walletRepository, remittanceRepository, quotationService);
    }

    @Override
    public void execute(RemittanceRequestDTO remittanceRequest) {
        final var sender = getUser(remittanceRequest.senderId());
        validatePassword(sender, remittanceRequest.password());
        final var receiver = getUser(remittanceRequest.receiverId());
        final var senderWallet = getWallet(remittanceRequest.senderId());
        final var receiverWallet = getWallet(remittanceRequest.receiverId());

        validateDailyLimit(sender, remittanceRequest.amount());
        validateBrlBalance(senderWallet, remittanceRequest.amount());

        final var exchangeRate = getExchangeRate();
        final var amountUsd = getAmountUsd(remittanceRequest.amount(), exchangeRate);

        senderWallet.setBalanceBrl(senderWallet.getBalanceBrl().subtract(remittanceRequest.amount()));
        receiverWallet.setBalanceUsd(receiverWallet.getBalanceUsd().add(amountUsd));

        walletRepository.update(senderWallet);
        walletRepository.update(receiverWallet);

        final var remittance = buildRemittance(sender, receiver, remittanceRequest.amount(), amountUsd, exchangeRate);
        remittanceRepository.save(remittance);
    }

    private BigDecimal getAmountUsd(BigDecimal amountBrl, BigDecimal exchangeRate) {
        return amountBrl.divide(exchangeRate, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getExchangeRate() {
        final var quotationResponse = quotationService.getTodayQuotation();
        final var quotation = quotationResponse.quotations().getFirst();
        return quotation.purchaseRate();
    }
}
