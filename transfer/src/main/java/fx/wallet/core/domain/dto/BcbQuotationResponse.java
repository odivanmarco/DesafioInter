package fx.wallet.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record BcbQuotationResponse (
    @JsonProperty("value")
    List<QuotationDTO> quotations
) {} 