package com.airesume.screening.controller;

import com.airesume.screening.dto.LocationDto;
import com.airesume.screening.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Operation(summary = "List active locations for job posting")
    public List<LocationDto> listActive() {
        return locationService.listActive();
    }
}
