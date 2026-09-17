package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.AuthorCreateDto;
import com.example.minecraftmodscatalog.dto.AuthorDto;
import com.example.minecraftmodscatalog.entity.Author;
import com.example.minecraftmodscatalog.repository.AuthorRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    void getAllAuthors_shouldReturnMappedAuthors() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Alice");
        when(authorRepository.findAll()).thenReturn(List.of(author));

        List<AuthorDto> result = service.getAllAuthors();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Alice");
    }

    @Test
    void getAuthorById_shouldReturnAuthor() {
        Author author = new Author();
        author.setId(2L);
        author.setName("Bob");
        when(authorRepository.findById(2L)).thenReturn(Optional.of(author));

        AuthorDto result = service.getAuthorById(2L);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Bob");
    }

    @Test
    void getAuthorById_shouldThrowWhenMissing() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAuthorById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author not found: 99");
    }

    @Test
    void createAuthor_shouldSaveTrimmedName() {
        AuthorCreateDto dto = new AuthorCreateDto();
        dto.setName("  Alice  ");
        Author saved = new Author();
        saved.setId(1L);
        saved.setName("Alice");

        when(authorRepository.existsByNameIgnoreCase("  Alice  ")).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenReturn(saved);

        AuthorDto result = service.createAuthor(dto);

        assertThat(result.getName()).isEqualTo("Alice");
    }

    @Test
    void createAuthor_shouldRejectBlankName() {
        AuthorCreateDto dto = new AuthorCreateDto();
        dto.setName("   ");

        assertThatThrownBy(() -> service.createAuthor(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Author name is required");
    }

    @Test
    void createAuthor_shouldRejectDuplicateName() {
        AuthorCreateDto dto = new AuthorCreateDto();
        dto.setName("Alice");
        when(authorRepository.existsByNameIgnoreCase("Alice")).thenReturn(true);

        assertThatThrownBy(() -> service.createAuthor(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Author name already exists: Alice");
    }

    @Test
    void updateAuthor_shouldSaveTrimmedName() {
        AuthorCreateDto dto = new AuthorCreateDto();
        dto.setName("  Bob  ");
        Author author = new Author();
        author.setId(4L);
        author.setName("Old");

        when(authorRepository.findById(4L)).thenReturn(Optional.of(author));
        when(authorRepository.existsByNameIgnoreCaseAndIdNot("  Bob  ", 4L)).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        AuthorDto result = service.updateAuthor(4L, dto);

        assertThat(result.getName()).isEqualTo("Bob");
        assertThat(author.getName()).isEqualTo("Bob");
    }

    @Test
    void updateAuthor_shouldRejectDuplicateName() {
        AuthorCreateDto dto = new AuthorCreateDto();
        dto.setName("Alice");
        Author author = new Author();
        author.setId(4L);
        author.setName("Old");

        when(authorRepository.findById(4L)).thenReturn(Optional.of(author));
        when(authorRepository.existsByNameIgnoreCaseAndIdNot("Alice", 4L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateAuthor(4L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Author name already exists: Alice");
    }

    @Test
    void deleteAuthor_shouldDeleteWhenUnused() {
        when(authorRepository.existsById(10L)).thenReturn(true);
        when(modRepository.existsByAuthorId(10L)).thenReturn(false);

        service.deleteAuthor(10L);

        verify(authorRepository).deleteById(10L);
    }

    @Test
    void deleteAuthor_shouldThrowWhenNotFound() {
        when(authorRepository.existsById(11L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteAuthor(11L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author not found: 11");
    }

    @Test
    void deleteAuthor_shouldThrowIfUsedByMods() {
        when(authorRepository.existsById(10L)).thenReturn(true);
        when(modRepository.existsByAuthorId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteAuthor(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("used by mods");
    }

    @Test
    void syntheticLambdas_shouldBeInvoked() throws Exception {
        EntityNotFoundException missing = (EntityNotFoundException) invokeDeclared(AuthorServiceImpl.class, service, "lambda$getAuthorById$0", 99L);
        assertThat(missing).isInstanceOf(EntityNotFoundException.class);
        assertThat(missing.getMessage()).contains("Author not found: 99");

        EntityNotFoundException updateMissing = (EntityNotFoundException) invokeDeclared(AuthorServiceImpl.class, service, "lambda$updateAuthor$1", 44L);
        assertThat(updateMissing).isInstanceOf(EntityNotFoundException.class);
        assertThat(updateMissing.getMessage()).contains("Author not found: 44");
    }

    private Object invokeDeclared(Class<?> owner, Object target, String methodName, Object... args) throws Exception {
        for (var candidate : owner.getDeclaredMethods()) {
            if (!candidate.getName().equals(methodName) || candidate.getParameterCount() != args.length) {
                continue;
            }
            boolean matches = true;
            for (int i = 0; i < candidate.getParameterTypes().length; i++) {
                if (args[i] == null) {
                    if (candidate.getParameterTypes()[i].isPrimitive()) {
                        matches = false;
                        break;
                    }
                    continue;
                }
                if (!candidate.getParameterTypes()[i].isAssignableFrom(args[i].getClass())) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                candidate.setAccessible(true);
                return candidate.invoke(target, args);
            }
        }
        throw new NoSuchMethodException(methodName);
    }
}
