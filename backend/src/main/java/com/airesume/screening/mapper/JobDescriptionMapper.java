package com.airesume.screening.mapper;

import com.airesume.screening.dto.JobDescriptionDto;
import com.airesume.screening.dto.JobDescriptionRequest;
import com.airesume.screening.entity.JobDescription;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface JobDescriptionMapper {

    JobDescriptionDto toDto(JobDescription entity);

    JobDescription toEntity(JobDescriptionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(JobDescriptionRequest request, @MappingTarget JobDescription entity);
}
