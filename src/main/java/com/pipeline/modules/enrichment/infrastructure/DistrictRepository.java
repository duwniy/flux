package com.pipeline.modules.enrichment.infrastructure;

import com.pipeline.modules.enrichment.domain.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictRepository extends JpaRepository<District, String> {
    java.util.List<District> findByCity(String city);
    java.util.List<District> findByCityOrderByDemandIndexDesc(String city);
}
