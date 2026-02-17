package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        validateUserForCreate(userDto);

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Email уже существует");
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        if (userDto.getEmail() != null && !userDto.getEmail().trim().isEmpty()) {
            String newEmail = userDto.getEmail().trim();

            userRepository.findByEmail(newEmail)
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new ConflictException("Email уже существует");
                        }
                    });

            if (!newEmail.contains("@")) {
                throw new ValidationException("Некорректный формат email");
            }

            user.setEmail(newEmail);
        }

        if (userDto.getName() != null && !userDto.getName().trim().isEmpty()) {
            user.setName(userDto.getName().trim());
        }

        User updatedUser = userRepository.save(user);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getById(Long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }

    private void validateUserForCreate(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (!userDto.getEmail().contains("@")) {
            throw new ValidationException("Некорректный формат email");
        }

        if (userDto.getName() == null || userDto.getName().trim().isEmpty()) {
            throw new ValidationException("Имя не может быть пустым");
        }
    }
}
