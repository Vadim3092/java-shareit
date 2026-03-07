package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner");
        ownerDto.setEmail("owner" + System.currentTimeMillis() + "@test.com");
        owner = userService.create(ownerDto);

        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker" + System.currentTimeMillis() + "@test.com");
        booker = userService.create(bookerDto);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
        item = itemService.create(itemDto, owner.getId());

        start = LocalDateTime.now().plusDays(1);
        end = LocalDateTime.now().plusDays(2);
    }

    @Test
    void create_ShouldSaveBooking() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        assertNotNull(savedBooking.getId());
        assertEquals(booker.getId(), savedBooking.getBooker().getId());
        assertEquals(item.getId(), savedBooking.getItem().getId());
        assertEquals(BookingStatus.WAITING, savedBooking.getStatus());
        assertEquals(start, savedBooking.getStart());
        assertEquals(end, savedBooking.getEnd());
    }

    @Test
    void create_WithUnavailableItem_ShouldThrowException() {
        ItemDto updateDto = new ItemDto();
        updateDto.setAvailable(false);
        itemService.update(item.getId(), updateDto, owner.getId());

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(booker.getId(), bookingDto));

        assertEquals("Вещь с id=" + item.getId() + " недоступна для бронирования",
                exception.getMessage());
    }

    @Test
    void create_ByOwner_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(owner.getId(), bookingDto));

        assertEquals("Владелец не может бронировать свою вещь", exception.getMessage());
    }

    @Test
    void approve_ShouldChangeStatusToApproved() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        BookingResponseDto approvedBooking = bookingService.approve(owner.getId(),
                savedBooking.getId(), true);

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    void approve_ShouldChangeStatusToRejected() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        BookingResponseDto rejectedBooking = bookingService.approve(owner.getId(),
                savedBooking.getId(), false);

        assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
    }

    @Test
    void approve_ByNotOwner_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        UserDto strangerDto = new UserDto();
        strangerDto.setName("Stranger");
        strangerDto.setEmail("stranger" + System.currentTimeMillis() + "@test.com");
        UserDto stranger = userService.create(strangerDto);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> bookingService.approve(stranger.getId(), savedBooking.getId(), true));

        assertEquals("Только владелец вещи может подтверждать бронирование",
                exception.getMessage());
    }

    @Test
    void approve_AlreadyApproved_ShouldThrowException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        bookingService.approve(owner.getId(), savedBooking.getId(), true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.approve(owner.getId(), savedBooking.getId(), true));

        assertEquals("Бронирование уже обработано", exception.getMessage());
    }

    @Test
    void getById_ShouldReturnBooking() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        BookingResponseDto foundBooking = bookingService.getById(booker.getId(),
                savedBooking.getId());

        assertEquals(savedBooking.getId(), foundBooking.getId());
    }

    @Test
    void getById_OwnerCanSee() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        BookingResponseDto foundBooking = bookingService.getById(owner.getId(),
                savedBooking.getId());

        assertEquals(savedBooking.getId(), foundBooking.getId());
    }

    @Test
    void getById_StrangerCannotSee() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        BookingResponseDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        UserDto strangerDto = new UserDto();
        strangerDto.setName("Stranger");
        strangerDto.setEmail("stranger" + System.currentTimeMillis() + "@test.com");
        UserDto stranger = userService.create(strangerDto);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getById(stranger.getId(), savedBooking.getId()));

        assertEquals("Нет доступа к просмотру этого бронирования", exception.getMessage());
    }

    @Test
    void getUserBookings_ShouldReturnAllBookings() {
        BookingDto bookingDto1 = new BookingDto();
        bookingDto1.setItemId(item.getId());
        bookingDto1.setStart(start);
        bookingDto1.setEnd(end);
        bookingService.create(booker.getId(), bookingDto1);

        BookingDto bookingDto2 = new BookingDto();
        bookingDto2.setItemId(item.getId());
        bookingDto2.setStart(start.plusDays(3));
        bookingDto2.setEnd(end.plusDays(3));
        bookingService.create(booker.getId(), bookingDto2);

        List<BookingResponseDto> bookings = bookingService.getUserBookings(booker.getId(), "ALL");

        assertEquals(2, bookings.size());
    }

    @Test
    void getUserBookings_WithStateWaiting_ShouldReturnWaiting() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingService.create(booker.getId(), bookingDto);

        List<BookingResponseDto> bookings = bookingService.getUserBookings(booker.getId(),
                "WAITING");

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
    }

    @Test
    void getOwnerBookings_ShouldReturnAllBookings() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingService.create(booker.getId(), bookingDto);

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(owner.getId(), "ALL");

        assertEquals(1, bookings.size());
    }
}
