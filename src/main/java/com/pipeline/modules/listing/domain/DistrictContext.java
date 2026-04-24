package com.pipeline.modules.listing.domain;

import java.math.BigDecimal;

public record DistrictContext(
    String districtId,
    String districtName,
    BigDecimal medianPriceSqm,
    BigDecimal demandIndex,
    Integer activeListingsCount,
    BigDecimal seasonalIndex
) {}
