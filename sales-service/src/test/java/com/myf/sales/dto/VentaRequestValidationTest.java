package com.myf.sales.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VentaRequestValidationTest {
    @Test
    void saleRequestRejectsNegativeItemQuantity() {
        VentaRequest request = new VentaRequest(null, null, "YAPE", null, "WEB", null,
                List.of(new VentaRequest.Item(1L, 2L, -1)));

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            assertThat(validator.validate(request))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("items[0].cantidad");
        }
    }
}
