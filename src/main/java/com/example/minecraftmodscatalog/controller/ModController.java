package com.example.minecraftmodscatalog.controller;

import org.springframework.web.bind.annotation.*;
import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.service.ModService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/mods")
@RequiredArgsConstructor
public class ModController {
    private final ModService modService;

    @GetMapping
    public ResponseEntity<List<ModDto>> getMods(@RequestParam(required = false) final String author) {
        if (author != null && !author.isBlank()) {
            return ResponseEntity.ok(modService.getModsByAuthor(author));
        }
        return ResponseEntity.ok(modService.getAllMods());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModDto> getModById(@PathVariable final Long id) {
        return modService.getModById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Добавим POST, так как в сервисе метод уже есть
    @PostMapping
    public ResponseEntity<ModDto> createMod(@RequestBody final ModCreateDto createDto) {
        return ResponseEntity.status(201).body(modService.createMod(createDto));
    }
}