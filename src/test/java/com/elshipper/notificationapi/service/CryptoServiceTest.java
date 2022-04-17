package com.elshipper.notificationapi.service;

import com.elshipper.notificationapi.domain.Asset;
import com.elshipper.notificationapi.domain.rest.BinanceTickerResponse;
import com.elshipper.notificationapi.domain.rest.DebankBalanceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.when;


@SpringBootTest
class CryptoServiceTest {
    private final BinanceTickerResponse BTC_USDT = new BinanceTickerResponse(Asset.BTC.getPair(), "40102.44");
    private final BinanceTickerResponse BNB_USDT = new BinanceTickerResponse(Asset.BNB.getPair(), "389.22");
    private final BinanceTickerResponse ETH_USDT = new BinanceTickerResponse(Asset.ETH.getPair(), "2900.24");
    private final DebankBalanceResponse BALANCE = new DebankBalanceResponse();

    private final String binanceEndpoint = "https://api1.binance.com/api/v3/ticker/price?symbol=";
    private final String debankEndpoint =  "https://openapi.debank.com/v1/user/total_balance?id=";
    @MockBean
    private RestTemplate restTemplate;
    @Autowired
    private CryptoService cryptoService;


    @BeforeEach
    void setUp() {
        when(restTemplate.getForEntity(binanceEndpoint + Asset.BTC.getPair(), BinanceTickerResponse.class)).thenReturn(ResponseEntity.ok(BTC_USDT));
        when(restTemplate.getForEntity(binanceEndpoint + Asset.BNB.getPair(), BinanceTickerResponse.class)).thenReturn(ResponseEntity.ok(BNB_USDT));
        when(restTemplate.getForEntity(binanceEndpoint + Asset.ETH.getPair(), BinanceTickerResponse.class)).thenReturn(ResponseEntity.ok(ETH_USDT));
        when(restTemplate.getForEntity(debankEndpoint + "0x344532524", DebankBalanceResponse.class)).thenReturn(ResponseEntity.ok(BALANCE));
    }

    @Test
    void getTickerPrice() {
        BinanceTickerResponse tickerResponse = cryptoService.getTickerPrice(Asset.BTC.getPair());

        assertEquals(BTC_USDT, tickerResponse);
    }

    @Test
    void getTickerPricesSync() {
        List<BinanceTickerResponse> responses = List.of(BTC_USDT, BNB_USDT, ETH_USDT);
        List<BinanceTickerResponse> tickerResponses = cryptoService.getTickerPricesSync(List.of(Asset.BTC.getPair(),
                Asset.BNB.getPair(), Asset.ETH.getPair()));

        assertIterableEquals(responses, tickerResponses);
    }

    @Test
    void getTickerPricesAsync() {

    }

    @Test
    void getAccountBalances() {

    }
}