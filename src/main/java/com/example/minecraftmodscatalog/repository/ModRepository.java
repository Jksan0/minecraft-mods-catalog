package com.example.minecraftmodscatalog.repository;

import com.example.minecraftmodscatalog.entity.Mod;
import java.util.List;
import java.util.Optional;

public interface ModRepository {
    List<Mod> findAll();
    Optional<Mod> findById(Long id);
    List<Mod> findByAuthor(String author);
    Mod save(Mod mod);
    void deleteById(Long id);
}