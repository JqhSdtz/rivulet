package liquibase.ext.hibernate;

import lombok.Getter;
import lombok.Setter;

/**
 * !!!
 * @author JQH
 * @since 下午 8:05 22/10/06
 */
public class GlobalSetting {
    @Getter
    @Setter
    private static boolean showFoundInfo = true;
    @Getter
    @Setter
    private static boolean showConvertedInfo = true;

}
