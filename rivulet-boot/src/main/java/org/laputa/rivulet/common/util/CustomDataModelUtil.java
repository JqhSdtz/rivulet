package org.laputa.rivulet.common.util;

public class CustomDataModelUtil {
    private static final String packageBase = "org.laputa.rivulet.common.entity.produced.";

    public static String getCustomDataModelClassName(String modelName) {
        return packageBase + modelName;
    }

    public static boolean isCustomDataModelClass(String className) {
        return className.startsWith(packageBase);
    }
}
