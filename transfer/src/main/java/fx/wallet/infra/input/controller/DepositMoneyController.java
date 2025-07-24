package fx.wallet.infra.input.controller;

import fx.wallet.core.domain.dto.DepositRequestDTO;
import fx.wallet.core.domain.dto.DepositResponseDTO;
import fx.wallet.core.service.DepositMoneyService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

@Controller("/deposit")
public class DepositMoneyController {
    private final DepositMoneyService depositMoneyService;

    @Inject
    public DepositMoneyController(DepositMoneyService depositMoneyService) {
        this.depositMoneyService = depositMoneyService;
    }

    @Post
    public HttpResponse<DepositResponseDTO> deposit(@Body DepositRequestDTO depositRequestDTO) {
        var depositResponseDTO = depositMoneyService.depositMoney(depositRequestDTO);
        return HttpResponse.ok(depositResponseDTO);
    }
} 