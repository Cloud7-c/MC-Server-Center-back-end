package com.yunjic.mcsc.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 生成盐值和对字符串使用SHA256加密的工具类
 *
 * @author yunji
 * @createTime 2023-03-29 17:08:07
 */
public class EncryptUtil {
    /**
     * 使用SHA256进行加密
     *
     * @param password 密码
     * @param salt 盐值
     * @return 加密后长为64的字符串
     */
    public static String Encrypt(String password, String salt){
        String str = salt + password;
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            encodeStr = byteToHex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * 生成32~64位的盐值
     *
     * @return 生成的盐值
     */
    public static String getSalt(){
        SecureRandom random = new SecureRandom();

        int randomInt = random.nextInt(16) + 16;
        byte[] bytes = new byte[randomInt];
        random.nextBytes(bytes);

        //转换为16进制字符串并返回
        return byteToHex(bytes);
    }

    /**
     * sha256加密 将byte转为16进制
     *
     * @param bytes 字节码
     * @return 加密后的字符串
     */
    private static String byteToHex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuilder.append("0");
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }
}
