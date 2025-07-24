package fx.wallet.core.strategy.impl;

import fx.wallet.core.domain.dto.RemittanceRequestDTO;
import fx.wallet.core.service.QuotationService;
import fx.wallet.core.strategy.RemittanceStrategy;
import fx.wallet.infra.repository.RemittanceRepository;
import fx.wallet.infra.repository.UserRepository;
import fx.wallet.infra.repository.WalletRepository;
import jakarta.inject.Singleton;

import java.math.BigDecimal;

@Singleton
public class BrlToBrlRemittanceStrategy extends RemittanceProcessor implements RemittanceStrategy {


    public BrlToBrlRemittanceStrategy(UserRepository userRepository, WalletRepository walletRepository, RemittanceRepository remittanceRepository, QuotationService quotationService) {
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

        senderWallet.setBalanceBrl(senderWallet.getBalanceBrl().subtract(remittanceRequest.amount()));
        receiverWallet.setBalanceBrl(receiverWallet.getBalanceBrl().add(remittanceRequest.amount()));

        walletRepository.update(senderWallet);
        walletRepository.update(receiverWallet);

        final var remittance = buildRemittance(sender, receiver, remittanceRequest.amount(), BigDecimal.ZERO, BigDecimal.ZERO);
        remittanceRepository.save(remittance);
    }

}
