package liquibase.ext.hibernate.util;

/**
 * !!!
 * @author JQH
 * @since 下午 5:40 22/10/27
 */
public class TableRemarkMetaInfoUtil {
    public final static String META_FLAG = "Rv&Sys";

    public static boolean existMetaInfo(String remarkStr) {
        if (remarkStr == null || remarkStr.length() < 7) return false;
        if (remarkStr.charAt(0) != '[') return false;
        if (!META_FLAG.equals(remarkStr.substring(1, 7))) return false;
        if (remarkStr.indexOf(']') == -1) return false;
        return true;
    }

    public static TableRemarkMetaInfo getMetaInfo(String remarkStr) {
        if (!existMetaInfo(remarkStr)) {
            return new TableRemarkMetaInfo();
        }
        int rightBracketPos = remarkStr.indexOf(']');
        String metaInfoStr = remarkStr.substring(1, rightBracketPos);
        String[] metaInfoParts = metaInfoStr.split("#");
        TableRemarkMetaInfo metaInfo = new TableRemarkMetaInfo();
        if (metaInfoParts.length > 1) {
            metaInfo.setBuiltIn("1".equals(metaInfoParts[1]));
        }
        return metaInfo;
    }

    public static String setMetaInfo(String remarkStr, TableRemarkMetaInfo metaInfo) {
        remarkStr = remarkStr == null ? "" : remarkStr;
        // 经测试，PostreSQL在保存注释的时候，会去掉末尾换行符，所以这里需要trim一下，以保证对应
        remarkStr = remarkStr.trim();
        StringBuilder metaInfoStr = new StringBuilder('[' + META_FLAG);
        metaInfoStr.append('#').append(metaInfo.isBuiltIn() ? '1' : '0');
        metaInfoStr.append("#方括号内请勿修改]");
        remarkStr = metaInfoStr + remarkStr;
        return remarkStr;
    }
}
