package com.proxy.common.util;

/**
 * @PACKAGE_NAME: com.proxy.common.util
 * @Description
 * @Author 周志成
 * @DATE: 2020/8/20 9:38
 * @PROJECT_NAME: proxy
 **/

public class HexUtil {
    /**
     * @Description:
     * @Author: 周志成
     * @Date: 2020/8/20 9:39
     * @param bArr:
     * @return: java.lang.String
     **/
    public static String bytesToHexString(byte[] bArr) {
        StringBuffer sb = new StringBuffer(bArr.length);
        String sTmp;
        for (int i = 0; i < bArr.length; i++) {
            sTmp = Integer.toHexString(0xFF & bArr[i]);
            if (sTmp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTmp);
        }
        return sb.toString();
    }
    /**
     * @Description:
     * @Author: 周志成
     * @Date: 2020/8/20 9:39
     * @param bArr:
     * @return: java.lang.String
     **/
    public static String bytesToHexString2(byte[] bArr) {
        StringBuffer sb = new StringBuffer(bArr.length);
        String sTmp;
        for (int i = 0; i < bArr.length; i++) {
            sTmp = Integer.toHexString(0xFF & bArr[i]);
            if (sTmp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTmp);
            sb.append(",");
        }
        return sb.toString();
    }
    /**
     * @Description:
     * @Author: 周志成
     * @Date: 2020/8/20 9:39
     * @param hexString:
     * @return: byte[]
     **/
    public static byte[] hexStringTobyte(String hexString) {
        int len = hexString.length();
        byte[] length = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            length[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i+1), 16));
        }
        return length;
    }
}
