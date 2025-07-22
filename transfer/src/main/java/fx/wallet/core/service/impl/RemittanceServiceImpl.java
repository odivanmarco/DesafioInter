package fx.wallet.core.service.impl;

import fx.wallet.core.domain.dto.RemittanceRequestDTO;
import fx.wallet.core.exception.RemittanceValidationException;
import fx.wallet.core.exception.UserNotFoundException;
import fx.wallet.core.exception.WalletNotFoundException;
import fx.wallet.core.service.QuotationService;
import fx.wallet.core.service.RemittanceService;
import fx.wallet.infra.repository.RemittanceRepository;
import fx.wallet.infra.repository.UserRepository;
import fx.wallet.infra.repository.WalletRepository;
import fx.wallet.infra.repository.entity.Remittance;
import fx.wallet.infra.repository.entity.User;
import fx.wallet.infra.repository.entity.Wallet;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Singleton
public class RemittanceServiceImpl implements RemittanceService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final RemittanceRepository remittanceRepository;
    private final QuotationService quotationService;

    public RemittanceServiceImpl(UserRepository userRepository, WalletRepository walletRepository, RemittanceRepository remittanceRepository, QuotationService quotationService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.remittanceRepository = remittanceRepository;
        this.quotationService = quotationService;
    }


    @Override
    @Transactional
    public void send(RemittanceRequestDTO dto) {
        final var sender = getUser(dto.senderId());
        final var receiver = getUser(dto.receiverId());
        final var senderWallet = getWallet(dto.senderId());
        final var receiverWallet = getWallet(dto.receiverId());

        validateDailyLimit(sender, dto.amount());
        validateBalance(senderWallet, dto.amount());

        final var exchangeRate = getExchangeRate(dto.amount());
        final var amountUsd = getAmountUsd(dto.amount(), exchangeRate);
     
        senderWallet.setBalanceBrl(senderWallet.getBalanceBrl().subtract(dto.amount()));
        walletRepository.update(senderWallet);

        receiverWallet.setBalanceUsd(receiverWallet.getBalanceUsd().add(amountUsd));
        walletRepository.update(receiverWallet);

        final var remittance = buildRemittance(sender, receiver, dto.amount(), amountUsd, exchangeRate);
        remittanceRepository.save(remittance);
    }


    private BigDecimal getAmountUsd(BigDecimal amountBrl, BigDecimal exchangeRate) {
        return amountBrl.divide(exchangeRate, 2, RoundingMode.HALF_UP);
    }


    private void validateBalance(Wallet senderWallet, BigDecimal amountBrl) {
        if (senderWallet.getBalanceBrl().compareTo(amountBrl) < 0) {
            throw new RemittanceValidationException("Insufficient funds");
        }
    }


    private void validateDailyLimit(User sender, BigDecimal amountBrl) {
        var startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        var endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        var totalAmountToday = remittanceRepository.findTotalAmountBySenderAndCreatedAtBetween(
                sender.getId(), startOfDay, endOfDay
        );

        if (totalAmountToday.add(amountBrl).compareTo(sender.getDailyLimit()) > 0) {
            throw new RemittanceValidationException("Exceeds daily limit");
        }
    }


    private Remittance buildRemittance(User sender, User receiver, BigDecimal amountBrl, BigDecimal amountUsd, BigDecimal exchangeRate) {
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


    private BigDecimal getExchangeRate(BigDecimal amountBrl) {
        final var quotationResponse = quotationService.getTodayQuotation();
        final var quotation = quotationResponse.quotations().getFirst();
        return quotation.purchaseRate();
    }


    private User getUser(String senderId) {
        return userRepository.findById(UUID.fromString(senderId))
                .orElseThrow(() -> new UserNotFoundException("Sender not found"));
    }

    private Wallet getWallet(String senderId) {
        return walletRepository.findByUserId(UUID.fromString(senderId))
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found"));
    }
} 