package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.config.ApiErrorResponses;
import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.service.ModService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/mods")
@RequiredArgsConstructor
@Tag(name = "Mods", description = "Mod management API")
@ApiErrorResponses
public class ModController {
    private final ModService modService;

    @GetMapping
    @Operation(summary = "Get all mods or filter by author")
    public ResponseEntity<List<ModDto>> getMods(@RequestParam(required = false) final String author) {
        if (author != null && !author.isBlank()) {
            return ResponseEntity.ok(modService.getModsByAuthor(author));
        }
        return ResponseEntity.ok(modService.getAllMods());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mod by ID")
    public ResponseEntity<ModDto> getModById(@PathVariable final Long id) {
        return ResponseEntity.ok(modService.getModById(id));
    }

    @PostMapping
    @Operation(summary = "Create one or more mods")
    public ResponseEntity<List<ModDto>> createMods(
            @NotEmpty @Valid @RequestBody final List<ModCreateDto> createDtos) {
        return ResponseEntity.status(201).body(modService.createMods(createDtos));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update mod by ID")
    public ResponseEntity<ModDto> updateMod(
            @PathVariable final Long id,
            @Valid @RequestBody final ModCreateDto updateDto
    ) {
        return ResponseEntity.ok(modService.updateMod(id, updateDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete mod by ID")
    public ResponseEntity<Void> deleteMod(@PathVariable final Long id) {
        modService.deleteMod(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nplus1/naive")
    @Operation(summary = "Get all mods with N+1 issue (naive approach)")
    public ResponseEntity<List<ModDto>> naiveNPlusOne() {
        return ResponseEntity.ok(modService.getModsNaiveNPlusOne());
    }

    @GetMapping("/filtered/jpql")
    @Operation(summary = "Get mods with filters using JPQL")
    public ResponseEntity<Page<ModDto>> getModsWithFiltersJpql(
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) List<String> tagNames,
            @RequestParam(required = false) Integer page,
            Pageable pageable
    ) {
        Pageable effectivePageable = pageable;
        if (page != null) {
            if (page < 1) {
                throw new IllegalArgumentException("page must be >= 1");
            }
            effectivePageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());
        }
        return ResponseEntity
                .ok(modService.getModsWithFiltersJpql(authorName, categoryName, tagNames, effectivePageable));
    }

    @GetMapping("/filtered/native")
    @Operation(summary = "Get mods with filters using native SQL")
    public ResponseEntity<Page<ModDto>> getModsWithFiltersNative(
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) List<String> tagNames,
            @RequestParam(required = false) Integer page,
            Pageable pageable
    ) {
        Pageable effectivePageable = pageable;
        if (page != null) {
            if (page < 1) {
                throw new IllegalArgumentException("page must be >= 1");
            }
            effectivePageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());
        }
        return ResponseEntity
                .ok(modService.getModsWithFiltersNative(authorName, categoryName, tagNames, effectivePageable));
    }

    @PostMapping("/demo/without-transaction")
    @Operation(summary = "Create multiple mods without transaction")
    public ResponseEntity<List<ModDto>> withoutTransaction(
            @NotEmpty @Valid @RequestBody final List<ModCreateDto> createDtos
    ) {
        return ResponseEntity.status(201).body(modService.createModsWithoutTransaction(createDtos));
    }
}
