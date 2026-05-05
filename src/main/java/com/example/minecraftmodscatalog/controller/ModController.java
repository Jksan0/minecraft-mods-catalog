package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.service.ModService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
        return ResponseEntity.ok(modService.getModById(id));
    }

    @PostMapping
    public ResponseEntity<List<ModDto>> createMods(@RequestBody final List<ModCreateDto> createDtos) {
        return ResponseEntity.status(201).body(modService.createMods(createDtos));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModDto> updateMod(
            @PathVariable final Long id,
            @RequestBody final ModCreateDto updateDto
    ) {
        return ResponseEntity.ok(modService.updateMod(id, updateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMod(@PathVariable final Long id) {
        modService.deleteMod(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nplus1/naive")
    public ResponseEntity<List<ModDto>> naiveNPlusOne() {
        return ResponseEntity.ok(modService.getModsNaiveNPlusOne());
    }

    @GetMapping("/filtered")
    public ResponseEntity<Page<ModDto>> getModsWithFilters(
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) List<String> tagNames,
            @RequestParam(defaultValue = "false") boolean useNative,
            Pageable pageable
    ) {
        return ResponseEntity
                .ok(modService.getModsWithFilters(authorName, categoryName, tagNames, useNative, pageable));
    }

    @PostMapping("/demo/without-transaction")
    public ResponseEntity<List<ModDto>> withoutTransaction(
            @RequestBody final List<ModCreateDto> createDtos
    ) {
        return ResponseEntity.status(201).body(modService.createModsWithoutTransaction(createDtos));
    }
}
