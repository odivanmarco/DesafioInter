package fx.wallet.infra.output.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import fx.wallet.core.domain.dto.BcbQuotationResponse;
import fx.wallet.core.exception.GetQuotationException;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
        String url = bcbApiUrl + "CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao='" + date + "'&$top=100&$format=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), BcbQuotationResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GetQuotationException("Error fetching quotation from BCB API", e);
        }
    }
}