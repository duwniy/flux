package com.pipeline.modules.ingestion.domain.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.pipeline.modules.ingestion.domain.validators.FloorConsistencyValidator;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FloorConsistencyValidator.class)
public @interface ValidFloorConsistency {
    String message() default "Floor cannot exceed total floors";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
