package com.example.demo.config;

import com.example.demo.model.Administrator;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsById("admin@gmail.com")) {
                Administrator admin = new Administrator();
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("adminpass"));
                admin.setName("Admin Adminovic");
                admin.setPhoneNumber("+381641234567");
                admin.setBirthday(LocalDate.of(1990, 1, 1));
                admin.setAddress("Bulevar kralja Aleksandra 73");
                admin.setCity("Belgrade");
                admin.setCreatedAt(LocalDate.now());
                userRepository.save(admin);
            }
        };
    }
}


