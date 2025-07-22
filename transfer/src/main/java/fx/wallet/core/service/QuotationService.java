package fx.wallet.core.service;

import fx.wallet.core.domain.dto.BcbQuotationResponse;

public interface QuotationService {
    public BcbQuotationResponse getTodayQuotation();
}
