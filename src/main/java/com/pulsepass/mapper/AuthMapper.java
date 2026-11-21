package com.pulsepass.mapper;

import com.pulsepass.domain.Role;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.security.SecurityPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(config = PulsePassMapperConfig.class)
public interface AuthMapper {

    @Mapping(target = "id", source = "userId")
    AuthenticatedUserResponse toAuthenticatedUser(SecurityPrincipal principal);

    // MapStruct usa este metodo para pasar Set<Role> a List<String>.
    // Se ordenan para que /me siempre devuelva los roles en el mismo orden.
    default List<String> rolesToNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::name)
                .sorted()
                .toList();
    }
}
