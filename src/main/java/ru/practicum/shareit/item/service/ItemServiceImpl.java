package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        validateItem(itemDto, true);

        Item item = ItemMapper.toItem(itemDto, userId);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!userId.equals(item.getOwner())) {
            throw new ForbiddenException("Недостаточно прав для редактирования вещи");
        }

        validateItem(itemDto, false);

        Item itemToUpdate = ItemMapper.toItem(itemDto, userId);
        itemToUpdate.setId(itemId);
        Item updatedItem = itemRepository.update(itemToUpdate);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        return itemRepository.findById(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
    }

    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + ownerId + " не найден"));

        return itemRepository.findAllByOwner(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void validateItem(ItemDto itemDto, boolean isCreate) {
        if (isCreate) {
            if (itemDto.getName() == null || itemDto.getName().trim().isEmpty()) {
                throw new ValidationException("Название не может быть пустым");
            }

            if (itemDto.getDescription() == null || itemDto.getDescription().trim().isEmpty()) {
                throw new ValidationException("Описание не может быть пустым");
            }

            if (itemDto.getAvailable() == null) {
                throw new ValidationException("Поле available обязательно для заполнения");
            }
        } else {
            if (itemDto.getName() != null && itemDto.getName().trim().isEmpty()) {
                throw new ValidationException("Название не может быть пустым");
            }

            if (itemDto.getDescription() != null && itemDto.getDescription().trim().isEmpty()) {
                throw new ValidationException("Описание не может быть пустым");
            }
        }
    }
}
