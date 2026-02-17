package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        validateItem(itemDto);

        Item item = ItemMapper.toItem(itemDto, userId);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!item.getOwner().equals(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не является владельцем вещи");
        }

        if (itemDto.getName() != null && !itemDto.getName().trim().isEmpty()) {
            item.setName(itemDto.getName().trim());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().trim().isEmpty()) {
            item.setDescription(itemDto.getDescription().trim());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        item.setComments(comments);

        return ItemMapper.toItemDtoWithBooking(item, null, null);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        return itemRepository.findByOwner(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemBookingDto> getAllByOwnerWithBooking(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        List<Item> items = itemRepository.findByOwner(userId);

        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        List<Comment> allComments = commentRepository.findByItemIn(items);
        Map<Long, List<Comment>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        List<ItemBookingDto> result = new ArrayList<>();
        Pageable singleItem = PageRequest.of(0, 1);

        for (Item item : items) {
            LocalDateTime lastBooking = null;
            LocalDateTime nextBooking = null;

            List<Booking> lastBookings = bookingRepository.findLastBookings(item.getId(), singleItem);
            if (!lastBookings.isEmpty()) {
                lastBooking = lastBookings.get(0).getEnd();
            }

            List<Booking> nextBookings = bookingRepository.findNextBookings(item.getId(), singleItem);
            if (!nextBookings.isEmpty()) {
                nextBooking = nextBookings.get(0).getStart();
            }

            List<Comment> itemComments = commentsByItemId.getOrDefault(item.getId(), new ArrayList<>());

            ItemBookingDto dto = ItemMapper.toItemBookingDto(
                    item, lastBooking, nextBooking, itemComments);

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, String text) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!hasBooked) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду");
        }

        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Comment comment = CommentMapper.toComment(text.trim(), item, author);
        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(savedComment);
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().trim().isEmpty()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().trim().isEmpty()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности должен быть указан");
        }
    }
}
