package com.example.demo.config;

import com.example.demo.model.Administrator;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository) {
        return args -> {
            if (!userRepository.existsById("admin@system.local")) {
                Administrator admin = new Administrator();
                admin.setEmail("admin@system.local");
                admin.setPassword("admin");
                admin.setName("System Admin");
                userRepository.save(admin);
            }
        };
    }
}


