package com.vectoredu.backend.util.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = InputValidator.class)
public @interface ValidInput {
    String message() default "Max length is 256 characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
