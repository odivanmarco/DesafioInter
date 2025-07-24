package fx.wallet.core.strategy.impl;

import fx.wallet.core.domain.dto.BcbQuotationResponse;
import fx.wallet.core.domain.dto.QuotationDTO;
import fx.wallet.core.domain.dto.RemittanceRequestDTO;
import fx.wallet.core.enums.TransferType;
import fx.wallet.core.exception.RemittanceValidationException;
import fx.wallet.core.exception.InvalidPasswordException;
import fx.wallet.core.service.QuotationService;
import fx.wallet.infra.repository.RemittanceRepository;
import fx.wallet.infra.repository.UserRepository;
import fx.wallet.infra.repository.WalletRepository;
import fx.wallet.infra.repository.entity.User;
import fx.wallet.infra.repository.entity.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsdToBrlRemittanceStrategyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private RemittanceRepository remittanceRepository;

    @Mock
    private QuotationService quotationService;

    @InjectMocks
    private UsdToBrlRemittanceStrategy usdToBrlRemittanceStrategy;

    private User sender;
    private User receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        sender = buildUser(new BigDecimal("6000.00"));
        receiver = buildUser(new BigDecimal("6000.00"));
        senderWallet = buildWallet(sender, new BigDecimal("1000.00"), new BigDecimal("1000.00"));
        receiverWallet = buildWallet(receiver, new BigDecimal("1000.00"), new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Should execute USD to BRL remittance successfully")
    void shouldExecuteUsdToBrlRemittanceSuccessfully() {
        RemittanceRequestDTO request = buildRemittanceRequest(sender.getId(), receiver.getId(), new BigDecimal("100.00"), "password");
        BcbQuotationResponse quotationResponse = buildQuotationResponse();

        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUserId(sender.getId())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserId(receiver.getId())).thenReturn(Optional.of(receiverWallet));
        when(quotationService.getTodayQuotation()).thenReturn(quotationResponse);
        when(remittanceRepository.findTotalAmountBySenderAndCreatedAtBetween(any(), any(), any())).thenReturn(new BigDecimal("0.00"));

        usdToBrlRemittanceStrategy.execute(request);

        assertEquals(new BigDecimal("900.00"), senderWallet.getBalanceUsd());
        assertEquals(new BigDecimal("1500.00"), receiverWallet.getBalanceBrl());
        verify(walletRepository).update(senderWallet);
        verify(walletRepository).update(receiverWallet);
        verify(remittanceRepository).save(any());
    }

    @Test
    @DisplayName("Should throw RemittanceValidationException for insufficient USD balance")
    void shouldThrowRemittanceValidationExceptionForInsufficientUsdBalance() {
        RemittanceRequestDTO request = buildRemittanceRequest(sender.getId(), receiver.getId(), new BigDecimal("1500.00"), "password");

        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUserId(sender.getId())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserId(receiver.getId())).thenReturn(Optional.of(receiverWallet));

        assertThrows(RemittanceValidationException.class, () -> usdToBrlRemittanceStrategy.execute(request));
        verify(walletRepository, never()).update(any());
        verify(remittanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw RemittanceValidationException for exceeding daily limit")
    void shouldThrowRemittanceValidationExceptionForExceedingDailyLimit() {
        sender.setDailyLimit(new BigDecimal("400.00"));
        RemittanceRequestDTO request = buildRemittanceRequest(sender.getId(), receiver.getId(), new BigDecimal("100.00"), "password");
        BcbQuotationResponse quotationResponse = buildQuotationResponse();


        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUserId(sender.getId())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserId(receiver.getId())).thenReturn(Optional.of(receiverWallet));
        when(quotationService.getTodayQuotation()).thenReturn(quotationResponse);
        when(remittanceRepository.findTotalAmountBySenderAndCreatedAtBetween(any(), any(), any())).thenReturn(new BigDecimal("0.00"));

        assertThrows(RemittanceValidationException.class, () -> usdToBrlRemittanceStrategy.execute(request));
        verify(walletRepository, never()).update(any());
        verify(remittanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException for incorrect password")
    void shouldThrowInvalidPasswordExceptionForIncorrectPassword() {
        RemittanceRequestDTO request = buildRemittanceRequest(sender.getId(), receiver.getId(), new BigDecimal("100.00"), "wrong_password");

        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));

        assertThrows(InvalidPasswordException.class, () -> usdToBrlRemittanceStrategy.execute(request));
    }

    private User buildUser(BigDecimal dailyLimit) {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email("test" + UUID.randomUUID() + "@email.com")
                .document("12345678900")
                .password("password")
                .userType("COMMON")
                .dailyLimit(dailyLimit)
                .build();
    }

    private Wallet buildWallet(User user, BigDecimal brlBalance, BigDecimal usdBalance) {
        return Wallet.builder()
                .id(UUID.randomUUID())
                .user(user)
                .balanceBrl(brlBalance)
                .balanceUsd(usdBalance)
                .build();
    }

    private RemittanceRequestDTO buildRemittanceRequest(UUID senderId, UUID receiverId, BigDecimal amount, String password) {
        return RemittanceRequestDTO.builder()
                .senderId(senderId.toString())
                .receiverId(receiverId.toString())
                .amount(amount)
                .password(password)
                .transferType(TransferType.USD_TO_BRL)
                .build();
    }

    private BcbQuotationResponse buildQuotationResponse() {
        return new BcbQuotationResponse(Collections.singletonList(
                new QuotationDTO(new BigDecimal("5.00"), new BigDecimal("5.00"), "2023-10-26 13:00:00.000"))
        );
    }
} 