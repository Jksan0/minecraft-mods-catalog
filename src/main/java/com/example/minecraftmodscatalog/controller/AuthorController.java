package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.dto.AuthorCreateDto;
import com.example.minecraftmodscatalog.dto.AuthorDto;
import com.example.minecraftmodscatalog.service.AuthorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<List<AuthorDto>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorDto> getAuthorById(@PathVariable final Long id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    @PostMapping
    public ResponseEntity<AuthorDto> createAuthor(@RequestBody final AuthorCreateDto createDto) {
        return ResponseEntity.status(201).body(authorService.createAuthor(createDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorDto> updateAuthor(
            @PathVariable final Long id,
            @RequestBody final AuthorCreateDto updateDto
    ) {
        return ResponseEntity.ok(authorService.updateAuthor(id, updateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable final Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
