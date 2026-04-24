package com.pipeline.modules.listing.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pipeline.modules.listing.domain.District;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, String> {

    @Query("""
        SELECT d FROM District d
        WHERE d.city = :city
        ORDER BY d.demandIndex DESC
    """)
    List<District> findByCityOrderByDemand(@Param("city") String city);
}
