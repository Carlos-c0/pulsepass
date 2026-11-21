package com.pulsepass.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

// Configuracion comun de todos los mappers.
// unmappedTargetPolicy = ERROR hace que la compilacion falle si un campo del DTO queda sin llenar.
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PulsePassMapperConfig {
}
