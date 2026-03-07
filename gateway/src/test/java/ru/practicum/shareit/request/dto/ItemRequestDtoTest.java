package ru.practicum.shareit.request.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenDescriptionIsValid_thenNoViolations() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужен мощный перфоратор");

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(requestDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenDescriptionIsNull_thenViolation() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription(null);

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(requestDto);
        assertEquals(1, violations.size());
        assertEquals("Описание запроса не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenDescriptionIsBlank_thenViolation() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("   ");

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(requestDto);
        assertEquals(1, violations.size());
        assertEquals("Описание запроса не может быть пустым", violations.iterator().next().getMessage());
    }
}
