package com.example.minecraftmodscatalog.mapper;

import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.dto.ModVersionDto;
import com.example.minecraftmodscatalog.entity.Mod;
import java.util.Comparator;
import java.util.List;

public final class ModMapper {
    private ModMapper() {
    }

    public static ModDto toDto(final Mod mod) {
        ModDto dto = new ModDto();
        dto.setId(mod.getId());
        dto.setName(mod.getName());
        dto.setDescription(mod.getDescription());
        dto.setAuthorName(mod.getAuthor().getName());
        dto.setCategories(mod.getCategories().stream()
                .map(category -> category.getName())
                .sorted()
                .toList());
        dto.setTags(mod.getTags().stream()
                .map(tag -> tag.getName())
                .sorted()
                .toList());
        dto.setVersions(toVersionDtoList(mod));
        return dto;
    }

    private static List<ModVersionDto> toVersionDtoList(final Mod mod) {
        return mod.getVersions().stream()
                .sorted(Comparator.comparing(version -> version.getVersionName()))
                .map(version -> {
                    ModVersionDto dto = new ModVersionDto();
                    dto.setId(version.getId());
                    dto.setVersionName(version.getVersionName());
                    dto.setDownloadCount(version.getDownloadCount());
                    return dto;
                })
                .toList();
    }
}
