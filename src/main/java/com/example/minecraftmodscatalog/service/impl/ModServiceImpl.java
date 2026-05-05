package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.dto.ModVersionCreateDto;
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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ModServiceImpl implements ModService {
    private static final String MOD_NOT_FOUND_PREFIX = "Mod not found: ";
    private static final String MOD_NAME_ALREADY_EXISTS_PREFIX = "Mod name already exists: ";

    private final ModRepository modRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    private final Map<ModFilterCacheKey, Page<ModDto>> modFilterCache = new HashMap<>();

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
        validateModPayload(createDto);
        validateUniqueNameForCreate(createDto.getName());
        Mod saved = modRepository.save(buildModGraph(createDto));
        modFilterCache.clear();
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
            validateModPayload(createDto);
            String normalizedName = createDto.getName().toLowerCase(Locale.ROOT);
            if (!batchNames.add(normalizedName)) {
                throw new IllegalArgumentException(MOD_NAME_ALREADY_EXISTS_PREFIX + createDto.getName());
            }

            validateUniqueNameForCreate(createDto.getName());
            Mod saved = modRepository.save(buildModGraph(createDto));
            created.add(ModMapper.toDto(saved));
        }
        modFilterCache.clear();
        return created;
    }

    @Override
    @Transactional
    public ModDto updateMod(final Long id, final ModCreateDto updateDto) {
        Mod existing = modRepository.findByIdWithGraph(id)
                .orElseThrow(() -> new EntityNotFoundException(MOD_NOT_FOUND_PREFIX + id));
        validateModPayload(updateDto);
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
        ModDto result = ModMapper.toDto(modRepository.save(existing));
        modFilterCache.clear();
        return result;
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
        modFilterCache.clear();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModDto> getModsNaiveNPlusOne() {
        return modRepository.findAllNaiveForNPlusOne().stream()
                .map(ModMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModDto> getModsWithFilters(String authorName, String categoryName,
                                           List<String> tagNames, boolean useNative, Pageable pageable) {
        ModFilterCacheKey key = buildKey(authorName, categoryName, tagNames, useNative, pageable);
        Page<ModDto> cached = modFilterCache.get(key);
        if (cached != null) {
            return cached;
        }
        List<String> queryTagNames = key.tagNames().isEmpty() ? null : key.tagNames();
        long tagCount = key.tagNames().size();
        Page<Long> idPage = useNative
                ? modRepository
                .findModIdsWithFiltersNative(key.authorName(), key.categoryName(), queryTagNames, tagCount, pageable)
                : modRepository
                .findModIdsWithFiltersJpql(key.authorName(), key.categoryName(), queryTagNames, tagCount, pageable);

        if (idPage.isEmpty()) {
            Page<ModDto> emptyPage = new PageImpl<>(List.of(), pageable, idPage.getTotalElements());
            modFilterCache.put(key, emptyPage);
            return emptyPage;
        }

        List<Long> ids = idPage.getContent();
        Map<Long, Mod> byId = modRepository.findAllWithGraphByIdIn(ids).stream()
                .collect(java.util.stream.Collectors
                        .toMap(Mod::getId, mod -> mod, (left, right) -> left, LinkedHashMap::new));
        List<ModDto> content = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(ModMapper::toDto)
                .toList();
        Page<ModDto> dtoPage = new PageImpl<>(content, pageable, idPage.getTotalElements());
        modFilterCache.put(key, dtoPage);
        return dtoPage;
    }

    private ModFilterCacheKey buildKey(String authorName, String categoryName, List<String> tagNames,
                                       boolean useNative, Pageable pageable) {
        String normalizedAuthorName = normalizeNullable(authorName);
        String normalizedCategoryName = normalizeNullable(categoryName);
        List<String> normalizedTags = normalizeTags(tagNames);
        return new ModFilterCacheKey(normalizedAuthorName, normalizedCategoryName, normalizedTags, useNative,
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().toString());
    }

    private List<String> normalizeTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }
        return tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .distinct()
                .sorted()
                .toList();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private record ModFilterCacheKey(
            String authorName,
            String categoryName,
            List<String> tagNames,
            boolean useNative,
            int pageNumber,
            int pageSize,
            String sort
    ) {
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
            validateModPayload(createDto);
            String normalizedName = createDto.getName().toLowerCase(Locale.ROOT);
            if (!batchNames.add(normalizedName)) {
                throw new IllegalArgumentException(MOD_NAME_ALREADY_EXISTS_PREFIX + createDto.getName());
            }
            validateUniqueNameForCreate(createDto.getName());
        }

        List<ModDto> result = modRepository.saveAll(createDtos.stream()
                        .map(this::buildModGraph)
                        .toList())
                .stream()
                .map(ModMapper::toDto)
                .toList();
        modFilterCache.clear();
        return result;
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

    private void validateModPayload(final ModCreateDto dto) {
        validateDtoPresence(dto);
        validateRequiredText(dto.getName(), "Mod name is required");
        validateRequiredText(dto.getDescription(), "Mod description is required");
        validateRequiredText(dto.getAuthorName(), "Author name is required");
        validateRequiredText(extractCategoryName(dto), "Category is required");
        validateVersions(dto);
    }

    private void validateDtoPresence(final ModCreateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
    }

    private void validateRequiredText(final String value, final String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateVersions(final ModCreateDto dto) {
        if (dto.getVersions() == null || dto.getVersions().isEmpty()) {
            throw new IllegalArgumentException("At least one mod version is required");
        }
        for (var version : dto.getVersions()) {
            validateVersion(version);
        }
    }

    private void validateVersion(final ModVersionCreateDto version) {
        if (version == null || version.getVersionName() == null || version.getVersionName().isBlank()) {
            throw new IllegalArgumentException("Version name is required");
        }
        if (version.getDownloadCount() < 0) {
            throw new IllegalArgumentException("Version downloadCount must be non-negative");
        }
    }
}
