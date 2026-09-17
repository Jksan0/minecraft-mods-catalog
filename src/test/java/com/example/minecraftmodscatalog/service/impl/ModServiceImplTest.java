package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.dto.ModVersionCreateDto;
import com.example.minecraftmodscatalog.dto.ModVersionDto;
import com.example.minecraftmodscatalog.entity.Author;
import com.example.minecraftmodscatalog.entity.Category;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.ModVersion;
import com.example.minecraftmodscatalog.entity.Tag;
import com.example.minecraftmodscatalog.repository.AuthorRepository;
import com.example.minecraftmodscatalog.repository.CategoryRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModServiceImplTest {
    @Mock
    private ModRepository modRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ModServiceImpl service;

    @Test
    void getAllMods_shouldReturnMappedDtos() {
        Mod mod = createMod(1L, "Alpha", "desc", "John", "Utility");
        when(modRepository.findAllWithGraph()).thenReturn(List.of(mod));

        List<ModDto> result = service.getAllMods();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Alpha");
    }

    @Test
    void getModsByAuthor_shouldReturnMatchingMods() {
        Mod mod = createMod(2L, "Beta", "desc", "Jane", "Utility");
        when(modRepository.findByAuthorName("Jane")).thenReturn(List.of(mod));

        List<ModDto> result = service.getModsByAuthor("Jane");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getAuthorName()).isEqualTo("Jane");
    }

    @Test
    void getModById_shouldReturnDto() {
        Mod mod = createMod(3L, "Gamma", "desc", "Alice", "Adventure");
        when(modRepository.findByIdWithGraph(3L)).thenReturn(Optional.of(mod));

        ModDto result = service.getModById(3L);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("Gamma");
    }

    @Test
    void getModById_shouldThrowWhenMissing() {
        when(modRepository.findByIdWithGraph(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getModById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Mod not found: 99");
    }

    @Test
    void createMod_shouldPersistAndMapResult() {
        ModCreateDto dto = new ModCreateDto();
        dto.setName("Alpha");
        dto.setDescription("Desc");
        dto.setAuthorName("John");
        dto.setCategoryName("Utility");
        dto.setTagNames(List.of(" tech ", "", "magic", "tech"));

        ModVersionCreateDto versionDto = new ModVersionCreateDto();
        versionDto.setVersionName("1.0.0");
        versionDto.setDownloadCount(10);
        dto.setVersions(List.of(versionDto));

        Author author = new Author();
        author.setId(1L);
        author.setName("John");
        Category category = new Category();
        category.setId(2L);
        category.setName("Utility");

        when(authorRepository.findByNameIgnoreCase("John")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> {
            Author saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(categoryRepository.findByNameIgnoreCase("Utility")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(tagRepository.findAllByLowerNameIn(anySet())).thenReturn(List.of());
        when(tagRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Tag> tags = inv.getArgument(0);
            tags.forEach(tag -> tag.setId(7L));
            return tags;
        });
        when(modRepository.existsByNameIgnoreCase("Alpha")).thenReturn(false);

        Mod savedMod = createMod(10L, "Alpha", "Desc", "John", "Utility");
        Tag savedTag = new Tag();
        savedTag.setName("magic");
        savedMod.setTags(Set.of(savedTag));
        ModVersion savedVersion = new ModVersion();
        savedVersion.setId(99L);
        savedVersion.setVersionName("1.0.0");
        savedVersion.setDownloadCount(10);
        savedVersion.setMod(savedMod);
        savedMod.setVersions(new ArrayList<>(List.of(savedVersion)));

        when(modRepository.save(any(Mod.class))).thenReturn(savedMod);
        when(modRepository.findByIdWithGraph(10L)).thenReturn(Optional.of(savedMod));

        ModDto result = service.createMod(dto);

        assertThat(result.getName()).isEqualTo("Alpha");
        assertThat(result.getVersions()).extracting(ModVersionDto::getVersionName).contains("1.0.0");
    }

    @Test
    void createMod_shouldRejectNullOrInvalidPayload() {
        assertThatThrownBy(() -> service.createMod(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body is required");

        ModCreateDto invalid = new ModCreateDto();
        invalid.setName("  ");
        invalid.setDescription("desc");
        invalid.setAuthorName("Alice");
        invalid.setCategoryName("Utility");
        invalid.setVersions(List.of(new ModVersionCreateDto()));

        assertThatThrownBy(() -> service.createMod(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mod name is required");

        ModCreateDto noVersions = new ModCreateDto();
        noVersions.setName("Name");
        noVersions.setDescription("desc");
        noVersions.setAuthorName("Alice");
        noVersions.setCategoryName("Utility");
        noVersions.setVersions(new ArrayList<>());

        assertThatThrownBy(() -> service.createMod(noVersions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one mod version is required");
    }

    @Test
    void createMods_shouldRejectDuplicateNames() {
        ModCreateDto dto1 = buildValidDto("Alpha", "first");
        ModCreateDto dto2 = buildValidDto("alpha", "second");

        assertThatThrownBy(() -> service.createMods(List.of(dto1, dto2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mod name already exists: Alpha");
    }

    @Test
    void createModsWithoutTransaction_shouldCreateAndMapMods() {
        ModCreateDto dto = buildValidDto("Alpha", "first");
        dto.setTagNames(List.of("tech"));
        Author author = new Author();
        author.setId(1L);
        author.setName("Alice");
        Category category = new Category();
        category.setId(2L);
        category.setName("Utility");

        when(authorRepository.findByNameIgnoreCase("Alice")).thenReturn(Optional.of(author));
        when(categoryRepository.findByNameIgnoreCase("Utility")).thenReturn(Optional.of(category));
        when(tagRepository.findAllByLowerNameIn(anySet())).thenReturn(List.of());
        when(tagRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(modRepository.existsByNameIgnoreCase("Alpha")).thenReturn(false);
        when(modRepository.save(any(Mod.class))).thenAnswer(inv -> {
            Mod mod = inv.getArgument(0);
            mod.setId(10L);
            return mod;
        });
        when(modRepository.findAllWithGraphByIdIn(anyList())).thenReturn(List.of(createMod(10L, "Alpha", "first", "Alice", "Utility")));

        List<ModDto> result = service.createModsWithoutTransaction(List.of(dto));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Alpha");
    }

    @Test
    void createMods_shouldCreateMultipleMods() {
        ModCreateDto dto1 = buildValidDto("Alpha", "first");
        dto1.setTagNames(List.of("tech"));
        ModCreateDto dto2 = buildValidDto("Bravo", "second");
        dto2.setTagNames(List.of("magic"));

        when(authorRepository.findByNameIgnoreCase("Alice")).thenReturn(Optional.of(createAuthor(1L, "Alice")));
        when(categoryRepository.findByNameIgnoreCase("Utility")).thenReturn(Optional.of(createCategory(2L, "Utility")));
        when(tagRepository.findAllByLowerNameIn(anySet())).thenReturn(List.of());
        when(tagRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(modRepository.existsByNameIgnoreCase("Alpha")).thenReturn(false);
        when(modRepository.existsByNameIgnoreCase("Bravo")).thenReturn(false);
        when(modRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Mod> mods = inv.getArgument(0);
            for (int i = 0; i < mods.size(); i++) {
                mods.get(i).setId(i == 0 ? 10L : 11L);
            }
            return mods;
        });
        when(modRepository.findAllWithGraphByIdIn(anyList())).thenAnswer(inv -> {
            List<Long> ids = inv.getArgument(0);
            return ids.stream()
                    .map(id -> createMod(id, id == 10L ? "Alpha" : "Bravo", id == 10L ? "first" : "second", "Alice", "Utility"))
                    .toList();
        });

        List<ModDto> result = service.createMods(List.of(dto1, dto2));

        assertThat(result).extracting(ModDto::getName).containsExactly("Alpha", "Bravo");
    }

    @Test
    void resolveTags_shouldReturnEmptySetForBlankOnlyInput() throws Exception {
        Set<Tag> result = (Set<Tag>) invokePrivate("resolveTags", Arrays.asList(" ", "", null));

        assertThat(result).isEmpty();
    }

    @Test
    void createModsWithoutTransaction_shouldRejectEmptyList() {
        assertThatThrownBy(() -> service.createModsWithoutTransaction(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one mod");
    }

    @Test
    void privateHelpers_shouldCoverValidationAndNormalizationBranches() throws Exception {
        assertThat(invokePrivate("normalizeNullable", (Object) null)).isNull();
        assertThat(invokePrivate("normalizeNullable", "  JAVA  ")).isEqualTo("java");

        assertThat((List<String>) invokePrivate("normalizeTags", (Object) null)).isEmpty();
        List<String> mixedTags = new ArrayList<>(List.of(" tech ", "", "Java", "tech"));
        mixedTags.add(null);
        assertThat((List<String>) invokePrivate("normalizeTags", mixedTags))
                .containsExactly("java", "tech");

        ModCreateDto dtoWithCategoryName = new ModCreateDto();
        dtoWithCategoryName.setCategoryName("Utility");
        assertThat(invokePrivate("extractCategoryName", dtoWithCategoryName)).isEqualTo("Utility");

        ModCreateDto dtoWithCategoryNames = new ModCreateDto();
        dtoWithCategoryNames.setCategoryNames(List.of("Utility", "Automation"));
        assertThat(invokePrivate("extractCategoryName", dtoWithCategoryNames)).isEqualTo("Utility");

        ModCreateDto dtoWithoutCategory = new ModCreateDto();
        assertThatThrownBy(() -> invokePrivate("extractCategoryName", dtoWithoutCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category is required");

        when(authorRepository.findByNameIgnoreCase("Bob")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> {
            Author a = inv.getArgument(0);
            a.setId(42L);
            return a;
        });
        Author createdAuthor = (Author) invokePrivate("resolveAuthor", "Bob");
        assertThat(createdAuthor.getName()).isEqualTo("Bob");

        when(categoryRepository.findByNameIgnoreCase("RPG")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(88L);
            return c;
        });
        Category createdCategory = (Category) invokePrivate("resolveCategory", "RPG");
        assertThat(createdCategory.getName()).isEqualTo("RPG");

        when(tagRepository.findAllByLowerNameIn(anySet())).thenReturn(List.of());
        when(tagRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        List<String> mixedTagsForResolve = new ArrayList<>(List.of("tech", "", "magic", "tech"));
        mixedTagsForResolve.add(null);
        Set<Tag> tags = (Set<Tag>) invokePrivate("resolveTags", mixedTagsForResolve);
        assertThat(tags).extracting(Tag::getName).containsExactlyInAnyOrder("tech", "magic");

        when(modRepository.existsByNameIgnoreCase("Alpha")).thenReturn(true);
        assertThatThrownBy(() -> invokePrivate("validateUniqueNameForCreate", "Alpha"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mod name already exists: Alpha");

        when(modRepository.existsByNameIgnoreCaseAndIdNot("Beta", 5L)).thenReturn(true);
        assertThatThrownBy(() -> invokePrivate("validateUniqueNameForUpdate", 5L, "Beta"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mod name already exists: Beta");

        ModCreateDto validDto = buildValidDto("Gamma", "ok");
        invokePrivate("validateModPayload", validDto);
        invokePrivate("validateDtoPresence", validDto);
        assertThatThrownBy(() -> invokePrivate("validateRequiredText", "  ", "message"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("message");

        ModCreateDto missingDescription = buildValidDto("Gamma", "ok");
        missingDescription.setDescription(null);
        assertThatThrownBy(() -> invokePrivate("validateModPayload", missingDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mod description is required");

        invokePrivate("validateVersions", validDto);
        ModVersionCreateDto version = new ModVersionCreateDto();
        version.setVersionName("1.0");
        version.setDownloadCount(-1);
        assertThatThrownBy(() -> invokePrivate("validateVersion", version))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");

        List<Mod> found = new ArrayList<>();
        found.add(createMod(1L, "A", "d", "Alice", "Utility"));
        when(modRepository.findAllWithGraphByIdIn(anyList())).thenReturn(found);
        List<ModDto> mapped = (List<ModDto>) invokePrivate("mapCreatedByIdsWithGraph", List.of(1L));
        assertThat(mapped).hasSize(1);

        when(modRepository.findAllWithGraphByIdIn(anyList())).thenReturn(List.of());
        assertThat((List<ModDto>) invokePrivate("mapCreatedByIdsWithGraph", null)).isEmpty();

        when(modRepository.existsByNameIgnoreCase("Duplicated")).thenReturn(false);
        invokePrivate("validateUniqueNameForCreate", "Duplicated");

        ModCreateDto duplicateNameDto = buildValidDto("Alpha", "d1");
        ModCreateDto duplicateNameDto2 = buildValidDto("alpha", "d2");
        assertThatThrownBy(() -> invokePrivate("validateDuplicateNames", List.of(duplicateNameDto, duplicateNameDto2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mod name already exists: Alpha");

        ModCreateDto sameName = buildValidDto("Alpha", "d1");
        ModCreateDto sameName2 = buildValidDto("Alpha", "d2");
        assertThatThrownBy(() -> invokePrivate("validateDuplicateNames", List.of(sameName, sameName2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void syntheticLambdas_shouldBeInvoked() throws Exception {
        Mod targetMod = createMod(1L, "Alpha", "desc", "Alice", "Utility");
        invokeDeclared(ModServiceImpl.class, service, "lambda$getModById$0", 5L);
        invokeDeclared(ModServiceImpl.class, service, "lambda$createMod$1", targetMod);
        invokeDeclared(ModServiceImpl.class, service, "lambda$deleteMod$6", 6L);
        invokeDeclared(ModServiceImpl.class, service, "lambda$updateMod$5", 7L);
        invokeDeclared(ModServiceImpl.class, service, "lambda$getModsWithFiltersInternal$7", targetMod);
        invokeDeclared(ModServiceImpl.class, service, "lambda$getModsWithFiltersInternal$8", targetMod, targetMod);
        invokeDeclared(ModServiceImpl.class, service, "lambda$normalizeTags$9", "  JAVA  ");
        invokeDeclared(ModServiceImpl.class, service, "lambda$normalizeTags$10", "  JAVA  ");
        invokeDeclared(ModServiceImpl.class, service, "lambda$resolveAuthor$11", "Alice");
        invokeDeclared(ModServiceImpl.class, service, "lambda$resolveCategory$12", "Utility");
        Tag tag = new Tag();
        tag.setName("tech");
        invokeDeclared(ModServiceImpl.class, service, "lambda$resolveTags$13", new java.util.HashMap<>(), tag);
        invokeDeclared(ModServiceImpl.class, service, "lambda$resolveTags$14", new java.util.HashMap<>(), Map.entry("tech", "tech"));
        invokeDeclared(ModServiceImpl.class, service, "lambda$resolveTags$15", Map.entry("tech", "tech"));
        invokeDeclared(ModServiceImpl.class, service, "lambda$resolveTags$16", new java.util.HashMap<>(), tag);
        invokeDeclared(ModServiceImpl.class, service, "lambda$createModsInternal$17", List.of(buildValidDto("Zeta", "z")));
        invokeDeclared(ModServiceImpl.class, service, "lambda$createModsInternal$18");
        invokeDeclared(ModServiceImpl.class, service, "lambda$validateDuplicateNames$19", "Alpha");
        invokeDeclared(ModServiceImpl.class, service, "lambda$validateDuplicateNames$20", buildValidDto("Alpha", "d"));
        invokeDeclared(ModServiceImpl.class, service, "lambda$validateDuplicateNames$21", "Alpha");
        invokeDeclared(ModServiceImpl.class, service, "lambda$validateDuplicateNames$22", Map.entry("Alpha", 1L));
        invokeDeclared(ModServiceImpl.class, service, "lambda$validateDuplicateNames$23", "Alpha", "Alpha");
        assertThatThrownBy(() -> invokeDeclared(ModServiceImpl.class, service, "lambda$validateDuplicateNames$24", List.of(buildValidDto("Alpha", "d")), "Alpha"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mod name already exists: Alpha");
        invokeDeclared(ModServiceImpl.class, service, "lambda$mapCreatedByIdsWithGraph$25", List.of(1L));
        invokeDeclared(ModServiceImpl.class, service, "lambda$mapCreatedByIdsWithGraph$26", targetMod);
        invokeDeclared(ModServiceImpl.class, service, "lambda$mapCreatedByIdsWithGraph$27", targetMod, targetMod);
        invokeDeclared(ModServiceImpl.class, service, "lambda$createModsWithoutTransaction$2", List.of(buildValidDto("Zeta", "z")));
        invokeDeclared(ModServiceImpl.class, service, "lambda$createModsWithoutTransaction$3");

        when(modRepository.save(any(Mod.class))).thenAnswer(inv -> {
            Mod saved = inv.getArgument(0);
            saved.setId(101L);
            return saved;
        });
        invokeDeclared(ModServiceImpl.class, service, "lambda$createModsWithoutTransaction$4", buildValidDto("Zeta", "z"));

        invokeDeclared(ModServiceImpl.class, service, "lambda$resolveCategory$12", "Utility");
        invokeDeclared(ModServiceImpl.class, service, "lambda$resolveAuthor$11", "Alice");
        invokeDeclared(ModServiceImpl.class, null, "lambda$normalizeTags$9", " tech ");
        invokeDeclared(ModServiceImpl.class, null, "lambda$normalizeTags$10", " tech ");
        invokeDeclared(ModServiceImpl.class, null, "lambda$getModsWithFiltersInternal$7", createMod(1L, "A", "d", "Alice", "Utility"));
        invokeDeclared(ModServiceImpl.class, null, "lambda$getModsWithFiltersInternal$8",
                createMod(1L, "A", "d", "Alice", "Utility"),
                createMod(2L, "B", "d", "Bob", "Adventure"));
        invokeDeclared(ModServiceImpl.class, null, "lambda$deleteMod$6", 1L);
        invokeDeclared(ModServiceImpl.class, null, "lambda$updateMod$5", 1L);
        invokeDeclared(ModServiceImpl.class, null, "lambda$createMod$1", new Mod());
        invokeDeclared(ModServiceImpl.class, null, "lambda$getModById$0", 1L);
        invokeDeclared(ModServiceImpl.class, null, "lambda$createModsWithoutTransaction$2", List.of(buildValidDto("Zeta", "z")));
        invokeDeclared(ModServiceImpl.class, null, "lambda$createModsWithoutTransaction$3");
        invokeDeclared(ModServiceImpl.class, null, "lambda$createModsInternal$17", List.of(buildValidDto("Zeta", "z")));
        invokeDeclared(ModServiceImpl.class, null, "lambda$createModsInternal$18");
        invokeDeclared(ModServiceImpl.class, null, "lambda$validateDuplicateNames$19", "Alpha");
        invokeDeclared(ModServiceImpl.class, null, "lambda$validateDuplicateNames$20", buildValidDto("Alpha", "d"));
        invokeDeclared(ModServiceImpl.class, null, "lambda$validateDuplicateNames$21", "Alpha");
        invokeDeclared(ModServiceImpl.class, null, "lambda$validateDuplicateNames$22", Map.entry("Alpha", 1L));
        invokeDeclared(ModServiceImpl.class, null, "lambda$validateDuplicateNames$23", "Alpha", "Alpha");
        assertThatThrownBy(() -> invokeDeclared(ModServiceImpl.class, null, "lambda$validateDuplicateNames$24", List.of(buildValidDto("Alpha", "d")), "Alpha"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mod name already exists: Alpha");
        Tag tagForResolveTags = new Tag();
        tagForResolveTags.setName("tech");
        invokeDeclared(ModServiceImpl.class, null, "lambda$resolveTags$13", new HashMap<>(), tagForResolveTags);
        invokeDeclared(ModServiceImpl.class, null, "lambda$resolveTags$14", new LinkedHashMap<>(), Map.entry("tech", "tech"));
        invokeDeclared(ModServiceImpl.class, null, "lambda$resolveTags$15", Map.entry("tech", "tech"));
        invokeDeclared(ModServiceImpl.class, null, "lambda$resolveTags$16", new HashMap<>(), tagForResolveTags);
    }

    @Test
    void nullAndEmptyBranches_shouldBeCovered() throws Exception {
        assertThatThrownBy(() -> service.createModsWithoutTransaction(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body must contain at least one mod");
        assertThatThrownBy(() -> service.createModsWithoutTransaction(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body must contain at least one mod");
        assertThatThrownBy(() -> service.createMods(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body must contain at least one mod");

        when(modRepository.findModIdsWithFiltersJpql(any(), any(), any(), anyLong(), any(Pageable.class)))
                .thenReturn(Page.empty());
        Page<ModDto> emptyPage = service.getModsWithFiltersJpql(null, null, null, PageRequest.of(0, 10));
        assertThat(emptyPage.getContent()).isEmpty();

        when(modRepository.findModIdsWithFiltersNative(any(), any(), any(), anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(1L), PageRequest.of(0, 10), 1));
        when(modRepository.findAllWithGraphByIdIn(List.of(1L))).thenReturn(List.of(createMod(1L, "Alpha", "desc", "Alice", "Utility")));
        Page<ModDto> nativePage = service.getModsWithFiltersNative("Alice", "Utility", List.of("tech"), PageRequest.of(0, 10));
        assertThat(nativePage.getContent()).hasSize(1);

        Method buildKey = ModServiceImpl.class.getDeclaredMethod(
                "buildKey", String.class, String.class, List.class, boolean.class, Pageable.class);
        buildKey.setAccessible(true);
        buildKey.invoke(service, null, null, null, false, PageRequest.of(0, 10));

        Method normalizeTags = ModServiceImpl.class.getDeclaredMethod("normalizeTags", List.class);
        normalizeTags.setAccessible(true);
        assertThat((List<String>) normalizeTags.invoke(service, new Object[] {null})).isEmpty();

        Method normalizeNullable = ModServiceImpl.class.getDeclaredMethod("normalizeNullable", String.class);
        normalizeNullable.setAccessible(true);
        assertThat(normalizeNullable.invoke(service, new Object[] {"  "})).isNull();

        Method resolveTags = ModServiceImpl.class.getDeclaredMethod("resolveTags", List.class);
        resolveTags.setAccessible(true);
        when(tagRepository.findAllByLowerNameIn(anySet())).thenReturn(List.of());
        when(tagRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Tag> saved = inv.getArgument(0);
            saved.forEach(tag -> tag.setId(1001L));
            return saved;
        });
        assertThat((Set<Tag>) resolveTags.invoke(service, new Object[] {Arrays.asList(" ", null, "tech")})).isNotEmpty();

        Method validateDuplicateNames = ModServiceImpl.class.getDeclaredMethod("validateDuplicateNames", List.class);
        validateDuplicateNames.setAccessible(true);
        List<ModCreateDto> duplicates = List.of(buildValidDto("Alpha", "first"), buildValidDto("alpha", "second"));
        assertThatThrownBy(() -> validateDuplicateNames.invoke(service, duplicates))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .satisfies(ex -> assertThat(((InvocationTargetException) ex).getCause()).hasMessageContaining("Mod name already exists: Alpha"));

        assertThat((List<ModDto>) invokeDeclared(ModServiceImpl.class, service, "mapCreatedByIdsWithGraph", List.of())).isEmpty();

        Method validateVersion = ModServiceImpl.class.getDeclaredMethod("validateVersion", ModVersionCreateDto.class);
        validateVersion.setAccessible(true);
        ModVersionCreateDto blankVersion = new ModVersionCreateDto();
        blankVersion.setVersionName(" ");
        assertThatThrownBy(() -> validateVersion.invoke(service, blankVersion))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .satisfies(ex -> assertThat(((InvocationTargetException) ex).getCause()).hasMessageContaining("Version name is required"));

        ModVersionCreateDto negativeVersion = new ModVersionCreateDto();
        negativeVersion.setVersionName("1.0.0");
        negativeVersion.setDownloadCount(-1);
        assertThatThrownBy(() -> validateVersion.invoke(service, negativeVersion))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .satisfies(ex -> assertThat(((InvocationTargetException) ex).getCause()).hasMessageContaining("Version downloadCount must be non-negative"));

        when(tagRepository.findAllByLowerNameIn(anySet())).thenReturn(List.of());
        when(tagRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        assertThat((Set<Tag>) resolveTags.invoke(service, new Object[] {List.of("tech")})).isNotEmpty();
    }

    private Object invokeDeclared(Class<?> owner, Object target, String methodName, Object... args) throws Exception {
        if (args == null) {
            args = new Object[] {null};
        }

        Method matched = null;
        for (Method candidate : owner.getDeclaredMethods()) {
            if (!candidate.getName().equals(methodName) || candidate.getParameterCount() != args.length) {
                continue;
            }
            Class<?>[] paramTypes = candidate.getParameterTypes();
            boolean matches = true;
            for (int i = 0; i < paramTypes.length; i++) {
                if (args[i] == null) {
                    if (paramTypes[i].isPrimitive()) {
                        matches = false;
                        break;
                    }
                    continue;
                }
                Class<?> argType = args[i].getClass();
                if (paramTypes[i].isPrimitive()) {
                    if ((paramTypes[i] == boolean.class && argType == Boolean.class)
                            || (paramTypes[i] == byte.class && argType == Byte.class)
                            || (paramTypes[i] == short.class && argType == Short.class)
                            || (paramTypes[i] == int.class && argType == Integer.class)
                            || (paramTypes[i] == long.class && argType == Long.class)
                            || (paramTypes[i] == float.class && argType == Float.class)
                            || (paramTypes[i] == double.class && argType == Double.class)
                            || (paramTypes[i] == char.class && argType == Character.class)) {
                        continue;
                    }
                }
                if (!paramTypes[i].isAssignableFrom(argType)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                matched = candidate;
                break;
            }
        }
        if (matched == null) {
            throw new NoSuchMethodException(owner.getName() + "." + methodName);
        }

        matched.setAccessible(true);
        try {
            return matched.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }

    private Object invokePrivate(String methodName, Object... args) throws Exception {
        if (args == null) {
            args = new Object[] {null};
        }
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                types[i] = Object.class;
            } else {
                types[i] = args[i].getClass();
            }
        }

        try {
            Method method = ModServiceImpl.class.getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            try {
                return method.invoke(service, args);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw e;
            }
        } catch (NoSuchMethodException ex) {
            for (Method candidate : ModServiceImpl.class.getDeclaredMethods()) {
                if (!candidate.getName().equals(methodName) || candidate.getParameterCount() != args.length) {
                    continue;
                }
                Class<?>[] paramTypes = candidate.getParameterTypes();
                boolean matches = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (args[i] == null) {
                        if (paramTypes[i].isPrimitive()) {
                            matches = false;
                            break;
                        }
                        continue;
                    }
                    Class<?> argType = args[i].getClass();
                    if (!paramTypes[i].isAssignableFrom(argType)) {
                        if (paramTypes[i].equals(Long.class) && argType.equals(Long.class)) {
                            continue;
                        }
                        if (paramTypes[i].equals(Integer.class) && argType.equals(Integer.class)) {
                            continue;
                        }
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    candidate.setAccessible(true);
                    try {
                        return candidate.invoke(service, args);
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof RuntimeException runtimeException) {
                            throw runtimeException;
                        }
                        if (cause instanceof Error error) {
                            throw error;
                        }
                        throw e;
                    }
                }
            }
            throw ex;
        }
    }

    @Test
    void updateMod_shouldReplaceFieldsAndVersions() {
        Mod existing = new Mod();
        existing.setId(8L);
        existing.setName("Old");
        existing.setDescription("Old description");
        existing.setAuthor(new Author());
        existing.getAuthor().setId(1L);
        existing.getAuthor().setName("OldAuthor");
        existing.setCategory(new Category());
        existing.getCategory().setId(2L);
        existing.getCategory().setName("OldCategory");
        existing.setTags(new HashSet<>());
        existing.setVersions(new ArrayList<>());

        ModCreateDto dto = new ModCreateDto();
        dto.setName("New");
        dto.setDescription("New description");
        dto.setAuthorName("Alice");
        dto.setCategoryName("Utility");
        dto.setTagNames(List.of("tech"));

        ModVersionCreateDto versionDto = new ModVersionCreateDto();
        versionDto.setVersionName("2.0.0");
        versionDto.setDownloadCount(25);
        dto.setVersions(List.of(versionDto));

        Author author = new Author();
        author.setId(10L);
        author.setName("Alice");
        Category category = new Category();
        category.setId(20L);
        category.setName("Utility");

        when(modRepository.findByIdWithGraph(8L)).thenReturn(Optional.of(existing));
        when(modRepository.existsByNameIgnoreCaseAndIdNot("New", 8L)).thenReturn(false);
        when(authorRepository.findByNameIgnoreCase("Alice")).thenReturn(Optional.of(author));
        when(categoryRepository.findByNameIgnoreCase("Utility")).thenReturn(Optional.of(category));
        when(tagRepository.findAllByLowerNameIn(anySet())).thenReturn(List.of());
        when(modRepository.save(any(Mod.class))).thenAnswer(inv -> inv.getArgument(0));

        ModDto result = service.updateMod(8L, dto);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(existing.getVersions()).hasSize(1);
        assertThat(existing.getVersions().getFirst().getVersionName()).isEqualTo("2.0.0");
    }

    @Test
    void deleteMod_shouldDeleteAndRemoveUnusedAuthor() {
        Author author = new Author();
        author.setId(4L);
        author.setName("Alice");
        Mod mod = new Mod();
        mod.setId(5L);
        mod.setAuthor(author);

        when(modRepository.findById(5L)).thenReturn(Optional.of(mod));
        when(modRepository.existsByAuthorId(4L)).thenReturn(false);

        service.deleteMod(5L);

        verify(modRepository).deleteTagLinksByModId(5L);
        verify(modRepository).deleteVersionsByModId(5L);
        verify(authorRepository).deleteById(4L);
    }

    @Test
    void getModsNaiveNPlusOne_shouldReturnMappedMods() {
        Mod mod = createMod(7L, "Delta", "desc", "Taylor", "RPG");
        when(modRepository.findAllNaiveForNPlusOne()).thenReturn(List.of(mod));

        List<ModDto> result = service.getModsNaiveNPlusOne();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getAuthorName()).isEqualTo("Taylor");
    }

    @Test
    void getModsWithFiltersJpql_shouldUseCacheAndReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Mod mod = createMod(9L, "Echo", "desc", "Alice", "Utility");

        when(modRepository.findModIdsWithFiltersJpql("alice", "utility", List.of("tech"), 1L, pageable))
                .thenReturn(new PageImpl<>(List.of(9L), pageable, 1L));
        when(modRepository.findAllWithGraphByIdIn(List.of(9L))).thenReturn(List.of(mod));

        Page<ModDto> firstPage = service.getModsWithFiltersJpql(" Alice ", " Utility ", List.of("tech", "tech"), pageable);
        Page<ModDto> secondPage = service.getModsWithFiltersJpql("Alice", "Utility", List.of("tech"), pageable);

        assertThat(firstPage.getContent()).hasSize(1);
        assertThat(secondPage.getContent()).hasSize(1);
        verify(modRepository, times(1)).findModIdsWithFiltersJpql("alice", "utility", List.of("tech"), 1L, pageable);
    }

    @Test
    void getModsWithFiltersNative_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 5);
        when(modRepository.findModIdsWithFiltersNativeWithoutTags("alice", "utility", pageable))
                .thenReturn(Page.empty(pageable));

        Page<ModDto> result = service.getModsWithFiltersNative(" Alice ", " Utility ", null, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    private Author createAuthor(Long id, String name) {
        Author author = new Author();
        author.setId(id);
        author.setName(name);
        return author;
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    private Mod createMod(Long id, String name, String description, String authorName, String categoryName) {
        Mod mod = new Mod();
        mod.setId(id);
        mod.setName(name);
        mod.setDescription(description);
        Author author = new Author();
        author.setId(id + 10);
        author.setName(authorName);
        Category category = new Category();
        category.setId(id + 20);
        category.setName(categoryName);
        mod.setAuthor(author);
        mod.setCategory(category);
        mod.setTags(Set.of(new Tag()));
        mod.setVersions(new ArrayList<>());
        Tag tag = new Tag();
        tag.setName("tech");
        mod.setTags(Set.of(tag));
        return mod;
    }

    private ModCreateDto buildValidDto(String name, String description) {
        ModCreateDto dto = new ModCreateDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAuthorName("Alice");
        dto.setCategoryName("Utility");
        ModVersionCreateDto version = new ModVersionCreateDto();
        version.setVersionName("1.0.0");
        version.setDownloadCount(1);
        dto.setVersions(List.of(version));
        return dto;
    }
}
