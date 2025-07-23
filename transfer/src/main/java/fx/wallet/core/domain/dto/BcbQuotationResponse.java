package fx.wallet.core.domain.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record BcbQuotationResponse (
    @JsonProperty("value")
    List<QuotationDTO> quotations
) {} 