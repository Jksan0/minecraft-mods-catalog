package com.example.minecraftmodscatalog.service;

import com.example.minecraftmodscatalog.dto.AuthorCreateDto;
import com.example.minecraftmodscatalog.dto.AuthorDto;
import java.util.List;

public interface AuthorService {
    List<AuthorDto> getAllAuthors();

    AuthorDto getAuthorById(Long id);

    AuthorDto createAuthor(AuthorCreateDto createDto);

    AuthorDto updateAuthor(Long id, AuthorCreateDto updateDto);

    void deleteAuthor(Long id);
}
