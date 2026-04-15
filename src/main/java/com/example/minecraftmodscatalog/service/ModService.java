package com.example.minecraftmodscatalog.service;

import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import java.util.List;

public interface ModService {
    List<ModDto> getAllMods();
    List<ModDto> getModsByAuthor(String authorName);
    ModDto getModById(Long id);
    ModDto createMod(ModCreateDto createDto);
    List<ModDto> createMods(List<ModCreateDto> createDtos);
    List<ModDto> createModsWithoutTransaction(List<ModCreateDto> createDtos);
    ModDto updateMod(Long id, ModCreateDto updateDto);
    void deleteMod(Long id);
    List<ModDto> getModsNaiveNPlusOne();
}
