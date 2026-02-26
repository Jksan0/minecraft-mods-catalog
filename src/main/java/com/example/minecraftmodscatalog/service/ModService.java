package com.example.minecraftmodscatalog.service;

import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.dto.ModCreateDto;
import java.util.List;
import java.util.Optional;

public interface ModService {
    List<ModDto> getAllMods();
    Optional<ModDto> getModById(Long id);
    List<ModDto> getModsByAuthor(String author);
    ModDto createMod(ModCreateDto createDto); // на будущее
}