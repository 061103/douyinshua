package com.ztwd.douyinshua;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

import static com.ztwd.douyinshua.AccessibilityHelper.execShellCmd;
import static com.ztwd.douyinshua.AccessibilityHelper.sleepTime;
import static com.ztwd.douyinshua.AccessibilityHelper.upgradeRootPermission;
import static com.ztwd.douyinshua.StringTimeUtils.getTimeStr2;

/**
 *  抖音广告页面的activity：com.ss.android.excitingvideo.ExcitingVideoActivity
 *  抖音主页面的activity：com.ss.android.ugc.aweme.main.MainActivity
 *  抖音视频页面计时ID：com.ss.android.ugc.aweme.lite:id/aoz
 *  来赚钱ID：com.ss.android.ugc.aweme.lite:id/ke
 *  点赞ID：com.ss.android.ugc.aweme.lite:id/a4m
 *  加关注ID:com.ss.android.ugc.aweme.lite:id/a4k
 *  整个视频页面ID：com.ss.android.ugc.aweme.lite:id/a40
 * */
public class douyinserver extends AccessibilityService {
    private final static String TAG = "douyinserver";
    private boolean msroot;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//注意这个方法回调，是在主线程，不要在这里执行耗时操作
        int eventType = event.getEventType();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                try {
                    int sys_hh = (Integer.parseInt(getTimeStr2().substring(11, 12)) * 10) + Integer.parseInt(getTimeStr2().substring(12, 13));
                    if (sys_hh > 8 && sys_hh < 23) {
                        List<AccessibilityNodeInfo> aoz = rootNode.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme.lite:id/aoz");//判断是否在视频页面
                        if (aoz != null && !aoz.isEmpty()) {
                            int ran = (int) (Math.random() * 20);//产生随机数
                            int wait_sleep = ran * 1000;
                            if (wait_sleep > 3000) {//最少停留页面3秒
                                Log.i(TAG, "延时：" + wait_sleep/1000 + "秒！");
                                sleepTime(wait_sleep);
                                Log.i(TAG, "延时完成准备滑动");
                                if(Build.VERSION.SDK_INT>=26) {
                                        Log.i(TAG, "使用触摸屏事件滑动");
                                        Path path = new Path();
                                        path.moveTo(800, 2200);
                                        path.lineTo(800, 400);
                                        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(path, 0, 200,false);
                                        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
                                        return;
                                    }else {
                                    if(msroot) {
                                        execShellCmd("input swipe 800 2200 800 400"); //滑动坐标
                                        Log.i(TAG, "使用ADB命令滑动成功.");
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }catch (Exception ignored){}
                break;
        }
    }
    /**
     * 服务中断
     */
    @Override
    public void onInterrupt() {

    }
    /**
     * 服务连接
     */
    @Override
    protected void onServiceConnected() {
        if(upgradeRootPermission(getPackageCodePath())) msroot = true;
        Toast.makeText(this, "......抖音视频服务开启......", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }
    /**
     * 服务断开
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "抖音视频服务关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }
}
