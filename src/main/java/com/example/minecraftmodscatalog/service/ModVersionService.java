package com.example.minecraftmodscatalog.service;

import com.example.minecraftmodscatalog.dto.ModVersionDto;
import com.example.minecraftmodscatalog.dto.ModVersionUpsertDto;
import java.util.List;

public interface ModVersionService {
    List<ModVersionDto> getAllVersions(Long modId);

    ModVersionDto getVersionById(Long id);

    ModVersionDto createVersion(ModVersionUpsertDto createDto);

    ModVersionDto updateVersion(Long id, ModVersionUpsertDto updateDto);

    void deleteVersion(Long id);
}
