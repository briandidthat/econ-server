package com.briandidthat.econserver.controller;

import com.briandidthat.econserver.domain.exception.ResourceNotFoundException;
import com.briandidthat.econserver.service.TwelveService;
import com.briandidthat.econserver.util.TestingConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
class StockControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private TwelveService service;

    @Test
    void getAssetPrice() throws Exception {
        final String outputJson = mapper.writeValueAsString(TestingConstants.APPLE_PRICE_RESPONSE);

        when(service.getAssetPrice(TestingConstants.TEST_API_KEY,"AAPL")).thenReturn(TestingConstants.APPLE_PRICE);

        this.mockMvc.perform(get("/stocks")
                .param("symbol", "AAPL")
                .header("apiKey", TestingConstants.TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().json(outputJson))
                .andDo(print());
    }

    @Test
    void getBatchStockPrice() throws Exception {
        final String outputJson = mapper.writeValueAsString(TestingConstants.BATCH_STOCK_RESPONSE);

        when(service.getMultipleAssetPrices(TestingConstants.TEST_API_KEY, List.of("AAPL", "GOOG"))).thenReturn(TestingConstants.BATCH_STOCK_RESPONSE);

        this.mockMvc.perform(get("/stocks/batch")
                .param("symbols", "AAPL", "GOOG")
                .header("apiKey", TestingConstants.TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().json(outputJson))
                .andDo(print());
    }
    // 400
    @Test
    void testGetStockPriceShouldHandleResourceNotFoundException() throws Exception {
        final String expectedOutput = "Invalid symbol: ALABAMA";

        when(service.getAssetPrice(TestingConstants.TEST_API_KEY,"ALABAMA")).thenThrow(new ResourceNotFoundException(expectedOutput));

        this.mockMvc.perform(get("/stocks")
                .param("symbol", "ALABAMA")
                .header("apiKey", TestingConstants.TEST_API_KEY))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedOutput)))
                .andDo(print());
    }

    // 422
    @Test
    void testGetBatchStockPriceShouldHandleConstraintViolationException() throws Exception {
        final String expectedOutput = "symbols: size must be between 1 and 5";

        this.mockMvc.perform(get("/stocks/batch")
                .param("symbols", "AAPL,GOOG,VOO,TSLA,NVDA,VIX")
                .header("apiKey", TestingConstants.TEST_API_KEY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString(expectedOutput)))
                .andDo(print());
    }
}