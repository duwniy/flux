package com.pipeline.modules.ingestion.domain.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.pipeline.modules.ingestion.domain.validators.AreaConsistencyValidator;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AreaConsistencyValidator.class)
public @interface ValidAreaConsistency {
    String message() default "Price per square meter is too low";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
