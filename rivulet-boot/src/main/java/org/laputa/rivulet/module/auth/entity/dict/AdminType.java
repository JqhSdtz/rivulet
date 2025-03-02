package org.laputa.rivulet.module.auth.entity.dict;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;

import java.util.Arrays;

/**
 * 管理员类别
 *
 * @author JQH
 * @since 下午 12:08 22/04/04
 */
public enum AdminType {
    /**
     * 0.初始管理员
     */
    INITIAL_ADMIN(0),
    /**
     * 1.普通管理员
     */
    NORMAL_ADMIN(1);

    private final Integer value;

    @JsonCreator
    AdminType(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer getValue() {
        return value;
    }

    public static class Converter implements AttributeConverter<AdminType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(AdminType attribute) {
            return attribute.getValue();
        }

        @Override
        public AdminType convertToEntityAttribute(Integer dbData) {
            return Arrays.stream(AdminType.values()).filter(adminType -> adminType.getValue().equals(dbData))
                    .findFirst().orElse(null);
        }
    }

}
