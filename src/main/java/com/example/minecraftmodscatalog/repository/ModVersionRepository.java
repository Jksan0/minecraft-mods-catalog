package com.example.minecraftmodscatalog.repository;

import com.example.minecraftmodscatalog.entity.ModVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModVersionRepository extends JpaRepository<ModVersion, Long> {
}
