package com.pipeline.modules.ingestion.domain.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.ingestion.domain.annotations.ValidAreaConsistency;

public class AreaConsistencyValidator
    implements ConstraintValidator<ValidAreaConsistency, ListingIngestRequest> {

    private static final BigDecimal MIN_PRICE_PER_SQM = new BigDecimal("5000");

    @Override
    public boolean isValid(ListingIngestRequest request,
                           ConstraintValidatorContext context) {
        if (request.price() == null || request.totalAreaSqm() == null) {
            return true;
        }
        BigDecimal pricePerSqm = request.price()
            .divide(request.totalAreaSqm(), 2, RoundingMode.HALF_UP);
        return pricePerSqm.compareTo(MIN_PRICE_PER_SQM) >= 0;
    }
}
