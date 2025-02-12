package org.laputa.rivulet.module.auth.entity.dict;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.AttributeConverter;
import java.util.Arrays;

/**
 * 用户类别
 *
 * @author JQH
 * @since 下午 12:08 22/04/04
 */
public enum UserType {
    /**
     * 0.初始用户
     */
    INITIAL_USER(0),
    /**
     * 1.普通用户
     */
    NORMAL_USER(1);

    private Integer value;

    @JsonCreator
    UserType(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer getValue() {
        return value;
    }

    public static class Converter implements AttributeConverter<UserType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(UserType attribute) {
            return attribute.getValue();
        }

        @Override
        public UserType convertToEntityAttribute(Integer dbData) {
            return Arrays.stream(UserType.values()).filter(userType -> userType.getValue().equals(dbData))
                    .findFirst().orElse(null);
        }
    }

}
