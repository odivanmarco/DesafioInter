package fx.wallet.infra.output.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import fx.wallet.core.domain.dto.BcbQuotationResponse;
import fx.wallet.core.exception.GetQuotationException;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Singleton
public class BcbClient {

    @Value("${bcb.client.url}")
    private String bcbApiUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    public BcbClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BcbQuotationResponse getQuotation(String date) {
        var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        String adjustedDateStr = adjustDateForWeekend(date);
        Optional<BcbQuotationResponse> quotationOpt = fetchQuotationFromBcb(adjustedDateStr);

        if (quotationOpt.isPresent() && quotationOpt.get().quotations() != null && !quotationOpt.get().quotations().isEmpty()) {
            return quotationOpt.get();
        }

        LocalDate initialAdjustedDate = LocalDate.parse(adjustedDateStr, formatter);
        LocalDate previousBusinessDay;

        if (initialAdjustedDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            previousBusinessDay = initialAdjustedDate.minusDays(3);
        } else {
            previousBusinessDay = initialAdjustedDate.minusDays(1);
        }

        String retryDateStr = previousBusinessDay.format(formatter);
        Optional<BcbQuotationResponse> retryQuotationOpt = fetchQuotationFromBcb(retryDateStr);

        return retryQuotationOpt.filter(q -> q.quotations() != null && !q.quotations().isEmpty())
                .orElseThrow(() -> new GetQuotationException("BCB API returned no quotation for the requested date or the previous business day."));
    }

    @Cacheable("quotations-cache")
    public Optional<BcbQuotationResponse> fetchQuotationFromBcb(String date) {
        try {
            var encodedDate = URLEncoder.encode("'" + date + "'", StandardCharsets.UTF_8.toString());
            var url = bcbApiUrl + "CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao=" + encodedDate + "&$top=100&$format=json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(objectMapper.readValue(response.body(), BcbQuotationResponse.class));
        } catch (IOException | InterruptedException e) {
            throw new GetQuotationException("Error fetching quotation from BCB API", e);
        }
    }


    private String adjustDateForWeekend(String date) {
        var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        var localDate = LocalDate.parse(date, formatter);

        if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            localDate = localDate.minusDays(1);
        } else if (localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            localDate = localDate.minusDays(2);
        }

        return localDate.format(formatter);
    }
}