package com.pipeline.modules.ingestion.domain.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.ingestion.domain.annotations.ValidPriceRange;

public class PriceRangeValidator
    implements ConstraintValidator<ValidPriceRange, ListingIngestRequest> {

    private static final BigDecimal MIN_PRICE = new BigDecimal("100000");
    private static final BigDecimal MAX_PRICE = new BigDecimal("2000000000");

    @Override
    public boolean isValid(ListingIngestRequest request,
                           ConstraintValidatorContext context) {
        if (request.price() == null) return true;
        return request.price().compareTo(MIN_PRICE) >= 0
            && request.price().compareTo(MAX_PRICE) <= 0;
    }
}
