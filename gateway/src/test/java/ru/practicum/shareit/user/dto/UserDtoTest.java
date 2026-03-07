package ru.practicum.shareit.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john@test.com");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenNameIsNull_thenViolation() {
        UserDto userDto = new UserDto();
        userDto.setName(null);
        userDto.setEmail("john@test.com");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertEquals(1, violations.size());
        assertEquals("Имя не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenNameIsBlank_thenViolation() {
        UserDto userDto = new UserDto();
        userDto.setName("   ");
        userDto.setEmail("john@test.com");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertEquals(1, violations.size());
        assertEquals("Имя не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailIsNull_thenViolation() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail(null);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertEquals(1, violations.size());
        assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailIsBlank_thenTwoViolations() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("   ");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertEquals(2, violations.size());

        boolean hasNotBlank = false;
        boolean hasEmail = false;

        for (ConstraintViolation<UserDto> violation : violations) {
            String message = violation.getMessage();
            if ("Email не может быть пустым".equals(message)) {
                hasNotBlank = true;
            } else if ("Некорректный формат email".equals(message)) {
                hasEmail = true;
            }
        }

        assertTrue(hasNotBlank, "Должно быть сообщение о пустом email");
        assertTrue(hasEmail, "Должно быть сообщение о неверном формате email");
    }

    @Test
    void whenEmailIsInvalid_thenViolation() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("not-an-email");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertEquals(1, violations.size());
        assertEquals("Некорректный формат email", violations.iterator().next().getMessage());
    }
}
