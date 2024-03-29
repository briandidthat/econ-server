package com.briandidthat.econserver.service;

import com.briandidthat.econserver.domain.AssetPrice;
import com.briandidthat.econserver.domain.BatchResponse;
import com.briandidthat.econserver.domain.coinbase.SpotPriceResponse;
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

        final Map<String, SpotPriceResponse> BTC_RESPONSE = Map.of("data", TestingConstants.BTC_SPOT);
        final Map<String, SpotPriceResponse> BNB_RESPONSE = Map.of("data", TestingConstants.BNB_SPOT);
        final Map<String, SpotPriceResponse> ETH_RESPONSE = Map.of("data", TestingConstants.ETH_SPOT);
        final Map<String, SpotPriceResponse> HISTORICAL_BTC_RESPONSE = Map.of("data", TestingConstants.HISTORICAL_BTC);
        final Map<String, SpotPriceResponse> HISTORICAL_BNB_RESPONSE = Map.of("data", TestingConstants.HISTORICAL_BNB);
        final Map<String, SpotPriceResponse> HISTORICAL_ETH_RESPONSE = Map.of("data", TestingConstants.HISTORICAL_ETH);

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
        AssetPrice tickerResponse = service.getAssetPrice(TestingConstants.BTC);
        assertEquals(TestingConstants.BTC_PRICE, tickerResponse);
    }

    @Test
    void testGetMultipleSpotPrices() {
        BatchResponse response = service.getAssetPrices(TestingConstants.TOKENS);
        assertIterableEquals(List.of(TestingConstants.BTC_PRICE, TestingConstants.BNB_PRICE, TestingConstants.ETH_PRICE), response.assetPrices());
    }

    @Test
    void testGetHistoricalSpotPrice() {
        AssetPrice response = service.getHistoricalAssetPrice(TestingConstants.ETH, TestingConstants.START_DATE);
        assertEquals(TestingConstants.HISTORICAL_ETH_PRICE, response);
    }

    @Test
    void testGetHistoricalSpotPrices() {
        BatchResponse response = service.getHistoricalAssetPrices(TestingConstants.HISTORICAL_BATCH_REQUEST);
        assertIterableEquals(List.of(TestingConstants.HISTORICAL_BTC_PRICE, TestingConstants.HISTORICAL_BNB_PRICE, TestingConstants.HISTORICAL_ETH_PRICE), response.assetPrices());
    }

    @Test
    void testGetPriceStatistics() {
        Statistic statistic = service.getAssetPriceStatistics(TestingConstants.ETH, TestingConstants.START_DATE, TestingConstants.END_DATE);
        assertEquals("-1100.00", statistic.priceChange());
        assertEquals("-27.50", statistic.percentChange());
        assertEquals("2 Years", statistic.timeFrame());
    }

}