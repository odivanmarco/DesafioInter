package fx.wallet.core.service.impl;

import fx.wallet.core.domain.dto.BcbQuotationResponse;
import fx.wallet.core.domain.dto.QuotationDTO;
import fx.wallet.infra.output.http.BcbClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotationServiceImplTest {

    @Mock
    private BcbClient bcbClient;

    @InjectMocks
    private QuotationServiceImpl quotationService;

    @Test
    @DisplayName("Should get today's quotation successfully")
    void shouldGetTodayQuotationSuccessfully() {
        QuotationDTO quotation = QuotationDTO.builder()
            .purchaseRate(new BigDecimal("5.30"))
            .sellingRate(new BigDecimal("5.40"))
            .dateTimeQuotation("2023-10-26 13:00:00.000")
            .build();
        BcbQuotationResponse expectedResponse = new BcbQuotationResponse(List.of(quotation));

        when(bcbClient.getQuotation(anyString())).thenReturn(expectedResponse);

        BcbQuotationResponse result = quotationService.getTodayQuotation();

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    @DisplayName("Should return null when quotation response is null")
    void shouldReturnNullWhenQuotationResponseIsNull() {
        when(bcbClient.getQuotation(anyString())).thenReturn(null);

        BcbQuotationResponse result = quotationService.getTodayQuotation();

        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when quotations list is null")
    void shouldReturnNullWhenQuotationsListIsNull() {
        BcbQuotationResponse expectedResponse = new BcbQuotationResponse(null);
        when(bcbClient.getQuotation(anyString())).thenReturn(expectedResponse);

        BcbQuotationResponse result = quotationService.getTodayQuotation();

        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when quotations list is empty")
    void shouldReturnNullWhenQuotationsListIsEmpty() {
        BcbQuotationResponse expectedResponse = new BcbQuotationResponse(Collections.emptyList());
        when(bcbClient.getQuotation(anyString())).thenReturn(expectedResponse);

        BcbQuotationResponse result = quotationService.getTodayQuotation();

        assertNull(result);
    }
}