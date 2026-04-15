package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.AuthorCreateDto;
import com.example.minecraftmodscatalog.dto.AuthorDto;
import com.example.minecraftmodscatalog.entity.Author;
import com.example.minecraftmodscatalog.mapper.AuthorMapper;
import com.example.minecraftmodscatalog.repository.AuthorRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.service.AuthorService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {
    private static final String AUTHOR_NOT_FOUND_PREFIX = "Author not found: ";
    private static final String AUTHOR_NAME_ALREADY_EXISTS_PREFIX = "Author name already exists: ";
    private static final String AUTHOR_IS_USED_PREFIX = "Cannot delete author, it is used by mods: ";

    private final AuthorRepository authorRepository;
    private final ModRepository modRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AuthorDto> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(AuthorMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorDto getAuthorById(final Long id) {
        return authorRepository.findById(id)
                .map(AuthorMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(AUTHOR_NOT_FOUND_PREFIX + id));
    }

    @Override
    @Transactional
    public AuthorDto createAuthor(final AuthorCreateDto createDto) {
        validateName(createDto.getName());
        if (authorRepository.existsByNameIgnoreCase(createDto.getName())) {
            throw new IllegalArgumentException(AUTHOR_NAME_ALREADY_EXISTS_PREFIX + createDto.getName());
        }

        Author author = new Author();
        author.setName(createDto.getName().trim());
        return AuthorMapper.toDto(authorRepository.save(author));
    }

    @Override
    @Transactional
    public AuthorDto updateAuthor(final Long id, final AuthorCreateDto updateDto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AUTHOR_NOT_FOUND_PREFIX + id));
        validateName(updateDto.getName());
        if (authorRepository.existsByNameIgnoreCaseAndIdNot(updateDto.getName(), id)) {
            throw new IllegalArgumentException(AUTHOR_NAME_ALREADY_EXISTS_PREFIX + updateDto.getName());
        }
        author.setName(updateDto.getName().trim());
        return AuthorMapper.toDto(authorRepository.save(author));
    }

    @Override
    @Transactional
    public void deleteAuthor(final Long id) {
        if (!authorRepository.existsById(id)) {
            throw new EntityNotFoundException(AUTHOR_NOT_FOUND_PREFIX + id);
        }
        if (modRepository.existsByAuthorId(id)) {
            throw new IllegalStateException(AUTHOR_IS_USED_PREFIX + id);
        }
        authorRepository.deleteById(id);
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Author name is required");
        }
    }
}
