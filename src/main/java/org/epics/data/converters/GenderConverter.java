package org.epics.data.converters;

import org.epics.data.enums.Gender;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, String> {

    @Override
    public String convertToDatabaseColumn(Gender gender) {
        return gender.toString();
    }

    @Override
    public Gender convertToEntityAttribute(String s) {
        return Gender.fromString(s);
    }

}
