package com.example.minecraftmodscatalog;

import com.example.minecraftmodscatalog.entity.Author;
import com.example.minecraftmodscatalog.entity.Category;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.ModVersion;
import com.example.minecraftmodscatalog.entity.Tag;
import com.example.minecraftmodscatalog.repository.AuthorRepository;
import com.example.minecraftmodscatalog.repository.CategoryRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.TagRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MinecraftModsCatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinecraftModsCatalogApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(
            final ModRepository modRepository,
            final AuthorRepository authorRepository,
            final CategoryRepository categoryRepository,
            final TagRepository tagRepository
    ) {
        return args -> {
            if (modRepository.count() > 0) {
                return;
            }

            Author sp614x = new Author();
            sp614x.setName("sp614x");
            sp614x = authorRepository.save(sp614x);

            Author mezz = new Author();
            mezz.setName("mezz");
            mezz = authorRepository.save(mezz);

            Category optimization = new Category();
            optimization.setName("Optimization");
            optimization = categoryRepository.save(optimization);

            Category ui = new Category();
            ui.setName("UI");
            ui = categoryRepository.save(ui);

            Tag popular = new Tag();
            popular.setName("Popular");
            popular = tagRepository.save(popular);

            Tag fabric = new Tag();
            fabric.setName("Fabric");
            fabric = tagRepository.save(fabric);

            Mod optifine = new Mod();
            optifine.setName("OptiFine");
            optifine.setDescription("Improves graphics and performance");
            optifine.setAuthor(sp614x);
            optifine.setCategories(new HashSet<>(Set.of(optimization)));
            optifine.setTags(new HashSet<>(Set.of(popular)));
            optifine.getVersions().add(buildVersion("1.20.1", 210000, optifine));

            Mod jei = new Mod();
            jei.setName("JEI");
            jei.setDescription("Recipe and item viewer");
            jei.setAuthor(mezz);
            jei.setCategories(new HashSet<>(Set.of(ui)));
            jei.setTags(new HashSet<>(Set.of(popular, fabric)));
            jei.getVersions().add(buildVersion("1.20.1", 175000, jei));

            //modRepository.save(optifine);
            //modRepository.save(jei);
        };
    }

    private ModVersion buildVersion(final String name, final int downloads, final Mod mod) {
        ModVersion version = new ModVersion();
        version.setVersionName(name);
        version.setDownloadCount(downloads);
        version.setMod(mod);
        return version;
    }
}
