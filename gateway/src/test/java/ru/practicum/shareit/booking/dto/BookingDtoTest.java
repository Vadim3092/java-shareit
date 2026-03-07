package ru.practicum.shareit.booking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookingDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenItemIdIsNull_thenViolation() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(null);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertEquals(1, violations.size());
        assertEquals("ID вещи обязательно", violations.iterator().next().getMessage());
    }

    @Test
    void whenStartIsNull_thenViolation() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(null);
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertEquals(1, violations.size());
        assertEquals("Дата начала обязательна", violations.iterator().next().getMessage());
    }

    @Test
    void whenEndIsNull_thenViolation() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(null);

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertEquals(1, violations.size());
        assertEquals("Дата окончания обязательна", violations.iterator().next().getMessage());
    }

    @Test
    void whenStartIsInPast_thenViolation() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertEquals(1, violations.size());
        assertEquals("Дата начала не может быть в прошлом", violations.iterator().next().getMessage());
    }

    @Test
    void whenEndIsInPast_thenViolation() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertEquals(1, violations.size());
        assertEquals("Дата окончания должна быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    void whenEndEqualsStart_thenNoViolation() {
        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(sameTime);
        bookingDto.setEnd(sameTime);

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertTrue(violations.isEmpty());
    }
}