package com.briandidthat.econserver.util;

import com.briandidthat.econserver.domain.coinbase.Statistic;
import com.briandidthat.econserver.domain.coinbase.SpotPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsUtilitiesTest {

    @Test
    void testBuildStatistic() {
        SpotPrice historicalEth = new SpotPrice(TestingConstants.ETH, "USD", "4000", TestingConstants.START_DATE);
        SpotPrice currentEth = new SpotPrice(TestingConstants.ETH, "USD", "3000", TestingConstants.END_DATE);

        final Statistic expected = new Statistic(TestingConstants.ETH, historicalEth.getAmount(), currentEth.getAmount(),
                "-1000.00", "-25.00", historicalEth.getDate(), currentEth.getDate(), "24 months");

        Statistic statistic = StatisticsUtilities.buildStatistic(historicalEth, currentEth);
        assertEquals(expected, statistic);
    }
}