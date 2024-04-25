package org.example.clearsolutionstest.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.clearsolutionstest.service.TimeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EighteenPlusValidator implements ConstraintValidator<EighteenPlusConstraint, LocalDate> {

    private final Integer minimalUserAge;
    private final TimeService timeService;

    public EighteenPlusValidator(@Qualifier("minimalUserAge") Integer minimalUserAge,
                           TimeService timeService) {
        this.minimalUserAge = minimalUserAge;
        this.timeService = timeService;
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value == null || timeService.utcNow().toLocalDate().minusYears(value.getYear()).getYear() >= minimalUserAge;
    }
}
