package com.vunke.sharehome.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class MD5Util {

	public static String encryptByMD5(String str, String key) {

		String encryptStr = null;
		if (str != null && key != null) {

			byte[] src = (str + key).getBytes();
			char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
					'd', 'e', 'f' };

			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
				md.update(src);
				byte tmp[] = md.digest();
				// MD5 的计算结果是一个 128 位的长整数，
				char chr[] = new char[16 * 2];
				// 每个字节用 16 进制表示的话，使用两个字符，
				int k = 0; // 表示转换结果中对应的字符位置
				for (int i = 0; i < 16; i++) {
					// 从第一个字节开始，对 MD5 的每一个字节
					// 转换成 16 进制字符的转换
					byte byte0 = tmp[i]; // 取第 i 个字节
					chr[k++] = hexDigits[byte0 >>> 4 & 0xf];
					// 取字节中高 4 位的数字转换,
					// >>> 为逻辑右移，将符号位一起右移
					chr[k++] = hexDigits[byte0 & 0xf];
					// 取字节中低 4 位的数字转换
				}
				encryptStr = new String(chr); // 换后的结果转换为字符串
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return encryptStr;
	}

	private static String desKey = "4567";

	public final static byte[] doit(byte[] bytes) {
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(bytes);

			return mdTemp.digest();
		} catch (Exception e) {
			return null;
		}
	}

	public final static String doit(String s) {
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };

		try {
			byte[] strTemp = s.getBytes("UTF-8");
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);

			// MessageDigest.getInstance(algorithm)
			byte[] md = mdTemp.digest();
			int j = md.length;
			char[] str = new char[j * 2];

			// 二进制转十六进制
			int k = 0;

			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}

			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getDesKey() {
		return desKey;
	}

	public static void setDesKey(String key) {
		MD5Util.desKey = key;
	}
	   /**
     * 对字符串加密,加密算法使用MD5,SHA-1,SHA-256,默认使用SHA-256
     *
     * @param strSrc
     *            要加密的字符串
     * @param encName
     *            加密类型
     * @return
     */
    public static String Encrypt(String strSrc, String encName) {
        MessageDigest md = null;
        String strDes = null;

        byte[] bt = strSrc.getBytes();
        try {
            if (encName == null || encName.equals("")) {
                encName = "SHA-256";
            }
            md = MessageDigest.getInstance(encName);
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }

    public static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }
	/*public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		System.out.println(MD5Util.encryptByMD5("11111111111111111111111111",
				"100127BC9F987F649C853EE84122F99644F824766E881A21"));
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
	}*/
}
