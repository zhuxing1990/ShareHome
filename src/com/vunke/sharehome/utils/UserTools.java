/**
 * @(#)UserTools.java 2011-12-12
 *
 * Copyright 2009 LINKAGE, Inc. All rights reserved.
 * LINKAGE PROPRIETARY/CONFIDtheENTIAL. Use is subject to license terms.
 */
package com.vunke.sharehome.utils;

import java.util.Random;


/**
 * 用户账号处理类
 * 
 * @author yangcheng 65545
 * @date 2011-12-12
 * @version 1.0.0
 * @since 1.0
 */
public class UserTools {

    /*
     * userId、userToken 解密
     */
    public static String getOriginalInfo(String key, String userId) {
        // 判断key和userId是否为空
        if (key == null) {
            return null;
        }

        if (userId == null) {
            return null;
        }

        // 解密的密钥型如key1:key2，以 : 分割
        if (!key.contains(":")) {
            return null;
        }

        // 获取解码后的用户账号
        String result = "";

        String[] keys = null;
        try {
            keys = key.split(":");

            // 在这里请注意将keys[0]、keys[1]转换为int型！
            char[] use = userId.toCharArray();

            for (int i = 0; i < userId.length(); i++) {
                if ((i + 1) % 2 == 1) {
                    use[i] = (char) (use[i] + Integer.parseInt(keys[1]));
                } else {
                    use[i] = (char) (use[i] - Integer.parseInt(keys[1]));
                }
            }

            for (int i = 0; i < use.length; i++) {
                result += use[i];
            }
        } catch (Exception e) {
            // 异常处理.
        }

        String m1 = result.substring(0,
                result.length() - Integer.parseInt(keys[0]));

        String m2 = result.substring(
                result.length() - Integer.parseInt(keys[0]), result.length());

        int s = 0, e = m1.length() - 1;

        char[] us = m1.toCharArray();

        while (s < e) {
            char temp = us[e];
            us[e] = us[s];
            us[s] = temp;

            s++;
            e--;
        }

        m1 = "";
        for (int i = 0; i < us.length; i++)
            m1 += us[i];

        result = m2 + m1;

        return result;
    }

    /**
     * 
     * @description 获取加密key
     * @author Sunlei
     * @date 2012-7-10
     * @version 1.0.0
     * @history1:@author;@date;@description
     * @history2:@author;@date;@description
     * @param userID
     * @return
     */
    public static String getKey(String userID) {
        java.util.Random ram = new java.util.Random();
        int key = ram.nextInt(userID.length());
        return key + ":2";
    }

    /**
     * 
     * @description 账号加密
     * @author Sunlei
     * @date 2012-7-10
     * @version 1.0.0
     * @history1:@author;@date;@description
     * @history2:@author;@date;@description
     * @param userId
     * @param key
     * @return
     */
    public static String encodeInfo(String userId, String key) {
        // 判断key和userId是否为空
        if (key == null) {
            return null;
        }

        if (userId == null) {
            return null;
        }

        // 解密的密钥型如key1:key2，以 : 分割
        if (!key.contains(":")) {
            return null;
        }

        String[] keys = key.split(":");
        int key1 = Integer.parseInt(keys[0]);
        int key2 = Integer.parseInt(keys[1]);

        String u1 = userId.substring(0, key1);
        String u2 = userId.substring(key1, userId.length());
        int start = 0, end = u2.length() - 1;
        char[] nu2 = u2.toCharArray();
        while (start < end) {
            char temp = nu2[end];
            nu2[end] = nu2[start];
            nu2[start] = temp;
            start++;
            end--;
        }
        u2 = "";
        for (int i = 0; i < nu2.length; i++)
            u2 += nu2[i];
        userId = u2 + u1;
        char[] use = userId.toCharArray();
        for (int i = 0; i < userId.length(); i++) {
            if ((i + 1) % 2 == 1)
                use[i] = (char) (use[i] - key2);
            else
                use[i] = (char) (use[i] + key2);
        }
        userId = "";
        for (int i = 0; i < use.length; i++)
            userId += use[i];
        return userId;
    }

    /**
     * 获取TransactionID,,length要大于pixStr的长度+20
     * 
     * @return
     */
    public static String getTransactionID(String pixStr, int length) {
    	if (length<=pixStr.length()+20) {
			return new Random().nextInt(length)+"";
		}
       /* if (length < 20) {
            return Tools.getRandomNum(length);
        }*/
    		long currentTimeMillis = System.currentTimeMillis();
    		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMMddHHmmss"); //格式化获取年月日
    		String mydate = format.format(currentTimeMillis);
//    		java.util.Random(); 
//        String mydate = Tools.getDate("yyyyMMddHHmmss");
        int tmp = length - mydate.length();
        int pixsize = 0;
        if (pixStr != null) {
            pixsize = pixStr.length();
        }
        tmp = tmp - pixsize;

        String ret = "";
        String tmpStr = "0123456789";
        for (int i = 0; i < tmp; i++) {
            ret += tmpStr.charAt(((int) (Math.random() * tmpStr.length())));
        }

        return pixStr + mydate + ret;

    }

    public static void main(String args[]) {
        String userID = "0551ht";
        String key = UserTools.getKey(userID);
        String jiamiUserID = UserTools.encodeInfo(userID, key);
        WorkLog.a(jiamiUserID);
        
        WorkLog.a(UserTools.getOriginalInfo(key,
                jiamiUserID));
    }
}
