package com.pipeline.modules.dwh.application;

public record TopDistrictRow(
    String districtId,
    double avgScore,
    long listingCount
) {}
