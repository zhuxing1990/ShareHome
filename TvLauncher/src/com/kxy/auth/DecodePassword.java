package com.kxy.auth;


import android.util.Base64;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public class DecodePassword
{
  private static String publicKey = "MIGiMA0GCSqGSIb3DQEBAQUAA4GQADCBjAKBhADQhJgt4Wdt93zlF+3qQlxJ1kQM\nDTNH76KFh+Fz3Nui5D+5iCv+yeW2uGMlK/JB429GAd47H16bc8oUC0ZoH67dac6l\ncS6EZ2Lkn6gOHRzwtAjKZtKuTF/zWZ4y6/+Vnu8zxmuG/WHWyQmaY6O31yO7zH4u\nAsY1+bMpp1lfMOIo0vyR3wIDAQAB";

  public static String decode(String paramString)
  {
    try
    {
      byte[] arrayOfByte = Base64.decode(paramString, 2);
      Cipher localCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      localCipher.init(2, getPublicKey());
      String str = new String(localCipher.doFinal(arrayOfByte));
      return str;
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return "";
  }

  public static PublicKey getPublicKey()
  {
    try
    {
      X509EncodedKeySpec localX509EncodedKeySpec = new X509EncodedKeySpec(Base64.decode(publicKey, 0));
      PublicKey localPublicKey = KeyFactory.getInstance("RSA").generatePublic(localX509EncodedKeySpec);
      return localPublicKey;
    }
    catch (InvalidKeySpecException localInvalidKeySpecException)
    {
      localInvalidKeySpecException.printStackTrace();
      return null;
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      while (true)
        localNoSuchAlgorithmException.printStackTrace();
    }
  }
}