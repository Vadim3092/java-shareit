package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long itemId, ItemDto itemDto, Long userId);

    ItemDto getById(Long itemId);

    List<ItemDto> getAllByOwner(Long userId);

    List<ItemBookingDto> getAllByOwnerWithBooking(Long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, String text);  // НОВЫЙ МЕТОД
}
