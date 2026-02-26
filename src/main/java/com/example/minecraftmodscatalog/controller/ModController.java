package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.service.ModService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mods")
@RequiredArgsConstructor
public class ModController {
    private final ModService modService;

    // GET с @RequestParam: фильтрация по автору
    // Пример: /api/mods?author=Me
    @GetMapping
    public ResponseEntity<List<ModDto>> getMods(@RequestParam(required = false) String author) {
        if (author != null) {
            return ResponseEntity.ok(modService.getModsByAuthor(author));
        }
        return ResponseEntity.ok(modService.getAllMods());
    }

    // GET с @PathVariable: получение мода по ID
    // Пример: /api/mods/1
    @GetMapping("/{id}")
    public ResponseEntity<ModDto> getModById(@PathVariable Long id) {
        return modService.getModById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Остальные методы (POST и т.д.) можно добавить позже
}