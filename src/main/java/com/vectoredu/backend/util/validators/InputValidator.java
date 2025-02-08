package com.vectoredu.backend.util.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;


@Component
public class InputValidator implements ConstraintValidator<ValidInput, String> {

    private static final int MAX_LENGTH = 256;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Если поле необязательное, пропускаем валидацию
        }
        return value.length() <= MAX_LENGTH;
    }
}
