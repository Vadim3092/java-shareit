package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        validateUserForCreate(userDto);

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ConflictException("Email уже существует");
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        if (userDto.getEmail() != null && !userDto.getEmail().trim().isEmpty()) {
            userRepository.findByEmail(userDto.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new ConflictException("Email уже существует");
                        }
                    });

            String email = userDto.getEmail().trim();
            if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
                throw new ValidationException("Некорректный формат email");
            }
        }

        if (userDto.getName() != null && userDto.getName().trim().isEmpty()) {
            throw new ValidationException("Имя не может быть пустым");
        }

        User user = UserMapper.toUser(userDto);
        user.setId(userId);
        User updatedUser = userRepository.update(user);
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
    public void delete(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userRepository.deleteById(userId);
    }

    private void validateUserForCreate(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email не может быть пустым");
        }

        String email = userDto.getEmail().trim();
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new ValidationException("Некорректный формат email");
        }

        if (userDto.getName() == null || userDto.getName().trim().isEmpty()) {
            throw new ValidationException("Имя не может быть пустым");
        }
    }
}
