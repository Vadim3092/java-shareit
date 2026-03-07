package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto dto, User requestor) {
        if (dto == null || requestor == null) {
            return null;
        }

        return ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest request) {
        if (request == null) {
            return null;
        }

        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(Collections.emptyList())
                .build();
    }

    public static ItemRequestResponseDto toItemRequestResponseDtoWithItems(
            ItemRequest request, List<ItemDto> items) {
        if (request == null) {
            return null;
        }

        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items != null ? items : Collections.emptyList())
                .build();
    }

    public static List<ItemRequestResponseDto> toItemRequestResponseDtoList(
            List<ItemRequest> requests) {
        if (requests == null) {
            return Collections.emptyList();
        }

        return requests.stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }
}
