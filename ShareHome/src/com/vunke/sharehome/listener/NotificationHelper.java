package com.vunke.sharehome.listener;

/*
 * Copyright (C) 2013 Juwend's Demo
 *
 * 本代码可以任意复制与改动，欢迎转载，转载请注明出处
 * 
 *      http://www.juwends.com/
 * 
 */
 
 
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
 
/**
 * 通知制造
 * 
 * @author Juwend
 * 
 */
public class NotificationHelper {
    // 1.实例化Notification类
    // 2.设置Notification对象的icon，通知文字，声音
    // 3.实例化PendingIntent类，作为控制点击通知后显示内容的对象
    // 4.加载PendingIntent对象到Notification对象（设置 打开通知抽屉后的 标题/内容）
    // 5.获得 NotificationManager对象
    // 6.使用NotificationManager对象显示通知
     
    /**
     * 发布通知
     * 
     * @param c             上下文
     * @param notifyId      通知标识id
     * @param iconResId     显示的icon的id
     * @param textResId     显示的文字的id
     * @param soundResId    声音 - 没有使用（可以自己加）
     * @param titleResId    打开通知抽屉后的标题的id
     * @param contentResId  打开通知抽屉后的内容的id
     * @param cls           点击后打开的类
     * @param flag          通知标签
     * @return              返回Notification对象
     */
    static public Notification notify(Context c, int notifyId, int iconResId,
            int textResId, int soundResId, int titleResId, int contentResId,
            Class<?> cls, int flag) {
        final Resources res = ((Activity) c).getResources();
 
        return notify(c, notifyId, iconResId, res.getString(textResId), soundResId,
                res.getString(titleResId), res.getString(contentResId), cls,
                flag);
    }
 
    /**
     * 发布通知
     * 
     * @param c                 上下文
     * @param notifyId          通知标识id
     * @param iconResId         显示的icon的id
     * @param notifyShowText    显示的文字
     * @param soundResId        声音 - 没有使用（可以自己加）
     * @param titleText         打开通知抽屉后的标题
     * @param contentText       打开通知抽屉后的内容
     * @param cls               点击后打开的类
     * @param flag              通知标签
     * @return                  返回Notification对象
     */
    static public Notification notify(Context c, int notifyId, int iconResId,
            String notifyShowText, int soundResId, String titleText,
            String contentText, Class<?> cls, int flag) {
 
        Notification n = genNotification(c, notifyId, iconResId, notifyShowText, 
                soundResId, titleText, contentText, cls, flag);
         
        // 显示通知
        notify(c, notifyId, n);
         
        return n;
    }
     
    /**
     * 发布通知
     * 
     * @param c         上下文
     * @param notifyId  通知标识id
     * @param n         通知对象
     */
    static public void notify(Context c, int notifyId, Notification n) {
        final NotificationManager nm = (NotificationManager) c
                .getSystemService(Context.NOTIFICATION_SERVICE);
         
        // 显示通知
        nm.notify(notifyId, n);
    }
     
    /**
     * 生成Notification对象
     * 
     * @param c             上下文
     * @param notifyId      通知标识id
     * @param iconResId     显示的icon的id
     * @param textResId     显示的文字的id
     * @param soundResId    声音 - 没有使用（可以自己加）
     * @param titleResId    打开通知抽屉后的标题的id
     * @param contentResId  打开通知抽屉后的内容的id
     * @param cls           点击后打开的类
     * @param flag          通知标签
     * @return              返回Notification对象
     */
    static public Notification genNotification(Context c, int notifyId, int iconResId,
            int textResId, int soundResId, int titleResId, int contentResId,
            Class<?> cls, int flag) {
        final Resources res = ((Activity) c).getResources();
 
        return genNotification(c, notifyId, iconResId, res.getString(textResId), soundResId,
                res.getString(titleResId), res.getString(contentResId), cls,
                flag);
    }
     
    /**
     * 生成Notification对象
     * 
     * @param c                 上下文
     * @param notifyId          通知标识id
     * @param iconResId         显示的icon的id
     * @param notifyShowText    显示的文字
     * @param soundResId        声音 - 没有使用（可以自己加）
     * @param titleText         打开通知抽屉后的标题
     * @param contentText       打开通知抽屉后的内容
     * @param cls               点击后打开的类
     * @param flag              通知标签
     * @return                  返回Notification对象
     */
    static public Notification genNotification(Context c, int notifyId, int iconResId,
            String notifyShowText, int soundResId, String titleText,
            String contentText, Class<?> cls, int flag) {
         
        Intent intent = null;
        if (cls != null)
            intent = new Intent(c, cls);
         
        final Notification n = new Notification();
 
        // 控制点击通知后显示内容的类
        final PendingIntent ip = PendingIntent.getActivity(c, 
                0,  // requestCode  现在是没有使用的，所以任意值都可以
                intent, 
                0   // PendingIntent的flag，在update这个通知的时候可以加特别的flag
                );
        // 设置通知图标
        n.icon = iconResId;
        // 通知文字
        n.tickerText = notifyShowText;
        // 通知发出的标志设置
        n.flags = flag;
        // 设置通知参数
        n.setLatestEventInfo(c, titleText, contentText, ip);
         
        return n;
    }
 
    /**
     * 取消消息
     * 
     * @param c
     * @param notifyId
     * @return void
     */
    public static void cancel(Context c, int notifyId) {
        ((NotificationManager) ((Activity) c)
                .getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(notifyId);
    }
 
    // flags
    final static public int FLAG_ONGOING_EVENT_AUTO_CANCEL = Notification.FLAG_AUTO_CANCEL
            | Notification.FLAG_ONGOING_EVENT;
    final static public int FLAG_ONGOING_EVENT = Notification.FLAG_ONGOING_EVENT;
    final static public int FLAG_NO_CLEAR = Notification.FLAG_NO_CLEAR;
    final static public int FLAG_AUTO_CANCEL = Notification.FLAG_AUTO_CANCEL;
}
