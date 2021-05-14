package org.epics.data.converters;

import org.epics.data.enums.Role;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {

        if (role == null) return null;

        return role.getSlug();
    }

    @Override
    public Role convertToEntityAttribute(String slug) {

        if (slug == null) return null;

        return Stream.of(Role.values())
                .filter(role -> role.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
