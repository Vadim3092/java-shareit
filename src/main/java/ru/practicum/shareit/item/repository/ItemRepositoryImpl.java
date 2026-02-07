package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        if (item.getId() != null && items.containsKey(item.getId())) {
            Item existingItem = items.get(item.getId());

            if (item.getName() != null && !item.getName().trim().isEmpty()) {
                existingItem.setName(item.getName());
            }

            if (item.getDescription() != null && !item.getDescription().trim().isEmpty()) {
                existingItem.setDescription(item.getDescription());
            }

            if (item.getAvailable() != null) {
                existingItem.setAvailable(item.getAvailable());
            }

            items.put(item.getId(), existingItem);
            return existingItem;
        }
        return save(item);
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> ownerId.equals(item.getOwner()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(searchText)) ||
                        (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
    }
}
