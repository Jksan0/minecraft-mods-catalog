package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.ModVersionDto;
import com.example.minecraftmodscatalog.dto.ModVersionUpsertDto;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.ModVersion;
import com.example.minecraftmodscatalog.mapper.ModVersionMapper;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.ModVersionRepository;
import com.example.minecraftmodscatalog.service.ModVersionService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModVersionServiceImpl implements ModVersionService {
    private static final String VERSION_NOT_FOUND_PREFIX = "Mod version not found: ";
    private static final String MOD_NOT_FOUND_PREFIX = "Mod not found: ";
    private static final String CANNOT_DELETE_LAST_VERSION = "Cannot delete the last version of a mod";

    private final ModVersionRepository modVersionRepository;
    private final ModRepository modRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ModVersionDto> getAllVersions(final Long modId) {
        if (modId == null) {
            return modVersionRepository.findAll().stream()
                    .map(ModVersionMapper::toDto)
                    .toList();
        }
        return modVersionRepository.findByModId(modId).stream()
                .map(ModVersionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ModVersionDto getVersionById(final Long id) {
        return modVersionRepository.findById(id)
                .map(ModVersionMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(VERSION_NOT_FOUND_PREFIX + id));
    }

    @Override
    @Transactional
    public ModVersionDto createVersion(final ModVersionUpsertDto createDto) {
        validateUpsertDto(createDto);
        Mod mod = modRepository.findById(createDto.getModId())
                .orElseThrow(() -> new EntityNotFoundException(MOD_NOT_FOUND_PREFIX + createDto.getModId()));

        ModVersion version = new ModVersion();
        version.setVersionName(createDto.getVersionName().trim());
        version.setDownloadCount(createDto.getDownloadCount());
        version.setMod(mod);

        return ModVersionMapper.toDto(modVersionRepository.save(version));
    }

    @Override
    @Transactional
    public ModVersionDto updateVersion(final Long id, final ModVersionUpsertDto updateDto) {
        validateUpsertDto(updateDto);
        ModVersion version = modVersionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(VERSION_NOT_FOUND_PREFIX + id));
        Mod targetMod = modRepository.findById(updateDto.getModId())
                .orElseThrow(() -> new EntityNotFoundException(MOD_NOT_FOUND_PREFIX + updateDto.getModId()));

        Long currentModId = version.getMod().getId();
        if (!currentModId.equals(targetMod.getId())
                && modVersionRepository.countByModId(currentModId) <= 1) {
            throw new IllegalStateException(CANNOT_DELETE_LAST_VERSION);
        }

        version.setVersionName(updateDto.getVersionName().trim());
        version.setDownloadCount(updateDto.getDownloadCount());
        version.setMod(targetMod);
        return ModVersionMapper.toDto(modVersionRepository.save(version));
    }

    @Override
    @Transactional
    public void deleteVersion(final Long id) {
        ModVersion version = modVersionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(VERSION_NOT_FOUND_PREFIX + id));

        if (modVersionRepository.countByModId(version.getMod().getId()) <= 1) {
            throw new IllegalStateException(CANNOT_DELETE_LAST_VERSION);
        }
        modVersionRepository.deleteById(id);
    }

    private void validateUpsertDto(final ModVersionUpsertDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (dto.getVersionName() == null || dto.getVersionName().isBlank()) {
            throw new IllegalArgumentException("Version name is required");
        }
        if (dto.getModId() == null) {
            throw new IllegalArgumentException("modId is required");
        }
        if (dto.getDownloadCount() < 0) {
            throw new IllegalArgumentException("downloadCount must be non-negative");
        }
    }
}
