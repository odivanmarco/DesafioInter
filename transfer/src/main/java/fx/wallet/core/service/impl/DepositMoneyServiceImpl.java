package fx.wallet.core.service.impl;

import fx.wallet.core.domain.dto.DepositRequestDTO;
import fx.wallet.core.domain.dto.DepositResponseDTO;
import fx.wallet.core.exception.DepositAmountException;
import fx.wallet.core.exception.UserNotFoundException;
import fx.wallet.core.exception.WalletNotFoundException;
import fx.wallet.core.mapper.WalletMapper;
import fx.wallet.core.service.DepositMoneyService;
import fx.wallet.infra.repository.UserRepository;
import fx.wallet.infra.repository.WalletRepository;
import fx.wallet.infra.repository.entity.User;
import fx.wallet.infra.repository.entity.Wallet;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.util.UUID;

import static fx.wallet.ApplicationConstants.BRL;
import static fx.wallet.ApplicationConstants.USD;

@Singleton
public class DepositMoneyServiceImpl implements DepositMoneyService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Inject
    public DepositMoneyServiceImpl(UserRepository userRepository, WalletRepository walletRepository, WalletMapper walletMapper) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.walletMapper = walletMapper;
    }

    @Override
    @Transactional
    public DepositResponseDTO depositMoney(DepositRequestDTO dto) {
        User user = userRepository.findById(UUID.fromString(dto.userId())).orElseThrow(() -> new UserNotFoundException("User not found with id: " + dto.userId()));
        Wallet wallet = walletRepository.findByUserId(user.getId()).orElseThrow(() -> new WalletNotFoundException("Wallet not found for user id: " + dto.userId()));

        if (dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DepositAmountException("Amount must be greater than 0");
        }

        if (BRL.equalsIgnoreCase(dto.currency())) {
            wallet.setBalanceBrl(wallet.getBalanceBrl().add(dto.amount()));
        } else if (USD.equalsIgnoreCase(dto.currency())) {
            wallet.setBalanceUsd(wallet.getBalanceUsd().add(dto.amount()));
        }
        walletRepository.update(wallet);
        return walletMapper.toResponseDTO(wallet, user.getId().toString());
    }
} 