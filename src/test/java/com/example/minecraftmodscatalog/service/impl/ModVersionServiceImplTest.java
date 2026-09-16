package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.ModVersionUpsertDto;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.ModVersion;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.ModVersionRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

        var result = service.createVersion(dto);

        assertThat(result.getVersionName()).isEqualTo("1.2.3");
        assertThat(result.getDownloadCount()).isEqualTo(150);
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
}
