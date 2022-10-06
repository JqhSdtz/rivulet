package liquibase.ext.hibernate;

/**
 * @author JQH
 * @since 下午 8:05 22/10/06
 */
public class GlobalSetting {
    private static boolean showFoundInfo = true;

    public static boolean isShowFoundInfo() {
        return showFoundInfo;
    }

    public static void setShowFoundInfo(boolean showFoundInfo) {
        GlobalSetting.showFoundInfo = showFoundInfo;
    }
}
