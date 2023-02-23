package com.toogroovy.priceserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toogroovy.priceserver.domain.SpotPrice;
import com.toogroovy.priceserver.domain.Statistic;
import com.toogroovy.priceserver.domain.Token;
import com.toogroovy.priceserver.domain.exception.BackendClientException;
import com.toogroovy.priceserver.util.RequestUtilities;
import com.toogroovy.priceserver.util.StatisticsUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CryptoService {
    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private volatile List<Token> availableTokens;

    @Value("${apis.coinbase.baseUrl}")
    private String coinbaseUrl;
    @Autowired
    private RestTemplate restTemplate;

    public List<Token> getAvailableTokens() {
        try {
            final ResponseEntity<String> response = restTemplate.getForEntity(coinbaseUrl + "/currencies/crypto", String.class);
            final Map<String, Token[]> result = mapper.readValue(response.getBody(), new TypeReference<>() {
            });
            final Token[] tokens = result.get("data");
            return Arrays.asList(tokens);
        } catch (Exception e) {
            logger.error("Unable to retrieve token list");
            return null;
        }
    }

    public SpotPrice getSpotPrice(String symbol) throws HttpClientErrorException, BackendClientException {
        symbol = symbol.toUpperCase();

        if (!RequestUtilities.validateSymbol(symbol, availableTokens))
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid symbol: " + symbol);

        try {
            logger.info("Fetching current price for {}", symbol);
            final ResponseEntity<String> response = restTemplate.getForEntity(coinbaseUrl + "/prices/{symbol}-USD/spot",
                    String.class, Map.of("symbol", symbol));
            final Map<String, SpotPrice> result = mapper.readValue(response.getBody(), new TypeReference<>() {});
            final SpotPrice spotPrice = result.get("data");
            logger.info("Fetched {} spot price. Response: {}", symbol, spotPrice.amount());
            return spotPrice;
        } catch (RestClientException e) {
            logger.error("Unable to fetch {} spot price. Reason: {}", symbol, e.getMessage());
            throw new BackendClientException(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public SpotPrice getHistoricalSpotPrice(String symbol, LocalDate date) {
        symbol = symbol.toUpperCase();

        if (!RequestUtilities.validateSymbol(symbol, availableTokens))
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid symbol: " + symbol);

        try {
            logger.info("Fetching historical price for {} on date {}", symbol, date);
            final ResponseEntity<String> response = restTemplate.getForEntity(coinbaseUrl + "/prices/{symbol}-USD/spot?date={date}",
                    String.class, Map.of("symbol", symbol, "date", date.toString()));
            final Map<String, SpotPrice> result = mapper.readValue(response.getBody(), new TypeReference<>() {});
            final SpotPrice spotPrice = result.get("data");
            logger.info("Fetched historical spot price for {}. Response: {}", symbol, spotPrice.amount());
            return spotPrice;
        } catch (RestClientException e) {
            logger.error("Unable to fetch historical price for {}", symbol);
            throw new BackendClientException(e.getMessage());
        } catch (JsonProcessingException e) {
            logger.error("Unable to map json response");
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Statistic getPriceStatistics(String symbol, LocalDate startDate) {
        final SpotPrice currentPrice = getSpotPrice(symbol);
        final SpotPrice historicalPrice = getHistoricalSpotPrice(symbol, startDate);

        return StatisticsUtilities.buildStatistic(startDate, historicalPrice, currentPrice);
    }

    @Async
    private CompletableFuture<SpotPrice> getSpotPriceAsync(String symbol) {
        return CompletableFuture.supplyAsync(() -> getSpotPrice(symbol));
    }

    public List<SpotPrice> getSpotPrices(List<String> symbols) {
        final List<SpotPrice> responses;
        final List<CompletableFuture<SpotPrice>> requests;

        logger.info("Fetching prices asynchronously {}", symbols);

        final Instant start = Instant.now();
        // store list of symbols requests to be run in parallel
        requests = symbols.stream().map(this::getSpotPriceAsync).collect(Collectors.toList());
        // wait for all requests to be completed
        CompletableFuture.allOf(requests.toArray(new CompletableFuture[0])).join();
        // calculate the time it took for our request to be completed
        final Instant end = Instant.now();
        logger.info("Completed async spot price request in {}ms", end.minusMillis(start.toEpochMilli()).toEpochMilli());

        responses = requests.stream().map(c -> {
            SpotPrice response = null;
            try {
                response = c.get();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return response;
        }).collect(Collectors.toList());

        return responses;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @EventListener(ApplicationReadyEvent.class)
    protected void updateAvailableTokens() {
        availableTokens = getAvailableTokens();
        if (availableTokens == null) {
            logger.error("Error retrieving available tokens. Retrying");

            boolean retry = true;
            int retryCount = 0;

            // continue to retry until we update the available tokens or fail 5 times in which case we will wait till
            // next request or next day
            while (retry) {
                List<Token> tokens = getAvailableTokens();
                if (tokens != null) {
                    availableTokens = tokens;
                    retry = false;
                } else {
                    if (retryCount == 5) {
                        logger.info("Reached max retries {}.", retryCount);
                        return;
                    }

                    retryCount++;
                    tokens = getAvailableTokens();
                    if (tokens != null) {
                        retry = false;
                    }
                }
            }
        }

        logger.info("Updated available tokens list");
    }
}
