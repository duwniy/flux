package com.pipeline.modules.ingestion.domain.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.pipeline.modules.ingestion.domain.validators.PriceRangeValidator;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PriceRangeValidator.class)
public @interface ValidPriceRange {
    String message() default "Price is outside acceptable range";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
