package fx.wallet.infra.input.controller;

import fx.wallet.core.domain.dto.UserRequestDTO;
import fx.wallet.core.domain.dto.UserResponseDTO;
import fx.wallet.core.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import static io.micronaut.http.HttpResponse.created;


@Controller("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Post
    public HttpResponse<UserResponseDTO> createUser(@Body UserRequestDTO userDTO) {
        return created(userService.createUser(userDTO));
    }

} 