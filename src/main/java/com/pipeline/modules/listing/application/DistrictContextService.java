package com.pipeline.modules.listing.application;

import org.springframework.stereotype.Service;

import com.pipeline.modules.listing.domain.District;
import com.pipeline.modules.listing.domain.DistrictContext;
import com.pipeline.modules.listing.infrastructure.DistrictRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class DistrictContextService {

    private final DistrictRepository districtRepository;

    public DistrictContextService(DistrictRepository districtRepository) {
        this.districtRepository = districtRepository;
    }

    public Optional<DistrictContext> getContext(String districtId) {
        return districtRepository.findById(districtId)
            .map(this::toContext);
    }

    // Считаем отклонение цены от медианы района
    public BigDecimal calculatePriceDeviation(BigDecimal price,
                                               BigDecimal areaSqm,
                                               BigDecimal medianPriceSqm) {
        if (medianPriceSqm == null || medianPriceSqm.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal actualPriceSqm = price.divide(areaSqm, 2, RoundingMode.HALF_UP);
        BigDecimal deviation = actualPriceSqm
            .subtract(medianPriceSqm)
            .divide(medianPriceSqm, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
        return deviation.setScale(2, RoundingMode.HALF_UP);
    }

    // Считаем конкурентную плотность
    public int countCompetitors(String districtId, BigDecimal areaSqm) {
        return districtRepository.findById(districtId)
            .map(District::getActiveListingsCount)
            .orElse(0);
    }

    private DistrictContext toContext(District district) {
        return new DistrictContext(
            district.getId(),
            district.getName(),
            district.getMedianPriceSqm(),
            district.getDemandIndex(),
            district.getActiveListingsCount(),
            district.getSeasonalIndex()
        );
    }
}
