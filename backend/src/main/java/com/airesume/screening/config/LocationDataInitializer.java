package com.airesume.screening.config;

import com.airesume.screening.entity.Location;
import com.airesume.screening.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class LocationDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(LocationDataInitializer.class);

    private static final List<String> DEFAULT_LOCATIONS = List.of(
            "Mumbai",
            "Bangalore",
            "Delhi",
            "Hyderabad",
            "Chennai"
    );

    @Bean
    @Order(0)
    public CommandLineRunner seedLocations(LocationRepository locationRepository) {
        return args -> {
            if (locationRepository.count() > 0) {
                return;
            }
            DEFAULT_LOCATIONS.forEach(name -> locationRepository.save(
                    Location.builder().name(name).active(true).build()));
            log.info("Seeded {} locations", DEFAULT_LOCATIONS.size());
        };
    }
}
