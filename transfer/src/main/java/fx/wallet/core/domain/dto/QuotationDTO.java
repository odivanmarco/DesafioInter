package fx.wallet.core.domain.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

@Introspected
public record QuotationDTO (
    @JsonProperty("cotacaoCompra")
    BigDecimal purchaseRate,

    @JsonProperty("cotacaoVenda")
    BigDecimal sellingRate,

    @JsonProperty("dataHoraCotacao")
    String dateTimeQuotation
) {}
