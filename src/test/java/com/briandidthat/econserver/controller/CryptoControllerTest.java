package com.briandidthat.econserver.controller;

import com.briandidthat.econserver.domain.BatchRequest;
import com.briandidthat.econserver.domain.BatchResponse;
import com.briandidthat.econserver.domain.exception.RetrievalException;
import com.briandidthat.econserver.domain.exception.BadRequestException;
import com.briandidthat.econserver.service.CoinbaseService;
import com.briandidthat.econserver.util.TestingConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CryptoController.class)
class CryptoControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CoinbaseService service;

    @Test
    void testGetSpotPrice() throws Exception {
        String outputJson = mapper.writeValueAsString(TestingConstants.BTC_PRICE);

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("symbol", "BTC");

        when(service.getAssetPrice(TestingConstants.BTC)).thenReturn(TestingConstants.BTC_PRICE);

        this.mockMvc.perform(get("/crypto/spot")
                .params(params))
                .andExpect(status().isOk())
                .andExpect(content().json(outputJson))
                .andDo(print());
    }

    @Test
    void testGetMultipleSpotPrices() throws Exception {
        String symbolsString = String.join(",", TestingConstants.TOKENS);
        BatchResponse expectedResponse = TestingConstants.BATCH_SPOT_RESPONSE;

        String outputJson = mapper.writeValueAsString(expectedResponse);
        when(service.getAssetPrices(TestingConstants.TOKENS)).thenReturn(expectedResponse);

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("symbols", symbolsString);

        this.mockMvc.perform(get("/crypto/spot/batch")
                .params(params))
                .andExpect(status().isOk())
                .andExpect(content().json(outputJson))
                .andDo(print());
    }

    @Test
    void testGetHistoricalSpotPrice() throws Exception {
        String outputJson = mapper.writeValueAsString(TestingConstants.HISTORICAL_ETH_PRICE);
        when(service.getHistoricalAssetPrice(TestingConstants.ETH, TestingConstants.START_DATE)).thenReturn(TestingConstants.HISTORICAL_ETH_PRICE);

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("symbol", TestingConstants.ETH);
        params.add("date", TestingConstants.START_DATE.toString());

        this.mockMvc.perform(get("/crypto/spot/historical")
                .params(params))
                .andExpect(status().isOk())
                .andExpect(content().json(outputJson))
                .andDo(print());
    }

    @Test
    void testGetMultipleHistoricalSpotPrices() throws Exception {
        String inputJson = mapper.writeValueAsString(TestingConstants.HISTORICAL_BATCH_REQUEST);
        String outputJson = mapper.writeValueAsString(TestingConstants.BATCH_HISTORICAL_SPOT_RESPONSE);

        when(service.getHistoricalAssetPrices(TestingConstants.HISTORICAL_BATCH_REQUEST)).thenReturn(TestingConstants.BATCH_HISTORICAL_SPOT_RESPONSE);

        this.mockMvc.perform(post("/crypto/spot/batch/historical")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inputJson))
                .andExpect(status().isOk())
                .andExpect(content().json(outputJson))
                .andDo(print());
    }

    @Test
    void testGetPriceStatistics() throws Exception {
        String outputJson = mapper.writeValueAsString(TestingConstants.ETH_STATISTICS);

        when(service.getAssetPriceStatistics(TestingConstants.ETH, TestingConstants.START_DATE, TestingConstants.END_DATE)).thenReturn(TestingConstants.ETH_STATISTICS);

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("symbol", TestingConstants.ETH);
        params.add("startDate", TestingConstants.START_DATE.toString());
        params.add("endDate", TestingConstants.END_DATE.toString());

        this.mockMvc.perform(get("/crypto/spot/statistics")
                .params(params))
                .andExpect(status().isOk())
                .andExpect(content().json(outputJson))
                .andDo(print());
    }

    // Testing Error Handling Code

    // 400
    @Test
    void testGetSpotPriceShouldHandleResourceNotFoundException() throws Exception {
        String expectedOutput = "Invalid symbol: ALABAMA";

        when(service.getAssetPrice("ALABAMA")).thenThrow(new BadRequestException(expectedOutput));
        // should throw 400 exception due to invalid symbol
        this.mockMvc.perform(get("/crypto/spot")
                .param("symbol", "ALABAMA"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedOutput)))
                .andDo(print());
    }

    // 422
    @Test
    void testBatchSpotPricesShouldHandleConstraintViolation() throws Exception {
        // should throw constraint violation due to exceeding max of 5 symbols per request
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("symbols", String.join(",", List.of(TestingConstants.BTC, TestingConstants.BNB, TestingConstants.ETH, "USDC", "CAKE", "APE")));

        this.mockMvc.perform(get("/crypto/spot/batch")
                .params(params))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("symbols: size must be between 2 and 5")))
                .andDo(print());
    }

    // 500
    @Test
    void testGetSpotPriceShouldHandleBackendClientException() throws Exception {
        String expectedOutput = "SocketTimeoutException: Cannot connect";

        when(service.getAssetPrice(TestingConstants.ETH)).thenThrow(new RetrievalException(expectedOutput));
        // should throw 500 exception due to backend issue
        this.mockMvc.perform(get("/crypto/spot")
                .param("symbol", TestingConstants.ETH))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString(expectedOutput)))
                .andDo(print());
    }

}