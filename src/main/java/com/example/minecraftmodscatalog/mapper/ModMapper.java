package com.example.minecraftmodscatalog.mapper;

import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.entity.Mod;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModMapper {

    public static ModDto toDto(final Mod mod) {
        return new ModDto(
                mod.getId(),
                mod.getName(),
                mod.getDescription(),
                mod.getAuthor(),
                mod.getVersion(),
                mod.getDownloadCount()
        );
    }

    public static Mod toEntity(final ModCreateDto dto) {
        Mod mod = new Mod();
        mod.setName(dto.getName());
        mod.setDescription(dto.getDescription());
        mod.setAuthor(dto.getAuthor());
        mod.setVersion(dto.getVersion());
        mod.setDownloadCount(dto.getDownloadCount());
        return mod;
    }
}