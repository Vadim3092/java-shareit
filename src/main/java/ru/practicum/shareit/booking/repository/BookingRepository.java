package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId, Long itemId, BookingStatus status, LocalDateTime end);

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.owner = :ownerId
            ORDER BY b.start DESC
            """)
    List<Booking> findByItemOwnerIdOrderByStartDesc(@Param("ownerId") Long ownerId);

    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.item.id = :itemId
            AND b.status IN ('APPROVED', 'WAITING')
            AND b.start < :end AND b.end > :start
            """)
    boolean existsOverlappingBookings(
            @Param("itemId") Long itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id = :itemId
            AND b.status = 'APPROVED'
            AND b.end < CURRENT_TIMESTAMP
            ORDER BY b.end DESC
            """)
    List<Booking> findLastBookings(@Param("itemId") Long itemId, Pageable pageable);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id = :itemId
            AND b.status = 'APPROVED'
            AND b.start > CURRENT_TIMESTAMP
            ORDER BY b.start ASC
            """)
    List<Booking> findNextBookings(@Param("itemId") Long itemId, Pageable pageable);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id IN :itemIds
            AND b.status = 'APPROVED'
            AND b.end < CURRENT_TIMESTAMP
            ORDER BY b.end DESC
            """)
    List<Booking> findLastBookingsForItems(@Param("itemIds") List<Long> itemIds);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id IN :itemIds
            AND b.status = 'APPROVED'
            AND b.start > CURRENT_TIMESTAMP
            ORDER BY b.start ASC
            """)
    List<Booking> findNextBookingsForItems(@Param("itemIds") List<Long> itemIds);
}