package com.pipeline.modules.dwh.application;

import java.math.BigDecimal;

public record ConversionFunnelRow(
    String districtId,
    String districtName,
    double avgScore,
    long totalCalls
) {}
