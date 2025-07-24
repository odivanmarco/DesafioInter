package fx.wallet.core.service.impl;

import fx.wallet.core.domain.dto.DepositRequestDTO;
import fx.wallet.core.domain.dto.DepositResponseDTO;
import fx.wallet.core.exception.DepositAmountException;
import fx.wallet.core.exception.InvalidPasswordException;
import fx.wallet.core.exception.UserNotFoundException;
import fx.wallet.core.exception.WalletNotFoundException;
import fx.wallet.core.mapper.WalletMapper;
import fx.wallet.infra.repository.UserRepository;
import fx.wallet.infra.repository.WalletRepository;
import fx.wallet.infra.repository.entity.User;
import fx.wallet.infra.repository.entity.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static fx.wallet.ApplicationConstants.BRL;
import static fx.wallet.ApplicationConstants.USD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositMoneyServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletMapper walletMapper;

    @InjectMocks
    private DepositMoneyServiceImpl depositMoneyService;

    @Test
    @DisplayName("Should deposit BRL successfully")
    void shouldDepositBrlSuccessfully() {
        User user = buildUser();
        Wallet wallet = buildWallet(user);
        DepositRequestDTO depositRequestDTO = buildDepositRequestDTO(user.getId(), BRL, new BigDecimal("100.00"), "password");
        DepositResponseDTO depositResponseDTO = buildDepositResponseDTO(wallet, user.getId().toString());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(user.getId())).thenReturn(Optional.of(wallet));
        when(walletMapper.toResponseDTO(any(Wallet.class), anyString())).thenReturn(depositResponseDTO);

        DepositResponseDTO result = depositMoneyService.depositMoney(depositRequestDTO);

        assertNotNull(result);
        assertEquals(depositResponseDTO, result);
        verify(walletRepository).update(any(Wallet.class));
        assertEquals(new BigDecimal("110.00"), wallet.getBalanceBrl());
    }

    @Test
    @DisplayName("Should deposit USD successfully")
    void shouldDepositUsdSuccessfully() {
        User user = buildUser();
        Wallet wallet = buildWallet(user);
        DepositRequestDTO depositRequestDTO = buildDepositRequestDTO(user.getId(), USD, new BigDecimal("50.00"), "password");
        DepositResponseDTO depositResponseDTO = buildDepositResponseDTO(wallet, user.getId().toString());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(user.getId())).thenReturn(Optional.of(wallet));
        when(walletMapper.toResponseDTO(any(Wallet.class), anyString())).thenReturn(depositResponseDTO);

        DepositResponseDTO result = depositMoneyService.depositMoney(depositRequestDTO);

        assertNotNull(result);
        assertEquals(depositResponseDTO, result);
        verify(walletRepository).update(any(Wallet.class));
        assertEquals(new BigDecimal("60.00"), wallet.getBalanceUsd());
    }


    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowUserNotFoundException() {
        UUID userId = UUID.randomUUID();
        DepositRequestDTO depositRequestDTO = buildDepositRequestDTO(userId, BRL, new BigDecimal("100.00"), "password");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> depositMoneyService.depositMoney(depositRequestDTO));
        verify(walletRepository, never()).findByUserId(any());
        verify(walletRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when wallet does not exist")
    void shouldThrowWalletNotFoundException() {
        User user = buildUser();
        DepositRequestDTO depositRequestDTO = buildDepositRequestDTO(user.getId(), BRL, new BigDecimal("100.00"), "password");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> depositMoneyService.depositMoney(depositRequestDTO));
        verify(walletRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw DepositAmountException for zero amount")
    void shouldThrowDepositAmountExceptionForZeroAmount() {
        User user = buildUser();
        Wallet wallet = buildWallet(user);
        DepositRequestDTO depositRequestDTO = buildDepositRequestDTO(user.getId(), BRL, BigDecimal.ZERO, "password");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(user.getId())).thenReturn(Optional.of(wallet));

        assertThrows(DepositAmountException.class, () -> depositMoneyService.depositMoney(depositRequestDTO));
        verify(walletRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw DepositAmountException for negative amount")
    void shouldThrowDepositAmountExceptionForNegativeAmount() {
        User user = buildUser();
        Wallet wallet = buildWallet(user);
        DepositRequestDTO depositRequestDTO = buildDepositRequestDTO(user.getId(), BRL, new BigDecimal("-10.00"), "password");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(user.getId())).thenReturn(Optional.of(wallet));

        assertThrows(DepositAmountException.class, () -> depositMoneyService.depositMoney(depositRequestDTO));
        verify(walletRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException for incorrect password")
    void shouldThrowInvalidPasswordExceptionForIncorrectPassword() {
        User user = buildUser();
        DepositRequestDTO depositRequestDTO = buildDepositRequestDTO(user.getId(), BRL, new BigDecimal("100.00"), "wrong_password");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(InvalidPasswordException.class, () -> depositMoneyService.depositMoney(depositRequestDTO));
        verify(walletRepository, never()).findByUserId(any());
        verify(walletRepository, never()).update(any());
    }

    private User buildUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email("test@email.com")
                .document("12345678900")
                .password("password")
                .userType("COMMON")
                .dailyLimit(new BigDecimal("1000.00"))
                .build();
    }

    private Wallet buildWallet(User user) {
        return Wallet.builder()
                .id(UUID.randomUUID())
                .user(user)
                .balanceBrl(new BigDecimal("10.00"))
                .balanceUsd(new BigDecimal("10.00"))
                .build();
    }

    private DepositRequestDTO buildDepositRequestDTO(UUID userId, String currency, BigDecimal amount, String password) {
        return DepositRequestDTO.builder()
            .userId(userId.toString())
            .currency(currency)
            .amount(amount)
            .password(password)
            .build();
    }

    private DepositResponseDTO buildDepositResponseDTO(Wallet wallet, String userId) {
        return DepositResponseDTO.builder()
            .id(userId)
            .balanceBrl(wallet.getBalanceBrl())
            .balanceUsd(wallet.getBalanceUsd())
            .build();
    }
} 