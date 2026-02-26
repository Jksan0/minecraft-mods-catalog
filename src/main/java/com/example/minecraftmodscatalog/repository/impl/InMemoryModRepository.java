package com.example.minecraftmodscatalog.repository.impl;

import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.repository.ModRepository;
import java.util.*;
import org.springframework.stereotype.Repository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryModRepository implements ModRepository {
    private final Map<Long, Mod> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Mod> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Mod> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Mod> findByAuthor(String author) {
        return storage.values().stream()
                .filter(mod -> mod.getAuthor().equalsIgnoreCase(author))
                .toList();
    }

    @Override
    public Mod save(Mod mod) {
        if (mod.getId() == null) {
            mod.setId(idGenerator.getAndIncrement());
        }
        storage.put(mod.getId(), mod);
        return mod;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}