package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.mapper.ModMapper;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.service.ModService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModServiceImpl implements ModService {
    private final ModRepository modRepository;

    @Override
    public List<ModDto> getAllMods() {
        return modRepository.findAll().stream()
                .map(ModMapper::toDto)
                .toList();
    }

    @Override
    public Optional<ModDto> getModById(Long id) {
        return modRepository.findById(id)
                .map(ModMapper::toDto);
    }

    @Override
    public List<ModDto> getModsByAuthor(String author) {
        return modRepository.findByAuthor(author).stream()
                .map(ModMapper::toDto)
                .toList();
    }

    @Override
    public ModDto createMod(ModCreateDto createDto) {
        Mod mod = ModMapper.toEntity(createDto);
        Mod saved = modRepository.save(mod);
        return ModMapper.toDto(saved);
    }
}