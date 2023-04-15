package com.briandidthat.priceserver.util;

import com.briandidthat.priceserver.domain.Statistic;
import com.briandidthat.priceserver.domain.SpotPrice;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class StatisticsUtilities {
    private StatisticsUtilities() {}

    private static String formateDateString(LocalDate startDate, LocalDate endDate) {
        final long timeDelta = ChronoUnit.DAYS.between(startDate, endDate);
        if (timeDelta < 365) {
            return timeDelta + " days";
        } else {
            return ChronoUnit.MONTHS.between(startDate, endDate) + " months";
        }
    }

    public static Statistic buildStatistic(SpotPrice startPrice, SpotPrice endPrice) {
        final double startPriceDouble = Double.parseDouble(startPrice.getAmount());
        final double endPriceDouble = Double.parseDouble(endPrice.getAmount());

        final double priceChange = endPriceDouble - startPriceDouble;
        final double percentChange = ((endPriceDouble - startPriceDouble) / startPriceDouble) * 100;

        final String priceChangeString = String.format("%.2f", priceChange);
        final String percentChangeString = String.format("%.2f", percentChange);
        final String timeDeltaString = formateDateString(startPrice.getDate(), endPrice.getDate());

        return new Statistic(startPrice.getBase(), priceChangeString, percentChangeString, timeDeltaString);
    }
}
