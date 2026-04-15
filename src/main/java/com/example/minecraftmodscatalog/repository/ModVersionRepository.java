package com.example.minecraftmodscatalog.repository;

import com.example.minecraftmodscatalog.entity.ModVersion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModVersionRepository extends JpaRepository<ModVersion, Long> {
    long countByModId(Long modId);

    List<ModVersion> findByModId(Long modId);
}
