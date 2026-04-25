package com.pipeline.modules.enrichment.application;

import com.pipeline.modules.enrichment.domain.District;
import com.pipeline.modules.enrichment.infrastructure.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import com.pipeline.modules.enrichment.domain.DistrictContext;

@Service
@RequiredArgsConstructor
public class DistrictContextService implements DistrictInternalApi {

    private final DistrictRepository districtRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean districtExists(String districtId) {
        return districtRepository.existsById(districtId);
    }

    @Override
    public Optional<DistrictContext> getContext(String districtId) {
        return districtRepository.findById(districtId)
            .map(d -> new DistrictContext(
                d.getMedianPriceSqm(),
                d.getDemandIndex(),
                countActiveListings(districtId),
                d.getAvgDaysOnMarket(),
                d.getSeasonalIndex()
            ));
    }

    public Integer countCompetitors(String districtId, BigDecimal areaSqm) {
        if (areaSqm == null || areaSqm.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        // Простой поиск конкурентов: тот же район, похожая площадь (+- 20%) и статус ACTIVE
        BigDecimal minArea = areaSqm.multiply(new BigDecimal("0.8"));
        BigDecimal maxArea = areaSqm.multiply(new BigDecimal("1.2"));

        Integer result = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM listings WHERE district_id = ? AND status = 'ACTIVE' AND total_area_sqm BETWEEN ? AND ?",
            Integer.class,
            districtId, minArea, maxArea
        );
        return result != null ? result : 0;
    }

    private int countActiveListings(String districtId) {
        Integer result = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM listings WHERE district_id = ? AND status = 'ACTIVE'",
            Integer.class,
            districtId
        );
        return result != null ? result : 0;
    }

    public BigDecimal calculatePriceDeviation(BigDecimal price, BigDecimal areaSqm, BigDecimal medianPriceSqm) {
        if (price == null || areaSqm == null || medianPriceSqm == null || areaSqm.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal priceSqm = price.divide(areaSqm, 2, RoundingMode.HALF_UP);
        BigDecimal diff = priceSqm.subtract(medianPriceSqm);
        
        return diff.divide(medianPriceSqm, 4, RoundingMode.HALF_UP)
                   .multiply(new BigDecimal("100"))
                   .setScale(2, RoundingMode.HALF_UP);
    }
}
