package ru.practicum.shareit.item.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommentRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenTextIsValid_thenNoViolations() {
        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("Отличная вещь!");

        Set<ConstraintViolation<CommentRequestDto>> violations = validator.validate(commentDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenTextIsNull_thenViolation() {
        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText(null);

        Set<ConstraintViolation<CommentRequestDto>> violations = validator.validate(commentDto);
        assertEquals(1, violations.size());
        assertEquals("Текст комментария не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenTextIsBlank_thenViolation() {
        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("   ");

        Set<ConstraintViolation<CommentRequestDto>> violations = validator.validate(commentDto);
        assertEquals(1, violations.size());
        assertEquals("Текст комментария не может быть пустым", violations.iterator().next().getMessage());
    }
}
