package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }

        List<CommentDto> commentDtos = item.getComments() != null
                ? item.getComments().stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .lastBooking(null)
                .nextBooking(null)
                .comments(commentDtos)
                .build();
    }

    public static ItemDto toItemDtoWithBooking(Item item, LocalDateTime lastBooking, LocalDateTime nextBooking) {
        if (item == null) {
            return null;
        }

        List<CommentDto> commentDtos = item.getComments() != null
                ? item.getComments().stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(commentDtos)
                .build();
    }

    public static Item toItem(ItemDto itemDto, Long ownerId) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(ownerId)
                .requestId(itemDto.getRequestId())
                .build();
    }

    public static ItemBookingDto toItemBookingDto(Item item, LocalDateTime lastBooking, LocalDateTime nextBooking, List<Comment> comments) {
        List<CommentDto> commentDtos = comments != null
                ? comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return ItemBookingDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(commentDtos)
                .build();
    }
}
