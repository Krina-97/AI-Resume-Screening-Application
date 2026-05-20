package com.airesume.screening.service;

import com.airesume.screening.dto.LocationDto;
import com.airesume.screening.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional(readOnly = true)
    public List<LocationDto> listActive() {
        return locationRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(l -> LocationDto.builder()
                        .id(l.getId())
                        .name(l.getName())
                        .build())
                .toList();
    }
}
