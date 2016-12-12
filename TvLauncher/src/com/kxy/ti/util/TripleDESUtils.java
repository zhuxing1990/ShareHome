package com.kxy.ti.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TripleDESUtils {
	// 秘钥算法 algorithm
	private static final String KEY_ALGORITHM = "DESede";

	// 加密算法：algorithm/mode/padding 算法/工作模式/填充模式
	public static final String CIPHER_ALGORITHM_ECB = "DESede/ECB/PKCS5Padding";
	
	public static void initDES(String keycode,String data) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);
			SecretKey secretKey = new SecretKeySpec(build3Deskey(keycode),
					KEY_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] doFinal = cipher.doFinal(data.getBytes("ASCII"));
			String string = new String(doFinal);
			System.out.println(string);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	 // 根据字符串生成密钥24位的字节数组
    public static byte[] build3Deskey(String keyStr) throws Exception {
        byte[] key = new byte[24];
        byte[] temp = keyStr.getBytes("ASCII");
        if (key.length > temp.length) {
            System.arraycopy(temp, 0, key, 0, temp.length);
 
        } else {
            System.arraycopy(temp, 0, key, 0, key.length);
 
        }
        return key;
    }
}
