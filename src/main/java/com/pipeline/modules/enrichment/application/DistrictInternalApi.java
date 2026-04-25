package com.pipeline.modules.enrichment.application;

import com.pipeline.modules.enrichment.domain.DistrictContext;
import java.util.Optional;

/**
 * Public contract for cross-module interactions with Enrichment module.
 */
public interface DistrictInternalApi {
    boolean districtExists(String districtId);
    Optional<DistrictContext> getContext(String districtId);
}
