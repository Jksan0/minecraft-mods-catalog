package com.example.minecraftmodscatalog.service;

import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    Page<ModDto> getModsWithFiltersJpql(String authorName, String categoryName, List<String> tagNames,
                                        Pageable pageable);
    Page<ModDto> getModsWithFiltersNative(String authorName, String categoryName, List<String> tagNames,
                                          Pageable pageable);
}
