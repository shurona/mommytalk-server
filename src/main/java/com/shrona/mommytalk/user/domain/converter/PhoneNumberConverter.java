package com.shrona.mommytalk.user.domain.converter;

import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import jakarta.persistence.AttributeConverter;

//@Converter(autoApply = true) // 모든 PhoneNumber 필드에 자동 적용
public class PhoneNumberConverter implements AttributeConverter<PhoneNumber, String> {

    @Override
    public String convertToDatabaseColumn(PhoneNumber phoneNumber) {
        return (phoneNumber != null) ? phoneNumber.getPhoneNumber() : null;
    }

    @Override
    public PhoneNumber convertToEntityAttribute(String dbData) {
        return (dbData != null) ? new PhoneNumber(dbData) : null;
    }
}

