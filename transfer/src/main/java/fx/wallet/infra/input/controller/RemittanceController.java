package fx.wallet.infra.input.controller;

import fx.wallet.core.domain.dto.RemittanceRequestDTO;
import fx.wallet.core.usecase.RemittanceUseCase;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import jakarta.validation.Valid;

@Controller("/remittances")
public class RemittanceController {

    private final RemittanceUseCase remittanceUseCase;

    public RemittanceController(RemittanceUseCase remittanceUseCase) {
        this.remittanceUseCase = remittanceUseCase;
    }

    @Post
    public HttpResponse<Void> send(@Body @Valid RemittanceRequestDTO remittanceRequest) {
        remittanceUseCase.execute(remittanceRequest);
        return HttpResponse.ok();
    }
} 