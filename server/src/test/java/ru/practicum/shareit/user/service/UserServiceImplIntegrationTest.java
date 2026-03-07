package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john@test.com");
    }

    @Test
    void create_ShouldSaveUser() {
        UserDto savedUser = userService.create(userDto);

        assertNotNull(savedUser.getId(), "ID не должен быть null");
        assertEquals("John Doe", savedUser.getName(), "Имя должно совпадать");
        assertEquals("john@test.com", savedUser.getEmail(), "Email должен совпадать");
    }

    @Test
    void create_WithDuplicateEmail_ShouldThrowException() {
        userService.create(userDto);

        UserDto duplicateUser = new UserDto();
        duplicateUser.setName("Jane Doe");
        duplicateUser.setEmail("john@test.com");

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.create(duplicateUser));
        assertEquals("Email уже существует", exception.getMessage());
    }

    @Test
    void getById_ShouldReturnUser() {
        UserDto savedUser = userService.create(userDto);

        UserDto foundUser = userService.getById(savedUser.getId());

        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getName(), foundUser.getName());
        assertEquals(savedUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void getById_WithWrongId_ShouldThrowException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getById(999L));
        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }

    @Test
    void update_ShouldChangeUserData() {
        UserDto savedUser = userService.create(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@test.com");

        UserDto updatedUser = userService.update(savedUser.getId(), updateDto);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@test.com", updatedUser.getEmail());
        assertEquals(savedUser.getId(), updatedUser.getId());
    }

    @Test
    void update_OnlyName_ShouldNotChangeEmail() {
        UserDto savedUser = userService.create(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");

        UserDto updatedUser = userService.update(savedUser.getId(), updateDto);

        assertEquals("New Name", updatedUser.getName());
        assertEquals(savedUser.getEmail(), updatedUser.getEmail());
    }

    @Test
    void getAll_ShouldReturnAllUsers() {
        userService.create(userDto);

        UserDto secondUser = new UserDto();
        secondUser.setName("Jane Doe");
        secondUser.setEmail("jane@test.com");
        userService.create(secondUser);

        var users = userService.getAll();

        assertEquals(2, users.size());
    }

    @Test
    void delete_ShouldRemoveUser() {
        UserDto savedUser = userService.create(userDto);

        userService.delete(savedUser.getId());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getById(savedUser.getId()));
        assertEquals("Пользователь с id=" + savedUser.getId() + " не найден", exception.getMessage());
    }
}