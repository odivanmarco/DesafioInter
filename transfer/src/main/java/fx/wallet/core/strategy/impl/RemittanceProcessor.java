package fx.wallet.core.strategy.impl;

import fx.wallet.core.exception.RemittanceValidationException;
import fx.wallet.core.exception.UserNotFoundException;
import fx.wallet.core.exception.WalletNotFoundException;
import fx.wallet.core.service.QuotationService;
import fx.wallet.infra.repository.RemittanceRepository;
import fx.wallet.infra.repository.UserRepository;
import fx.wallet.infra.repository.WalletRepository;
import fx.wallet.infra.repository.entity.Remittance;
import fx.wallet.infra.repository.entity.User;
import fx.wallet.infra.repository.entity.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public abstract class RemittanceProcessor {

    protected final UserRepository userRepository;
    protected final WalletRepository walletRepository;
    protected final RemittanceRepository remittanceRepository;
    protected final QuotationService quotationService;

    public RemittanceProcessor(UserRepository userRepository,
                               WalletRepository walletRepository,
                               RemittanceRepository remittanceRepository,
                               QuotationService quotationService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.remittanceRepository = remittanceRepository;
        this.quotationService = quotationService;
    }

    protected User getUser(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    protected Wallet getWallet(String userId) {
        return walletRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));
    }

    protected void validateDailyLimit(User sender, BigDecimal amount) {
        var startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        var endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        var totalAmountToday = remittanceRepository.findTotalAmountBySenderAndCreatedAtBetween(
                sender.getId(), startOfDay, endOfDay
        );

        if (totalAmountToday.add(amount).compareTo(sender.getDailyLimit()) > 0) {
            throw new RemittanceValidationException("Exceeds daily limit");
        }
    }

    protected void validateBrlBalance(Wallet senderWallet, BigDecimal amountBrl) {
        if (senderWallet.getBalanceBrl().compareTo(amountBrl) < 0) {
            throw new RemittanceValidationException("Insufficient funds");
        }
    }

    protected void validateUsdBalance(Wallet senderWallet, BigDecimal amountUsd) {
        if (senderWallet.getBalanceUsd().compareTo(amountUsd) < 0) {
            throw new RemittanceValidationException("Insufficient funds");
        }
    }

    protected Remittance buildRemittance(User sender, User receiver, BigDecimal amountBrl, BigDecimal amountUsd, BigDecimal exchangeRate) {
        return Remittance.builder()
                .id(UUID.randomUUID())
                .sender(sender)
                .receiver(receiver)
                .amountBrl(amountBrl)
                .amountUsd(amountUsd)
                .exchangeRate(exchangeRate)
                .createdAt(LocalDateTime.now())
                .build();
    }
} 