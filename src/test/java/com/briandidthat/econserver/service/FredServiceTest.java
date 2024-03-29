package com.briandidthat.econserver.service;

import com.briandidthat.econserver.domain.fred.FredResponse;
import com.briandidthat.econserver.domain.fred.Observation;
import com.briandidthat.econserver.util.RequestUtilities;
import com.briandidthat.econserver.util.TestingConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FredServiceTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private FredService service;

    @BeforeEach
    void setUp() {
        final String fredBaseUrl = "https://api.stlouisfed.org/fred";
        final LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("series_id", TestingConstants.AVERAGE_MORTGAGE_RATE);
        params.put("file_type", "json");
        params.put("sort_order", "desc");
        params.put("api_key", TestingConstants.TEST_API_KEY);

        final String queryString = RequestUtilities.formatQueryString(fredBaseUrl + "/series/observations", params);

        ReflectionTestUtils.setField(service, "fredBaseUrl", fredBaseUrl);

        when(restTemplate.getForObject(queryString, FredResponse.class)).thenReturn(TestingConstants.MORTGAGE_RATE_RESPONSE);
    }

    @Test
    void getObservationsForMortgage() {
        FredResponse response = service.getObservations(TestingConstants.TEST_API_KEY, TestingConstants.AVERAGE_MORTGAGE_RATE, new LinkedHashMap<>());
        assertEquals(TestingConstants.MORTGAGE_RATE_RESPONSE, response);
    }

    @Test
    void getMostRecentObservation() {
        Observation response = service.getMostRecentObservation(TestingConstants.TEST_API_KEY, TestingConstants.AVERAGE_MORTGAGE_RATE, new LinkedHashMap<>());
        assertEquals(TestingConstants.CURRENT_MORTGAGE_RATE, response);
    }
}