package com.example.minecraftmodscatalog.mapper;

import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.dto.ModVersionDto;
import com.example.minecraftmodscatalog.entity.Mod;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                version -> version.getId() != null
                                        ? "id:" + version.getId()
                                        : "name:" + version.getVersionName()
                                        + "|downloads:" + version.getDownloadCount(),
                                version -> version,
                                (first, duplicate) -> first,
                                LinkedHashMap::new
                        ),
                        deduplicated -> List.copyOf(deduplicated.values())
                ));
    }
}
