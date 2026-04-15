package com.example.minecraftmodscatalog.mapper;

import com.example.minecraftmodscatalog.dto.AuthorDto;
import com.example.minecraftmodscatalog.entity.Author;

public final class AuthorMapper {
    private AuthorMapper() {
    }

    public static AuthorDto toDto(final Author author) {
        AuthorDto dto = new AuthorDto();
        dto.setId(author.getId());
        dto.setName(author.getName());
        return dto;
    }
}
