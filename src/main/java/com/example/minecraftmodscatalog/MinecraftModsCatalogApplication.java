package com.example.minecraftmodscatalog;

import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.repository.ModRepository;
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
    public CommandLineRunner initData(ModRepository repository) {
        return args -> {
            // Создаём несколько модов (id = null, т.к. будет сгенерирован репозиторием)
            Mod mod1 = new Mod(null, "OptiFine", "Улучшает графику и производительность", "sp614x", "1.19.2", 100000);
            Mod mod2 = new Mod(null, "Just Enough Items (JEI)", "Просмотр рецептов и предметов", "mezz", "1.19.2", 50000);
            Mod mod3 = new Mod(null, "Create", "Механизмы и автоматизация", "simibubi", "0.5.1", 75000);

            // Сохраняем их в репозиторий
            repository.save(mod1);
            repository.save(mod2);
            repository.save(mod3);

            // Выводим в лог, что данные добавлены (для проверки)
            System.out.println(">>> Тестовые моды успешно добавлены в репозиторий!");
        };
    }
}