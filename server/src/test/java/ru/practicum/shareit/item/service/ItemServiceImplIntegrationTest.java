package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private UserDto owner;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        UserDto userDto = new UserDto();
        userDto.setName("Owner");
        userDto.setEmail("owner" + System.currentTimeMillis() + "@test.com");
        owner = userService.create(userDto);

        itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
    }

    @Test
    void create_ShouldSaveItem() {
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        assertNotNull(savedItem.getId(), "ID не должен быть null");
        assertEquals("Дрель", savedItem.getName());
        assertEquals("Аккумуляторная дрель", savedItem.getDescription());
        assertTrue(savedItem.getAvailable());
    }

    @Test
    void create_WithUnknownUser_ShouldThrowException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(itemDto, 999L));

        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }

    @Test
    void getById_ShouldReturnItem() {
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        ItemDto foundItem = itemService.getById(savedItem.getId());

        assertEquals(savedItem.getId(), foundItem.getId());
        assertEquals(savedItem.getName(), foundItem.getName());
        assertEquals(savedItem.getDescription(), foundItem.getDescription());
    }

    @Test
    void getById_WithWrongId_ShouldThrowException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getById(999L));

        assertEquals("Вещь с id=999 не найдена", exception.getMessage());
    }

    @Test
    void update_ShouldChangeItemData() {
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новое имя");
        updateDto.setDescription("Новое описание");
        updateDto.setAvailable(false);

        ItemDto updatedItem = itemService.update(savedItem.getId(), updateDto, owner.getId());

        assertEquals("Новое имя", updatedItem.getName());
        assertEquals("Новое описание", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
        assertEquals(savedItem.getId(), updatedItem.getId());
    }

    @Test
    void update_OnlyName_ShouldNotChangeOtherFields() {
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Только имя");

        ItemDto updatedItem = itemService.update(savedItem.getId(), updateDto, owner.getId());


        assertEquals("Только имя", updatedItem.getName());
        assertEquals(savedItem.getDescription(), updatedItem.getDescription());
        assertEquals(savedItem.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_ByNotOwner_ShouldThrowException() {
        UserDto anotherUser = new UserDto();
        anotherUser.setName("Another");
        anotherUser.setEmail("another" + System.currentTimeMillis() + "@test.com");
        UserDto another = userService.create(anotherUser);

        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Хакер");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(savedItem.getId(), updateDto, another.getId()));

        assertEquals("Пользователь с id=" + another.getId() + " не является владельцем вещи",
                exception.getMessage());
    }

    @Test
    void getAllByOwner_ShouldReturnAllItems() {
        itemService.create(itemDto, owner.getId());

        ItemDto secondItem = new ItemDto();
        secondItem.setName("Перфоратор");
        secondItem.setDescription("Мощный");
        secondItem.setAvailable(true);
        itemService.create(secondItem, owner.getId());

        var items = itemService.getAllByOwner(owner.getId());

        assertEquals(2, items.size());
    }

    @Test
    void search_ShouldFindItemsByText() {
        itemService.create(itemDto, owner.getId());

        ItemDto secondItem = new ItemDto();
        secondItem.setName("Молоток");
        secondItem.setDescription("Тяжелый");
        secondItem.setAvailable(true);
        itemService.create(secondItem, owner.getId());

        var foundByName = itemService.search("Дрель");
        assertEquals(1, foundByName.size());
        assertEquals("Дрель", foundByName.get(0).getName());

        var foundByDesc = itemService.search("Тяжелый");
        assertEquals(1, foundByDesc.size());
        assertEquals("Молоток", foundByDesc.get(0).getName());

        var foundByPart = itemService.search("дре");
        assertEquals(1, foundByPart.size());
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() {
        itemService.create(itemDto, owner.getId());

        var result = itemService.search("");
        assertTrue(result.isEmpty());

        result = itemService.search(null);
        assertTrue(result.isEmpty());

        result = itemService.search("   ");
        assertTrue(result.isEmpty());
    }
}
