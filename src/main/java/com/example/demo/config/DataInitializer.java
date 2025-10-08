package com.example.demo.config;

import com.example.demo.model.Administrator;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsById("admin@system.local")) {
                Administrator admin = new Administrator();
                admin.setEmail("admin@system.local");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setName("System Admin");
                userRepository.save(admin);
            }
        };
    }
}


