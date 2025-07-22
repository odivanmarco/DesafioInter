package fx.wallet.core.service.impl;

import fx.wallet.core.domain.dto.UserRequestDTO;
import fx.wallet.core.domain.dto.UserResponseDTO;
import fx.wallet.core.exception.UserAlreadyExistsException;
import fx.wallet.core.exception.UserCreationException;
import fx.wallet.core.exception.UserNotFoundException;
import fx.wallet.core.exception.WalletNotFoundException;
import fx.wallet.core.mapper.UserMapper;
import fx.wallet.core.service.UserService;
import fx.wallet.infra.repository.UserRepository;
import fx.wallet.infra.repository.WalletRepository;
import fx.wallet.infra.repository.entity.User;
import fx.wallet.infra.repository.entity.Wallet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.util.UUID;

@Singleton
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final UserMapper userMapper;

    @Inject
    public UserServiceImpl(UserRepository userRepository, WalletRepository walletRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO userDTO) {
        try {
            if (userRepository.existsByDocument(userDTO.document())) {
                throw new UserAlreadyExistsException("User with this document already exists");
            }
            if (userRepository.existsByEmail(userDTO.email())) {
                throw new UserAlreadyExistsException("User with this email already exists");
            }
    
            var user = userMapper.toEntity(userDTO);
            var savedUser = userRepository.save(user);
    
            var wallet = buildWallet(savedUser);
            walletRepository.save(wallet);

            return userMapper.toResponseDTO(savedUser);
        } catch (Exception e) {
            throw new UserCreationException("Error creating user", e);
        }
    }

    @Override
    public UserResponseDTO getUser(String id) {
        var uuid = UUID.fromString(id);
        var user = userRepository.findById(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        var wallet = walletRepository.findByUserId(uuid)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user id: " + id));

        var userResponse = userMapper.toResponseDTO(user);
        return userResponse.toBuilder()
                                .balanceBrl(wallet.getBalanceBrl())
                                .balanceUsd(wallet.getBalanceUsd())
                            .build();
    }

    private Wallet buildWallet(User user) {
        return Wallet.builder()
            .id(UUID.randomUUID())
            .user(user)
            .balanceBrl(BigDecimal.ZERO)
            .balanceUsd(BigDecimal.ZERO)
            .build();
    }
} 