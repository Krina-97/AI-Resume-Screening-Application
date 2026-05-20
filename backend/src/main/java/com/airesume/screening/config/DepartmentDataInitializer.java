package com.airesume.screening.config;

import com.airesume.screening.entity.Department;
import com.airesume.screening.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class DepartmentDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DepartmentDataInitializer.class);

    private static final List<String> DEFAULT_DEPARTMENTS = List.of(
            "Engineering",
            "Finance & Accounting",
            "Data & Analytics",
            "Marketing",
            "Operations"
    );

    @Bean
    @Order(0)
    public CommandLineRunner seedDepartments(DepartmentRepository departmentRepository) {
        return args -> {
            if (departmentRepository.count() > 0) {
                return;
            }
            DEFAULT_DEPARTMENTS.forEach(name -> departmentRepository.save(
                    Department.builder().name(name).active(true).build()));
            log.info("Seeded {} departments", DEFAULT_DEPARTMENTS.size());
        };
    }
}
