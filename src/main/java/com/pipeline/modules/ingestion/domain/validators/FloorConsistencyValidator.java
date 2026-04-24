package com.pipeline.modules.ingestion.domain.validators;

import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.ingestion.domain.annotations.ValidFloorConsistency;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FloorConsistencyValidator
    implements ConstraintValidator<ValidFloorConsistency, ListingIngestRequest> {

    @Override
    public boolean isValid(ListingIngestRequest request,
                           ConstraintValidatorContext context) {
        if (request.floor() == null || request.totalFloors() == null) {
            return true; // null проверяет @NotNull на полях
        }
        return request.floor() <= request.totalFloors();
    }
}
