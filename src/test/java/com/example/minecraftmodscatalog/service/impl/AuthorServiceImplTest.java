package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.AuthorCreateDto;
import com.example.minecraftmodscatalog.entity.Author;
import com.example.minecraftmodscatalog.repository.AuthorRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {
    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private ModRepository modRepository;

    @InjectMocks
    private AuthorServiceImpl service;

    @Test
    void createAuthor_shouldSaveTrimmedName() {
        AuthorCreateDto dto = new AuthorCreateDto();
        dto.setName("  Alice  ");
        Author saved = new Author();
        saved.setId(1L);
        saved.setName("Alice");

        when(authorRepository.existsByNameIgnoreCase("  Alice  ")).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenReturn(saved);

        var result = service.createAuthor(dto);

        assertThat(result.getName()).isEqualTo("Alice");
    }

    @Test
    void deleteAuthor_shouldThrowIfUsedByMods() {
        when(authorRepository.existsById(10L)).thenReturn(true);
        when(modRepository.existsByAuthorId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteAuthor(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("used by mods");
    }
}
