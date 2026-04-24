package com.pipeline.modules.dwh.application;

import java.math.BigDecimal;

public record ConversionFunnelRow(
    String districtId,
    double avgScore,
    long totalCalls
) {}
