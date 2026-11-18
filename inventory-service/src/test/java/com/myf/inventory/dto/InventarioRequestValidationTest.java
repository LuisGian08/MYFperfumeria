package com.myf.inventory.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventarioRequestValidationTest {
    @Test
    void stockAndMovementRequestsRejectNegativeAmounts() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertThat(validator.validate(new StockConfigRequest(-1, 0, 0, 0)))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("stockActual");
            assertThat(validator.validate(new MovimientoRequest(-1, "Reposición", 1L, null)))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("cantidad");
            assertThat(validator.validate(new MovimientoDecantRequest(-1, "Ajuste", 1L, null)))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("mililitros");
        }
    }
}
