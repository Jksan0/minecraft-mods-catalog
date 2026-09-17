package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.ModVersionDto;
import com.example.minecraftmodscatalog.dto.ModVersionUpsertDto;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.ModVersion;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.ModVersionRepository;
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
class ModVersionServiceImplTest {
    @Mock
    private ModVersionRepository modVersionRepository;

    @Mock
    private ModRepository modRepository;

    @InjectMocks
    private ModVersionServiceImpl service;

    @Test
    void getAllVersions_shouldReturnAllWhenModIdIsNull() {
        ModVersion version = new ModVersion();
        version.setId(1L);
        version.setVersionName("1.0.0");
        version.setDownloadCount(10);
        when(modVersionRepository.findAll()).thenReturn(List.of(version));

        List<ModVersionDto> result = service.getAllVersions(null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getVersionName()).isEqualTo("1.0.0");
    }

    @Test
    void getAllVersions_shouldReturnModVersionsForModId() {
        ModVersion version = new ModVersion();
        version.setId(2L);
        version.setVersionName("2.0.0");
        version.setDownloadCount(25);
        when(modVersionRepository.findByModId(7L)).thenReturn(List.of(version));

        List<ModVersionDto> result = service.getAllVersions(7L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDownloadCount()).isEqualTo(25);
    }

    @Test
    void getVersionById_shouldReturnVersion() {
        ModVersion version = new ModVersion();
        version.setId(3L);
        version.setVersionName("3.0.0");
        version.setDownloadCount(30);
        when(modVersionRepository.findById(3L)).thenReturn(Optional.of(version));

        ModVersionDto result = service.getVersionById(3L);

        assertThat(result.getVersionName()).isEqualTo("3.0.0");
    }

    @Test
    void getVersionById_shouldThrowWhenMissing() {
        when(modVersionRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVersionById(4L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Mod version not found: 4");
    }

    @Test
    void createVersion_shouldSaveWhenModExists() {
        ModVersionUpsertDto dto = new ModVersionUpsertDto();
        dto.setModId(7L);
        dto.setVersionName("  1.2.3  ");
        dto.setDownloadCount(150);

        Mod mod = new Mod();
        mod.setId(7L);
        mod.setName("Alpha");

        ModVersion saved = new ModVersion();
        saved.setId(11L);
        saved.setVersionName("1.2.3");
        saved.setDownloadCount(150);
        saved.setMod(mod);

        when(modRepository.findById(7L)).thenReturn(Optional.of(mod));
        when(modVersionRepository.save(any(ModVersion.class))).thenReturn(saved);

        ModVersionDto result = service.createVersion(dto);

        assertThat(result.getVersionName()).isEqualTo("1.2.3");
        assertThat(result.getDownloadCount()).isEqualTo(150);
    }

    @Test
    void createVersion_shouldValidateRequest() {
        assertThatThrownBy(() -> service.createVersion(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body is required");

        ModVersionUpsertDto emptyName = new ModVersionUpsertDto();
        emptyName.setModId(1L);
        emptyName.setVersionName(" ");
        assertThatThrownBy(() -> service.createVersion(emptyName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Version name is required");

        ModVersionUpsertDto missingModId = new ModVersionUpsertDto();
        missingModId.setVersionName("1.0");
        assertThatThrownBy(() -> service.createVersion(missingModId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("modId is required");

        ModVersionUpsertDto negativeDownload = new ModVersionUpsertDto();
        negativeDownload.setModId(1L);
        negativeDownload.setVersionName("1.0");
        negativeDownload.setDownloadCount(-1);
        assertThatThrownBy(() -> service.createVersion(negativeDownload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void updateVersion_shouldUpdateExistingVersion() {
        ModVersionUpsertDto dto = new ModVersionUpsertDto();
        dto.setModId(8L);
        dto.setVersionName("  2.0.0  ");
        dto.setDownloadCount(99);

        Mod currentMod = new Mod();
        currentMod.setId(7L);
        Mod targetMod = new Mod();
        targetMod.setId(8L);

        ModVersion version = new ModVersion();
        version.setId(5L);
        version.setVersionName("1.0.0");
        version.setDownloadCount(10);
        version.setMod(currentMod);

        when(modVersionRepository.findById(5L)).thenReturn(Optional.of(version));
        when(modRepository.findById(8L)).thenReturn(Optional.of(targetMod));
        when(modVersionRepository.countByModId(7L)).thenReturn(2L);
        when(modVersionRepository.save(any(ModVersion.class))).thenReturn(version);

        ModVersionDto result = service.updateVersion(5L, dto);

        assertThat(result.getVersionName()).isEqualTo("2.0.0");
        assertThat(result.getDownloadCount()).isEqualTo(99);
    }

    @Test
    void updateVersion_shouldRejectMovingLastVersionAway() {
        ModVersionUpsertDto dto = new ModVersionUpsertDto();
        dto.setModId(12L);
        dto.setVersionName("2.0.0");
        dto.setDownloadCount(1);

        Mod currentMod = new Mod();
        currentMod.setId(11L);
        Mod targetMod = new Mod();
        targetMod.setId(12L);

        ModVersion version = new ModVersion();
        version.setId(6L);
        version.setVersionName("1.0.0");
        version.setMod(currentMod);

        when(modVersionRepository.findById(6L)).thenReturn(Optional.of(version));
        when(modRepository.findById(12L)).thenReturn(Optional.of(targetMod));
        when(modVersionRepository.countByModId(11L)).thenReturn(1L);

        assertThatThrownBy(() -> service.updateVersion(6L, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete the last version");
    }

    @Test
    void deleteVersion_shouldDeleteWhenMoreThanOneExists() {
        Mod mod = new Mod();
        mod.setId(77L);
        ModVersion version = new ModVersion();
        version.setId(9L);
        version.setMod(mod);

        when(modVersionRepository.findById(9L)).thenReturn(Optional.of(version));
        when(modVersionRepository.countByModId(77L)).thenReturn(2L);

        service.deleteVersion(9L);

        verify(modVersionRepository).deleteById(9L);
    }

    @Test
    void deleteVersion_shouldThrowWhenOnlyOneVersionExists() {
        ModVersion version = new ModVersion();
        Mod mod = new Mod();
        mod.setId(12L);
        version.setMod(mod);

        when(modVersionRepository.findById(2L)).thenReturn(Optional.of(version));
        when(modVersionRepository.countByModId(12L)).thenReturn(1L);

        assertThatThrownBy(() -> service.deleteVersion(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("last version");
    }

    @Test
    void syntheticLambdas_shouldBeInvoked() throws Exception {
        ModVersionUpsertDto dto = new ModVersionUpsertDto();
        dto.setModId(7L);
        dto.setVersionName("1.2.3");
        dto.setDownloadCount(5);

        assertThat(invokeDeclared(ModVersionServiceImpl.class, null, "lambda$getVersionById$0", 3L))
                .isInstanceOf(EntityNotFoundException.class);
        assertThat(invokeDeclared(ModVersionServiceImpl.class, null, "lambda$createVersion$1", dto))
                .isInstanceOf(EntityNotFoundException.class);
        assertThat(invokeDeclared(ModVersionServiceImpl.class, null, "lambda$updateVersion$2", 5L))
                .isInstanceOf(EntityNotFoundException.class);
        assertThat(invokeDeclared(ModVersionServiceImpl.class, null, "lambda$updateVersion$3", dto))
                .isInstanceOf(EntityNotFoundException.class);
        assertThat(invokeDeclared(ModVersionServiceImpl.class, null, "lambda$deleteVersion$4", 9L))
                .isInstanceOf(EntityNotFoundException.class);
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
