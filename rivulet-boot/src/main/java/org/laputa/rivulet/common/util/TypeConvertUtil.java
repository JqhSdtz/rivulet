package org.laputa.rivulet.common.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.lang.TypeReference;
import liquibase.ext.hibernate.DatabaseObjectAttrName;

import java.util.List;
import java.util.stream.Stream;

public class TypeConvertUtil {
    public static <T> T convert(TypeReference<T> reference, Object value) throws ConvertException {
        return Convert.convert(reference, value);
    }

    /**
     * 从stream转换到list，避免出现类型不匹配的问题
     */
    public static <T> List<T> streamToList(Stream<?> stream) {
        Stream<T> listStream = convert(new TypeReference<>() {
        }, stream);
        return listStream.toList();
    }
}
