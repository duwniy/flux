package com.pipeline.modules.enrichment.domain;

import java.math.BigDecimal;

public record DistrictContext(
    BigDecimal medianPriceSqm,
    BigDecimal demandIndex,
    Integer activeListingsCount,
    BigDecimal avgDaysOnMarket,
    BigDecimal seasonalIndex
) {}
