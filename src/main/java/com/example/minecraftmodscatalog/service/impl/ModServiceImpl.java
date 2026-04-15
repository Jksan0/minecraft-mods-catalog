package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.ModVersion;
import com.example.minecraftmodscatalog.entity.Author;
import com.example.minecraftmodscatalog.entity.Category;
import com.example.minecraftmodscatalog.entity.Tag;
import com.example.minecraftmodscatalog.mapper.ModMapper;
import com.example.minecraftmodscatalog.repository.AuthorRepository;
import com.example.minecraftmodscatalog.repository.CategoryRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.TagRepository;
import com.example.minecraftmodscatalog.service.ModService;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ModServiceImpl implements ModService {
    private static final String MOD_NOT_FOUND_PREFIX = "Mod not found: ";
    private static final String MOD_NAME_ALREADY_EXISTS_PREFIX = "Mod name already exists: ";

    private final ModRepository modRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ModDto> getAllMods() {
        return modRepository.findAllWithGraph().stream()
                .map(ModMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModDto> getModsByAuthor(final String authorName) {
        return modRepository.findByAuthorName(authorName).stream()
                .map(ModMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ModDto getModById(final Long id) {
        return modRepository.findByIdWithGraph(id)
                .map(ModMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(MOD_NOT_FOUND_PREFIX + id));
    }

    @Override
    @Transactional
    public ModDto createMod(final ModCreateDto createDto) {
        validateUniqueNameForCreate(createDto.getName());
        Mod saved = modRepository.save(buildModGraph(createDto));
        return ModMapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<ModDto> createMods(final List<ModCreateDto> createDtos) {
        return createModsInternal(createDtos);
    }

    @Override
    public List<ModDto> createModsWithoutTransaction(final List<ModCreateDto> createDtos) {
        if (createDtos == null || createDtos.isEmpty()) {
            throw new IllegalArgumentException("Request body must contain at least one mod");
        }

        Set<String> batchNames = new HashSet<>();
        List<ModDto> created = new java.util.ArrayList<>();
        for (ModCreateDto createDto : createDtos) {
            if (createDto == null || createDto.getName() == null || createDto.getName().isBlank()) {
                throw new IllegalArgumentException("Mod name is required");
            }
            String normalizedName = createDto.getName().toLowerCase(Locale.ROOT);
            if (!batchNames.add(normalizedName)) {
                throw new IllegalArgumentException(MOD_NAME_ALREADY_EXISTS_PREFIX + createDto.getName());
            }

            validateUniqueNameForCreate(createDto.getName());
            Mod saved = modRepository.save(buildModGraph(createDto));
            created.add(ModMapper.toDto(saved));
        }
        return created;
    }

    @Override
    @Transactional
    public ModDto updateMod(final Long id, final ModCreateDto updateDto) {
        Mod existing = modRepository.findByIdWithGraph(id)
                .orElseThrow(() -> new EntityNotFoundException(MOD_NOT_FOUND_PREFIX + id));
        validateUniqueNameForUpdate(id, updateDto.getName());
        existing.setName(updateDto.getName());
        existing.setDescription(updateDto.getDescription());
        existing.setAuthor(resolveAuthor(updateDto.getAuthorName()));
        existing.setCategory(resolveCategory(extractCategoryName(updateDto)));
        existing.setTags(resolveTags(updateDto.getTagNames()));

        existing.getVersions().clear();
        for (var versionDto : updateDto.getVersions()) {
            ModVersion version = new ModVersion();
            version.setVersionName(versionDto.getVersionName());
            version.setDownloadCount(versionDto.getDownloadCount());
            version.setMod(existing);
            existing.getVersions().add(version);
        }
        return ModMapper.toDto(modRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteMod(final Long id) {
        Mod mod = modRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MOD_NOT_FOUND_PREFIX + id));

        Long authorId = mod.getAuthor().getId();
        modRepository.deleteTagLinksByModId(id);
        modRepository.deleteVersionsByModId(id);
        modRepository.deleteById(id);
        modRepository.flush();

        if (!modRepository.existsByAuthorId(authorId)) {
            authorRepository.deleteById(authorId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModDto> getModsNaiveNPlusOne() {
        return modRepository.findAllNaiveForNPlusOne().stream()
                .map(ModMapper::toDto)
                .toList();
    }

    private Mod buildModGraph(final ModCreateDto createDto) {
        Mod mod = new Mod();
        mod.setName(createDto.getName());
        mod.setDescription(createDto.getDescription());
        mod.setAuthor(resolveAuthor(createDto.getAuthorName()));
        mod.setCategory(resolveCategory(extractCategoryName(createDto)));
        mod.setTags(resolveTags(createDto.getTagNames()));

        for (var versionDto : createDto.getVersions()) {
            ModVersion version = new ModVersion();
            version.setVersionName(versionDto.getVersionName());
            version.setDownloadCount(versionDto.getDownloadCount());
            version.setMod(mod);
            mod.getVersions().add(version);
        }
        return mod;
    }

    private Author resolveAuthor(final String authorName) {
        return authorRepository.findByNameIgnoreCase(authorName)
                .orElseGet(() -> {
                    Author author = new Author();
                    author.setName(authorName);
                    return authorRepository.save(author);
                });
    }

    private Category resolveCategory(final String categoryName) {
        return categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(categoryName);
                    return categoryRepository.save(category);
                });
    }

    private String extractCategoryName(final ModCreateDto dto) {
        if (dto.getCategoryName() != null && !dto.getCategoryName().isBlank()) {
            return dto.getCategoryName();
        }
        if (dto.getCategoryNames() != null && !dto.getCategoryNames().isEmpty()) {
            return dto.getCategoryNames().getFirst();
        }
        throw new IllegalArgumentException("Category is required");
    }

    private Set<Tag> resolveTags(final List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames == null) {
            return tags;
        }
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        return tags;
    }

    private List<ModDto> createModsInternal(final List<ModCreateDto> createDtos) {
        if (createDtos == null || createDtos.isEmpty()) {
            throw new IllegalArgumentException("Request body must contain at least one mod");
        }

        Set<String> batchNames = new HashSet<>();
        for (ModCreateDto createDto : createDtos) {
            if (createDto == null || createDto.getName() == null || createDto.getName().isBlank()) {
                throw new IllegalArgumentException("Mod name is required");
            }
            String normalizedName = createDto.getName().toLowerCase(Locale.ROOT);
            if (!batchNames.add(normalizedName)) {
                throw new IllegalArgumentException(MOD_NAME_ALREADY_EXISTS_PREFIX + createDto.getName());
            }
            validateUniqueNameForCreate(createDto.getName());
        }

        return modRepository.saveAll(createDtos.stream()
                        .map(this::buildModGraph)
                        .toList())
                .stream()
                .map(ModMapper::toDto)
                .toList();
    }

    private void validateUniqueNameForCreate(final String name) {
        if (modRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException(MOD_NAME_ALREADY_EXISTS_PREFIX + name);
        }
    }

    private void validateUniqueNameForUpdate(final Long id, final String name) {
        if (modRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException(MOD_NAME_ALREADY_EXISTS_PREFIX + name);
        }
    }
}
