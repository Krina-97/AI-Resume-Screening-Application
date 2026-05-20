package com.airesume.screening.config;

import com.airesume.screening.entity.Role;
import com.airesume.screening.entity.User;
import com.airesume.screening.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("System Administrator")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            User hr = User.builder()
                    .username("hruser")
                    .email("hr@example.com")
                    .password(passwordEncoder.encode("Hr@123456"))
                    .fullName("Demo HR")
                    .role(Role.HR)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            userRepository.save(hr);
            log.info("Seeded default users: admin / Admin@123 and hruser / Hr@123456");
        };
    }
}
