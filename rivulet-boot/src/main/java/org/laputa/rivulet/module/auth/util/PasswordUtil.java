package org.laputa.rivulet.module.auth.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import org.laputa.rivulet.common.model.Result;

/**
 * @author JQH
 * @since 下午 5:50 22/04/04
 */
public class PasswordUtil {
    private final static String saltRandomBase = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 加的盐的长度为32位
     */
    private final static int saltLength = 32;
    /**
     * 数据库中存储的密码长度均为64位
     */
    private final static int passwordLength = 64;

    /**
     * 向原始密码加随机盐后重新MD5，两次encode结果不相同
     * @param rawPassword
     * @return
     */
    public static String encode(String rawPassword) {
        String salt = RandomUtil.randomString(saltRandomBase, saltLength);
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        String md5Result = md5.digestHex(rawPassword + salt);
        return md5Result + salt;
    }

    public static boolean verify(String rawPassword, String encodedPassword) {
        if (encodedPassword.length() != passwordLength) {
            throw Result.fail("EncodedPasswordLengthWrong", "密码存储异常，请联系管理员").toException();
        }
        String oriMd5Result = encodedPassword.substring(0, 32);
        String salt = encodedPassword.substring(32, 64);
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        String md5Result = md5.digestHex(rawPassword + salt);
        return oriMd5Result.equals(md5Result);
    }
}
