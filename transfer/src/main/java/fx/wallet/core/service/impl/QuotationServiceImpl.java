package fx.wallet.core.service.impl;

import fx.wallet.core.domain.dto.BcbQuotationResponse;
import fx.wallet.core.service.QuotationService;
import fx.wallet.infra.output.http.BcbClient;
import jakarta.inject.Singleton;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static java.util.Objects.nonNull;

@Singleton
public class QuotationServiceImpl implements QuotationService {

    private final BcbClient bcbClient;

    @Inject
    public QuotationServiceImpl(BcbClient bcbClient) {
        this.bcbClient = bcbClient;
    }

    @Override
    public BcbQuotationResponse getTodayQuotation() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String formattedDate = today.format(formatter);

        var quotationResponse = bcbClient.getQuotation(formattedDate);

        if (nonNull(quotationResponse)
                &&  nonNull(quotationResponse.quotations())
                && !quotationResponse.quotations().isEmpty()) {
            return quotationResponse;
        }

        return null;
    }
}