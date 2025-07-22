package fx.wallet.infra.input.controller;

import fx.wallet.core.domain.dto.RemittanceRequestDTO;
import fx.wallet.core.service.RemittanceService;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import jakarta.validation.Valid;

@Controller("/remittances")
public class RemittanceController {

    private final RemittanceService remittanceService;

    public RemittanceController(RemittanceService remittanceService) {
        this.remittanceService = remittanceService;
    }

    @Post
    public void send(@Body @Valid RemittanceRequestDTO remittanceRequest) {
        remittanceService.send(remittanceRequest);
    }
} 