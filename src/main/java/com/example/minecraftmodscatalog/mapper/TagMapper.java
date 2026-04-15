package com.example.minecraftmodscatalog.mapper;

import com.example.minecraftmodscatalog.dto.TagDto;
import com.example.minecraftmodscatalog.entity.Tag;

public final class TagMapper {
    private TagMapper() {
    }

    public static TagDto toDto(final Tag tag) {
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        return dto;
    }
}
