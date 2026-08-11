package org.reciplease.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * TEMPORARY diagnostic endpoint — not a real feature. Tests whether a request originating from
 * Cloud Run (a distinct outbound IP range from a dev sandbox or a home network, both of which
 * get a flat 403 from Sainsbury's Akamai bot protection) fares any differently. No
 * {@code @PreAuthorize}, deliberately reachable without a token for a quick curl test — remove or
 * harden once the result is known, do not leave this as a permanent open proxy.
 */
@RestController
@RequestMapping("api/dev")
public class DevController {

    private static final String SEARCH_URL = "https://www.sainsburys.co.uk/groceries-api/gol-services/product/v1/product"
            + "?filter[keyword]={query}&page_number=1&page_size=5";

    private final RestClient restClient = RestClient.builder().build();

    @GetMapping("sainsburys-test")
    public SainsburysTestResult sainsburysTest(@RequestParam final String query) {
        try {
            final var body = restClient.get()
                    .uri(SEARCH_URL, query)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36")
                    .retrieve()
                    .toEntity(String.class);
            return new SainsburysTestResult(body.getStatusCode().value(), true, truncate(body.getBody()));
        } catch (final RestClientResponseException e) {
            return new SainsburysTestResult(e.getStatusCode().value(), false, truncate(e.getResponseBodyAsString()));
        } catch (final RestClientException e) {
            return new SainsburysTestResult(null, false, e.getMessage());
        }
    }

    private static String truncate(final String body) {
        return body == null ? null : body.substring(0, Math.min(body.length(), 2000));
    }

    public record SainsburysTestResult(Integer statusCode, boolean success, String bodyOrError) {}
}
