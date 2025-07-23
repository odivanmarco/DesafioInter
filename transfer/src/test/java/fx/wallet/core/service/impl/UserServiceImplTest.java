package fx.wallet.core.service.impl;

import fx.wallet.core.domain.dto.UserRequestDTO;
import fx.wallet.core.domain.dto.UserResponseDTO;
import fx.wallet.core.exception.UserAlreadyExistsException;
import fx.wallet.core.exception.UserCreationException;
import fx.wallet.core.exception.UserNotFoundException;
import fx.wallet.core.exception.WalletNotFoundException;
import fx.wallet.core.mapper.UserMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        UserRequestDTO userRequestDTO = buildUserRequestDTO();
        User user = buildUser();
        UserResponseDTO userResponseDTO = buildUserResponseDTO(user);

        when(userRepository.existsByDocument(userRequestDTO.document())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(false);
        when(userMapper.toEntity(userRequestDTO)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.createUser(userRequestDTO);

        assertNotNull(result);
        assertEquals(userResponseDTO, result);

        verify(userRepository, times(1)).existsByDocument(userRequestDTO.document());
        verify(userRepository, times(1)).existsByEmail(userRequestDTO.email());
        verify(userMapper, times(1)).toEntity(userRequestDTO);
        verify(userRepository, times(1)).save(any(User.class));
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(userMapper, times(1)).toResponseDTO(user);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when document already exists")
    void shouldThrowExceptionWhenDocumentExists() {
        UserRequestDTO userRequestDTO = buildUserRequestDTO();
        when(userRepository.existsByDocument(userRequestDTO.document())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(userRequestDTO));

        verify(userRepository, times(1)).existsByDocument(userRequestDTO.document());
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        UserRequestDTO userRequestDTO = buildUserRequestDTO();
        when(userRepository.existsByDocument(userRequestDTO.document())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(userRequestDTO));

        verify(userRepository, times(1)).existsByDocument(userRequestDTO.document());
        verify(userRepository, times(1)).existsByEmail(userRequestDTO.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserCreationException on generic error")
    void shouldThrowUserCreationExceptionOnGenericError() {
        UserRequestDTO userRequestDTO = buildUserRequestDTO();
        User user = buildUser();
        when(userRepository.existsByDocument(userRequestDTO.document())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(false);
        when(userMapper.toEntity(userRequestDTO)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(UserCreationException.class, () -> userService.createUser(userRequestDTO));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user successfully")
    void shouldGetUserSuccessfully() {
        User user = buildUser();
        UserResponseDTO userResponseDTO = buildUserResponseDTO(user);
        UUID userId = user.getId();
        Wallet wallet = Wallet.builder()
            .id(UUID.randomUUID())
            .user(user)
            .balanceBrl(BigDecimal.TEN)
            .balanceUsd(BigDecimal.ONE)
            .build();

        UserResponseDTO responseWithBalance = userResponseDTO.toBuilder()
            .balanceBrl(wallet.getBalanceBrl())
            .balanceUsd(wallet.getBalanceUsd())
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getUser(userId.toString());

        assertNotNull(result);
        assertEquals(responseWithBalance, result);
        assertEquals(BigDecimal.TEN, result.balanceBrl());
        assertEquals(BigDecimal.ONE, result.balanceUsd());

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, times(1)).findByUserId(userId);
        verify(userMapper, times(1)).toResponseDTO(user);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowUserNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(userId.toString()));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when wallet does not exist")
    void shouldThrowWalletNotFoundException() {
        User user = buildUser();
        UUID userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> userService.getUser(userId.toString()));

        verify(userRepository, times(1)).findById(userId);
        verify(walletRepository, times(1)).findByUserId(userId);
    }
    
    private UserRequestDTO buildUserRequestDTO() {
        return UserRequestDTO.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@email.com")
                .document("12345678900")
                .password("password")
                .userType("COMMON")
                .build();
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

    private UserResponseDTO buildUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .document(user.getDocument())
                .userType(user.getUserType())
                .dailyLimit(user.getDailyLimit())
                .build();
    }
} 