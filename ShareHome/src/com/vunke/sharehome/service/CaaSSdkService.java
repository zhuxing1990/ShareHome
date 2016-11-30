package com.vunke.sharehome.service;

import android.graphics.Rect;

import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.log.LogApi;
import com.vunke.sharehome.Const;

public class CaaSSdkService
{ 
    private static Rect m_remoteMainRect; // 记录远端主流窗口位置和尺寸, 用于窗口的隐藏和恢复
    
    /**
     * Comment for <code>m_remoteMainLayer</code><br>
     * 远端主屏的图层
     */
    private static int m_remoteMainLayer = CallApi.VIDEO_LAYER_TOP;
         
    private static boolean m_localViewOpen = false;
    
    public static boolean closeLocalView()
    {
        LogApi.d(Const.TAG_CAAS, "closeLocalView Enter");
        
        if (!m_localViewOpen)
        {
            LogApi.d(Const.TAG_CAAS, "LocalView not opened");
            return true;
        }
        
        int iResult = CallApi.closeLocalView();
        m_localViewOpen = false;
        
        LogApi.d(Const.TAG_CAAS, "Leave closeCamera result is " + iResult);
        
        return 0 == iResult;
    }

    public static boolean isCameraOpen()
    {
        return m_localViewOpen;
    }
  
    public static boolean openLocalView()
    {
        
        if (m_localViewOpen)
        {
            LogApi.d(Const.TAG_CAAS, "openLocalView Camera already opened");
            return true;
        }
        
        m_localViewOpen = true;

        int iResult = -1;
        iResult =  CallApi.openLocalView();
        if (0 != iResult)
        {
            m_localViewOpen = false;
        }
        
        LogApi.d(Const.TAG_CAAS, "openLocalView Leave openCamera result is " + iResult);
        return m_localViewOpen;
    }
         
    public static void setLocalCamaraStatus(boolean status)
    {
        LogApi.d(Const.TAG_CAAS, "Enter setLocalCamaraStatus status:" + status + " old status:" + m_localViewOpen);
        m_localViewOpen = status;
    }
    
    public static void setLocalRenderPos(Rect rectLocal, int layer)
    {
        if (rectLocal != null)
        {
           LogApi.d(Const.TAG_CAAS,
                    "Enter setLocalRenderPos layer:" + layer + "rectLocal.width():" + rectLocal.width()
                            + "rectLocal.height()" + rectLocal.height() + "rectLocal.left" + rectLocal.left
                            + "rectLocal.top" + rectLocal.top);
            if (rectLocal.width() > 0 && rectLocal.height() > 0)
            {
                CallApi.setRegion(CallApi.VIDEO_TYPE_LOCAL, rectLocal.left, rectLocal.top, rectLocal.width(), rectLocal.height(), layer);
            }
        }
    }
        
    public static void setRemoteRenderPos(Rect rectRemote, int layer)
    {
        LogApi.d(Const.TAG_CAAS, "Enter setRemoteRenderPos layer:" + layer);
        if (rectRemote != null && rectRemote.width() > 0 && rectRemote.height() > 0)
        {
            CallApi.setRegion(CallApi.VIDEO_TYPE_REMOTE, rectRemote.left, rectRemote.top, rectRemote.width(), rectRemote.height(), layer);
            m_remoteMainRect = rectRemote;
            m_remoteMainLayer = layer;
        }
    }
       
    public static void setVideoLevel(int video_level_id)
    {
        LogApi.d(Const.TAG_CAAS, "Enter setVideoLevel: " + video_level_id);
      
        //在同一个profile level(如16，即VGA/4CIF)情况下分辨率可选情况下，设置优选4:3，与手机保持一致
        CallApi.setConfig(CallApi.CONFIG_MAJOR_TYPE_VIDEO_PREFER_SIZE, CallApi.CONFIG_MINOR_TYPE_DEFAULT, "2");
        
        switch (video_level_id)
        {
            case 0:
                CallApi.setVideoLevel(CallApi.VIDEO_LEVEL_720P_NOMAL);
                break;
            case 1:
                CallApi.setVideoLevel(CallApi.VIDEO_LEVEL_VGA_HIGH);
                break;
            case 2:
                CallApi.setVideoLevel(CallApi.VIDEO_LEVEL_VGA_NOMAL);
                break;
            default:
                LogApi.e(Const.TAG_CAAS, "Invalid video level!");
                break;
        }
        
        LogApi.d(Const.TAG_CAAS, "Leave setVideoLevel");
    }
    

    public static void setVoiceDelay(int audioDelay)
    {
            if (audioDelay == Const.AUDIO_DELAY_INITIAL_VALUE)
            {
                CallApi.setVoiceDelay(0);
            }
            else
            {
                CallApi.setVoiceDelay(audioDelay);
            }
            
    }
      
    public static void showLocalVideoRender(boolean show)
    {
        if (m_localViewOpen)
        {
            CallApi.setVisible(CallApi.VIDEO_TYPE_LOCAL, show);
        }
        else
        {
            LogApi.e(Const.TAG_CAAS, "showLocalVideoRender failed, m_localRenderOpened is null");
        }
    }
    
    public static void showRemoteVideoRender(boolean show)
    {
       CallApi.setVisible(CallApi.VIDEO_TYPE_REMOTE, show);        
       return;
    }
    
}
