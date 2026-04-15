package com.example.minecraftmodscatalog.mapper;

import com.example.minecraftmodscatalog.dto.ModVersionDto;
import com.example.minecraftmodscatalog.entity.ModVersion;

public final class ModVersionMapper {
    private ModVersionMapper() {
    }

    public static ModVersionDto toDto(final ModVersion version) {
        ModVersionDto dto = new ModVersionDto();
        dto.setId(version.getId());
        dto.setVersionName(version.getVersionName());
        dto.setDownloadCount(version.getDownloadCount());
        return dto;
    }
}
