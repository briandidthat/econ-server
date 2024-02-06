package com.briandidthat.econserver.service;

import com.briandidthat.econserver.domain.coinbase.SpotPrice;
import com.briandidthat.econserver.domain.coinbase.Statistic;
import com.briandidthat.econserver.util.TestingConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CoinbaseServiceTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private CoinbaseService service;
    private final ObjectMapper mapper = new ObjectMapper();


    @BeforeEach
    void setUp() throws Exception {
        final String coinbaseEndpoint = "https://api.coinbase.com/v2";

        final Map<String, SpotPrice> BTC_RESPONSE = Map.of("data", TestingConstants.BTC_SPOT);
        final Map<String, SpotPrice> BNB_RESPONSE = Map.of("data", TestingConstants.BNB_SPOT);
        final Map<String, SpotPrice> ETH_RESPONSE = Map.of("data", TestingConstants.ETH_SPOT);
        final Map<String, SpotPrice> HISTORICAL_BTC_RESPONSE = Map.of("data", TestingConstants.HISTORICAL_BTC);
        final Map<String, SpotPrice> HISTORICAL_BNB_RESPONSE = Map.of("data", TestingConstants.HISTORICAL_BNB);
        final Map<String, SpotPrice> HISTORICAL_ETH_RESPONSE = Map.of("data", TestingConstants.HISTORICAL_ETH);

        final String btcJson = mapper.writeValueAsString(BTC_RESPONSE);
        final String bnbJson = mapper.writeValueAsString(BNB_RESPONSE);
        final String ethJson = mapper.writeValueAsString(ETH_RESPONSE);

        final String historicalBtcJson = mapper.writeValueAsString(HISTORICAL_BTC_RESPONSE);
        final String historicalBnbJson = mapper.writeValueAsString(HISTORICAL_BNB_RESPONSE);
        final String historicalEthJson = mapper.writeValueAsString(HISTORICAL_ETH_RESPONSE);

        when(restTemplate.getForEntity(coinbaseEndpoint + "/prices/" + TestingConstants.BTC + "-USD/spot", String.class)).thenReturn(ResponseEntity.ok(btcJson));
        when(restTemplate.getForEntity(coinbaseEndpoint + "/prices/" + TestingConstants.BNB + "-USD/spot", String.class)).thenReturn(ResponseEntity.ok(bnbJson));
        when(restTemplate.getForEntity(coinbaseEndpoint + "/prices/" + TestingConstants.ETH + "-USD/spot", String.class)).thenReturn(ResponseEntity.ok(ethJson));
        when(restTemplate.getForEntity(coinbaseEndpoint + "/prices/" + TestingConstants.BTC + "-USD/spot?date=" + TestingConstants.START_DATE, String.class)).thenReturn(ResponseEntity.ok(historicalBtcJson));
        when(restTemplate.getForEntity(coinbaseEndpoint + "/prices/" + TestingConstants.BNB + "-USD/spot?date=" + TestingConstants.START_DATE, String.class)).thenReturn(ResponseEntity.ok(historicalBnbJson));
        when(restTemplate.getForEntity(coinbaseEndpoint + "/prices/" + TestingConstants.ETH + "-USD/spot?date=" + TestingConstants.START_DATE, String.class)).thenReturn(ResponseEntity.ok(historicalEthJson));
        when(restTemplate.getForEntity(coinbaseEndpoint + "/prices/" + TestingConstants.ETH + "-USD/spot?date=" + TestingConstants.END_DATE, String.class)).thenReturn(ResponseEntity.ok(ethJson));

        ReflectionTestUtils.setField(service, "coinbaseUrl", coinbaseEndpoint);
        ReflectionTestUtils.setField(service, "availableTokens", TestingConstants.AVAILABLE_TOKENS);
    }

    @Test
    void testGetSpotPrice() {
        SpotPrice tickerResponse = service.getSpotPrice(TestingConstants.BTC);
        assertEquals(TestingConstants.BTC_SPOT, tickerResponse);
    }

    @Test
    void testGetMultipleSpotPrices() {
        List<SpotPrice> responses = service.getSpotPrices(TestingConstants.TOKENS);
        assertIterableEquals(List.of(TestingConstants.BTC_SPOT, TestingConstants.BNB_SPOT, TestingConstants.ETH_SPOT), responses);
    }

    @Test
    void testGetHistoricalSpotPrice() {
        SpotPrice response = service.getHistoricalSpotPrice(TestingConstants.ETH, TestingConstants.START_DATE);
        assertEquals(TestingConstants.HISTORICAL_ETH, response);
    }

    @Test
    void testGetHistoricalSpotPrices() {
        List<SpotPrice> response = service.getHistoricalSpotPrices(TestingConstants.HISTORICAL_BATCH);
        assertIterableEquals(List.of(TestingConstants.HISTORICAL_BTC, TestingConstants.HISTORICAL_BNB, TestingConstants.HISTORICAL_ETH), response);
    }

    @Test
    void testGetPriceStatistics() {
        Statistic statistic = service.getPriceStatistics(TestingConstants.ETH, TestingConstants.START_DATE, TestingConstants.END_DATE);
        assertEquals("-1100.00", statistic.priceChange());
        assertEquals("-27.50", statistic.percentChange());
        assertEquals("24 months", statistic.timeFrame());
    }

}