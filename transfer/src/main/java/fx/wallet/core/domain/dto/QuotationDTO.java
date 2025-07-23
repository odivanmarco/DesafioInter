package fx.wallet.core.domain.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import io.micronaut.core.annotation.Introspected;

@Builder
@Introspected
public record QuotationDTO (
    @JsonProperty("cotacaoCompra")
    BigDecimal purchaseRate,

    @JsonProperty("cotacaoVenda")
    BigDecimal sellingRate,

    @JsonProperty("dataHoraCotacao")
    String dateTimeQuotation
) {}
