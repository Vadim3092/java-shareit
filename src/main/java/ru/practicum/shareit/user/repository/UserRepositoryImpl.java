package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() != null && users.containsKey(user.getId())) {
            User existingUser = users.get(user.getId());
            if (user.getName() != null && !user.getName().trim().isEmpty()) {
                existingUser.setName(user.getName());
            }
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                existingUser.setEmail(user.getEmail());
            }
            users.put(user.getId(), existingUser);
            return existingUser;
        }
        return save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return users.values().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }
}
