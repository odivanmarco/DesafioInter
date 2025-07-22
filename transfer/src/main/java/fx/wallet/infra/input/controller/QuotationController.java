package fx.wallet.infra.input.controller;

import fx.wallet.core.domain.dto.BcbQuotationResponse;
import fx.wallet.core.service.QuotationService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;

@Controller("/quotation")
public class QuotationController {

    private final QuotationService quotationService;

    @Inject
    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    @Get
    public HttpResponse<BcbQuotationResponse> getQuotation() {
        return HttpResponse.ok(quotationService.getTodayQuotation());
    }

}
