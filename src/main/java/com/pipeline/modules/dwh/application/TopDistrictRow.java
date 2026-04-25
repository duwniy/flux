package com.pipeline.modules.dwh.application;

public record TopDistrictRow(
    String districtId,
    String districtName,
    double avgScore,
    long listingCount
) {}
